package org.minimalj.repository.sql;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.minimalj.application.Configuration;
import org.minimalj.model.Code;
import org.minimalj.model.EnumUtils;
import org.minimalj.model.Keys;
import org.minimalj.model.Keys.MethodProperty;
import org.minimalj.model.Model;
import org.minimalj.model.View;
import org.minimalj.model.ViewUtils;
import org.minimalj.model.annotation.Materialized;
import org.minimalj.model.annotation.Searched;
import org.minimalj.model.properties.ChainedProperty;
import org.minimalj.model.properties.FieldProperty;
import org.minimalj.model.properties.FlatProperties;
import org.minimalj.model.properties.Property;
import org.minimalj.model.test.ModelTest;
import org.minimalj.repository.DataSourceFactory;
import org.minimalj.repository.TransactionalRepository;
import org.minimalj.repository.query.Criteria;
import org.minimalj.repository.query.Query;
import org.minimalj.repository.sql.SqlDialect.PostgresqlDialect;
import org.minimalj.util.CloneHelper;
import org.minimalj.util.Codes;
import org.minimalj.util.FieldUtils;
import org.minimalj.util.IdUtils;
import org.minimalj.util.LoggingRuntimeException;
import org.minimalj.util.StringUtils;

/**
 * The Mapper to a relational Database
 * 
 */
public class SqlRepository implements TransactionalRepository {
	private static final Logger logger = Logger.getLogger(SqlRepository.class.getName());
	
	protected final SqlDialect sqlDialect;
	protected final SqlIdentifier sqlIdentifier;
	
	final Map<Class<?>, AbstractTable<?>> tables = new LinkedHashMap<>();
	
	private final Map<String, AbstractTable<?>> tableByName = new HashMap<>();
	private final Map<Class<?>, LinkedHashMap<String, Property>> columnsForClass = new HashMap<>(200);
	private final Map<Class<?>, HashMap<String, Property>> columnsForClassUpperCase = new HashMap<>(200);
	
	protected final Set<Class<? extends Enum<?>>> enums = new HashSet<>();
	
	protected final DataSource dataSource;
	
	private Connection autoCommitConnection;
	private final BlockingDeque<Connection> connectionDeque = new LinkedBlockingDeque<>();
	private final ThreadLocal<Stack<Connection>> threadLocalTransactionConnection = new ThreadLocal<>();

	public SqlRepository(Model model) {
		this(DataSourceFactory.create(), model.getEntityClasses());
	}
	
	public SqlRepository(DataSource dataSource, Class<?>... classes) {
		this.dataSource = dataSource;
		new ModelTest(classes).assertValid();

		try (Connection connection = getAutoCommitConnection()) {
			sqlDialect = findDialect(connection);
			sqlIdentifier = createSqlIdentifier();
			Model.getEntityClassesRecursive(classes).forEach(this::addClass);
			tables.values().forEach(table -> table.collectEnums(enums));
			
			SchemaPreparation schemaPreparation = getSchemaPreparation();
			schemaPreparation.prepare(this);
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, logger, "Initialize of SqlRepository failed");
		}
	}

	private SchemaPreparation getSchemaPreparation() throws SQLException {
		if (Configuration.available("schemaPreparation")) {
			try {
				return SchemaPreparation.valueOf(Configuration.get("schemaPreparation")); 
			} catch (IllegalArgumentException x) {
				logger.severe("Valid values for schemaPreparation: none, create, verify, update");
				throw x;
			}
		}
		// If the classes are not in the classpath a 'instanceof' would throw ClassNotFoundError
		if (StringUtils.equals(dataSource.getClass().getName(), "org.h2.jdbcx.JdbcDataSource")) {
			String url = ((org.h2.jdbcx.JdbcDataSource) dataSource).getUrl();
			if (url.startsWith("jdbc:h2:mem")) {
				return SchemaPreparation.create;
			}
			try (ResultSet tableDescriptions = getConnection().getMetaData().getTables(null, "PUBLIC", null, new String[] {"TABLE"})) {
				if (!tableDescriptions.next()) {
					return SchemaPreparation.create;
				}
			}
		} else if (sqlDialect instanceof PostgresqlDialect) {
			Integer tableCount = execute(Integer.class, "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = current_schema");
			if (tableCount == null || tableCount == 0) {
				return SchemaPreparation.create;
			} else {
				return SchemaPreparation.update;
			}
		}
		return SchemaPreparation.none;
	}

	private SqlDialect findDialect(Connection connection) throws SQLException {
		if (Configuration.available("MjSqlDialect")) {
			return Configuration.getClazz("MjSqlDialect", SqlDialect.class);
		}
		
		String databaseProductName = connection.getMetaData().getDatabaseProductName();
		if (StringUtils.equals(databaseProductName, "MySQL", "MariaDB")) {
			return new SqlDialect.MariaSqlDialect();
		} else if (StringUtils.equals(databaseProductName, "PostgreSQL")) {
			return new PostgresqlDialect();
		} else if (StringUtils.equals(databaseProductName, "H2")) {
			return new SqlDialect.H2SqlDialect();
		} else if (StringUtils.equals(databaseProductName, "Oracle")) {
			return new SqlDialect.OracleSqlDialect();
		} else if (StringUtils.equals(databaseProductName, "Microsoft SQL Server")) {
			return new SqlDialect.MsSqlDialect();
		} else {
			return new SqlDialect.H2SqlDialect();
//			throw new RuntimeException("Only Oracle, H2, MySQL/MariaDB and SQL Server supported at the moment. ProductName: " + databaseProductName);
		}
	}

	protected SqlIdentifier createSqlIdentifier() {
		if (sqlDialect instanceof PostgresqlDialect) {
			// https://stackoverflow.com/questions/13409094/why-does-postgresql-default-everything-to-lower-case
			return new SqlIdentifier(sqlDialect.getMaxIdentifierLength()) {
				protected String identifier(String identifier, Set<String> alreadyUsedIdentifiers) {
					identifier = super.identifier(identifier, alreadyUsedIdentifiers);
					return identifier.toLowerCase();
				}
			};
		} else {
			return new SqlIdentifier(sqlDialect.getMaxIdentifierLength());
		}
	}
	
	protected Connection getAutoCommitConnection() {
		try {
			if (autoCommitConnection == null || !sqlDialect.isValid(autoCommitConnection)) {
				autoCommitConnection = dataSource.getConnection();
				autoCommitConnection.setAutoCommit(true);
			}
			return autoCommitConnection;
		} catch (Exception e) {
			throw new LoggingRuntimeException(e, logger, "Not possible to create autocommit connection");
		}
	}
	
	public SqlDialect getSqlDialect() {
		return sqlDialect;
	}

	@Override
	public void startTransaction(int transactionIsolationLevel) {
		Connection transactionConnection = allocateConnection(transactionIsolationLevel);
		if (threadLocalTransactionConnection.get() == null) {
			threadLocalTransactionConnection.set(new Stack<>());
		}
		if (!threadLocalTransactionConnection.get().isEmpty() && Configuration.get("MjForbidInnerTransaction", "false").equals("true")) {
			throw new IllegalStateException("Start a new transaction while other transaction already startet is not allowed");
		}
		threadLocalTransactionConnection.get().push(transactionConnection);
	}

	@Override
	public void endTransaction(boolean commit) {
		Connection transactionConnection = isTransactionActive() ? threadLocalTransactionConnection.get().peek() : null;
		if (transactionConnection == null) return;
		
		try {
			if (!transactionConnection.getAutoCommit()) {
				if (commit) {
					transactionConnection.commit();
				} else {
					transactionConnection.rollback();
				}
			}
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, logger, "Transaction " + (commit ? "commit" : "rollback") + " failed");
		}
		
		releaseConnection(transactionConnection);
		threadLocalTransactionConnection.get().pop();
	}
	
	protected Connection allocateConnection(int transactionIsolationLevel) {
		logger.finest(() -> "Current connections in pool " + connectionDeque.size());
		Connection connection = connectionDeque.poll();
		while (true) {
			boolean valid = false;
			try {
				valid = connection != null && connection.isValid(0);
			} catch (SQLException x) {
				try {
					logger.warning("connection.isValid failed: " + x.getLocalizedMessage());
					connection.close();
				} catch (Exception x2) {
					logger.log(Level.WARNING, "connection.close failed", x);
				}
			}
			try {
				if (!valid) {
					connection = dataSource.getConnection();
				}
				if (transactionIsolationLevel != Connection.TRANSACTION_NONE) {
					connection.setTransactionIsolation(transactionIsolationLevel);
					connection.setAutoCommit(false);
				} else {
					connection.setAutoCommit(true);
				}
				return connection;
			} catch (Exception e) {
				// this could happen if there are already too many connections
				logger.log(Level.FINE, "Not possible to create additional connection", e);
			}
			// so no connection available and not possible to create one
			// block and wait till a connection is in deque
			try {
				connectionDeque.poll(10, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				logger.log(Level.FINEST, "poll for connection interrupted", e);
			}
		}
	}
	
	private void releaseConnection(Connection connection) {
		// last in first out in the hope that recent accessed objects are the fastest
		connectionDeque.push(connection);
	}
	
	/**
	 * Use with care. Removes all content of all tables. Should only
	 * be used for JUnit tests.
	 */
	public void clear() {
		List<AbstractTable<?>> tableList = new ArrayList<>(tables.values());
		for (AbstractTable<?> table : tableList) {
			table.clear();
		}
	}

	public boolean isTransactionActive() {
		return threadLocalTransactionConnection.get() != null && threadLocalTransactionConnection.get().size() > 0;
	}
	
	// TODO make final
	public Connection getConnection() {
		if (isTransactionActive()) {
			return threadLocalTransactionConnection.get().peek();
		} else {
			return getAutoCommitConnection();
		}
	}
	
	private boolean createTablesOnInitialize(DataSource dataSource) throws SQLException {
		// If the classes are not in the classpath a 'instanceof' would throw ClassNotFoundError
		if (StringUtils.equals(dataSource.getClass().getName(), "org.h2.jdbcx.JdbcDataSource")) {
			String url = ((org.h2.jdbcx.JdbcDataSource) dataSource).getUrl();
			if (url.startsWith("jdbc:h2:mem")) {
				return true;
			}
			try (ResultSet tableDescriptions = getConnection().getMetaData().getTables(null, "PUBLIC", null, new String[] {"TABLE"})) {
				return !tableDescriptions.next();
			}
		}
		return false;
	}
	
	@Override
	public <T> T read(Class<T> clazz, Object id) {
		if (View.class.isAssignableFrom(clazz)) {
			@SuppressWarnings("unchecked")
			Table<T> table = (Table<T>) getTable(ViewUtils.getViewedClass(clazz));
			return table.readView(clazz, id, new HashMap<>());
		} else {
			return getTable(clazz).read(id);
		}
	}

	@Override
	public <T> List<T> find(Class<T> resultClass, Query query) {
		@SuppressWarnings("unchecked")
		Table<T> table = (Table<T>) getTable(ViewUtils.resolve(resultClass));
		return table.find(query, resultClass, new HashMap<>());
	}
		
	@SuppressWarnings("unchecked")
	@Override
	public <T> long count(Class<T> clazz, Criteria criteria) {
		if (View.class.isAssignableFrom(clazz)) {
			clazz = (Class<T>) ViewUtils.getViewedClass(clazz);
		}
		Table<?> table = getTable(clazz);
		return table.count(criteria);
	}

	@Override
	public <T> Object insert(T object) {
		if (object == null) throw new NullPointerException();
		Object originalId = IdUtils.getId(object);
		@SuppressWarnings("unchecked")
		Table<T> table = getTable((Class<T>) object.getClass());
		try {
			return table.insert(object);
		} finally {
			// all repositories should behave to same and not set the new id in the
			// original object.
			IdUtils.setId(object, originalId);
		}
	}

	@Override
	public <T> void update(T object) {
		if (object == null) throw new NullPointerException();
		@SuppressWarnings("unchecked")
		Table<T> table = getTable((Class<T>) object.getClass());
		table.update(object);
	}

	@Override
	public <T> void delete(T object) {
		@SuppressWarnings("unchecked")
		Table<T> table = getTable((Class<T>) object.getClass());
		table.delete(object);
	}

	@Override
	public <T> int delete(Class<T> clazz, Criteria criteria) {
		Table<T> table = getTable(clazz);
		return table.delete(clazz, criteria);
	}
	
	public <T> void deleteAll(Class<T> clazz) {
		Table<T> table = getTable(clazz);
		table.clear();
	}

	//
	
	private PreparedStatement createStatement(Connection connection, String query, Object[] parameters) throws SQLException {
		PreparedStatement preparedStatement = AbstractTable.createStatement(getConnection(), query, false);
		int param = 1; // !
		for (Object parameter : parameters) {
			getSqlDialect().setParameter(preparedStatement, param++, parameter);
		}
		return preparedStatement;
	}

	public LinkedHashMap<String, Property> findColumns(Class<?> clazz) {
		if (columnsForClass.containsKey(clazz)) {
			return columnsForClass.get(clazz);
		}
		return findColumns(clazz, false);
	}
	
	public LinkedHashMap<String, Property> findColumns(Class<?> clazz, boolean includeTransient) {
		LinkedHashMap<String, Property> columns = new LinkedHashMap<>();
		for (Field field : clazz.getFields()) {
			if (!FieldUtils.isPublic(field) || FieldUtils.isStatic(field) || !includeTransient && FieldUtils.isTransient(field)) continue;
			String fieldName = field.getName();
			if (StringUtils.equals(fieldName, "id", "version", "historized")) continue;
			if (FieldUtils.isList(field)) continue;
			if (!FieldUtils.isFinal(field) && AbstractTable.isDependable(field.getType())) continue;
			if (FieldUtils.isFinal(field) && !FieldUtils.isSet(field) && !Codes.isCode(field.getType())) {
				Map<String, Property> inlinePropertys = findColumns(field.getType(), includeTransient);
				boolean hasClassName = FieldUtils.hasClassName(field) && !FlatProperties.hasCollidingFields(clazz, field.getType(), field.getName());
				for (String inlineKey : inlinePropertys.keySet()) {
					String key = inlineKey;
					if (!hasClassName) {
						key = fieldName + "_" + inlineKey;
					}
					key = sqlIdentifier.column(key, columns.keySet(), field.getType());
					columns.put(key, new ChainedProperty(new FieldProperty(field, clazz), inlinePropertys.get(inlineKey)));
				}
			} else {
				fieldName = sqlIdentifier.column(fieldName, columns.keySet(), field.getType());
				columns.put(fieldName, new FieldProperty(field, clazz));
			}
		}
		for (Method method: clazz.getMethods()) {
			if (!Keys.isPublic(method) || Keys.isStatic(method)) continue;
			if (method.getAnnotation(Searched.class) == null && method.getAnnotation(Materialized.class) == null) continue;
			String methodName = method.getName();
			if (!methodName.startsWith("get") || methodName.length() < 4) continue;
			String fieldName = StringUtils.lowerFirstChar(methodName.substring(3));
			String columnName = sqlIdentifier.column(fieldName, columns.keySet(), method.getReturnType());
			columns.put(columnName, new Keys.MethodProperty(method.getReturnType(), fieldName, method, null));
		}
		columnsForClass.put(clazz, columns);
		return columns;
	}	
	
	protected HashMap<String, Property> findColumnsUpperCase(Class<?> clazz) {
		if (columnsForClassUpperCase.containsKey(clazz)) {
			return columnsForClassUpperCase.get(clazz);
		}
		LinkedHashMap<String, Property> columns = findColumns(clazz, true);
		HashMap<String, Property> columnsUpperCase = new HashMap<>(columns.size() * 3);
		columns.forEach((key, value) -> columnsUpperCase.put(key.toUpperCase(), value));
		columnsForClassUpperCase.put(clazz, columnsUpperCase);
		return columnsUpperCase;
	}

	public <T> List<T> find(Class<T> clazz, String query, int maxResults, Object... parameters) {
		try (PreparedStatement preparedStatement = createStatement(getConnection(), query, parameters)) {
			if (tables.containsKey(clazz)) {
				Table<T> table = (Table<T>) tables.get(clazz);
				return table.executeSelectAll(preparedStatement);
			} else if (View.class.isAssignableFrom(clazz) && tables.containsKey(ViewUtils.getViewedClass(clazz))) {
				Table<T> table = (Table<T>) tables.get(ViewUtils.getViewedClass(clazz));
				return table.executeSelectViewAll(clazz, preparedStatement);
			} else {
				try (ResultSet resultSet = preparedStatement.executeQuery()) {
					List<T> result = new ArrayList<>();
					Map<Class<?>, Map<Object, Object>> loadedReferences = new HashMap<>();
					while (resultSet.next() && result.size() < maxResults) {
						result.add(readResultSetRow(clazz, resultSet, loadedReferences));
					}
					return result;
				}
			}
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, logger, "Couldn't execute query");
		}
	}

	public int execute(String query, Serializable... parameters) {
		try (PreparedStatement preparedStatement = createStatement(getConnection(), query, parameters)) {
			preparedStatement.execute();
			return preparedStatement.getUpdateCount();
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, logger, "Couldn't execute query");
		}
	}

	public <T> T execute(Class<T> resultClass, String query, Serializable... parameters) {
		try (PreparedStatement preparedStatement = createStatement(getConnection(), query, parameters)) {
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				T result = null;
				if (resultSet.next()) {
					result = readResultSetRow(resultClass, resultSet);
				}
				return result;
			}
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, logger, "Couldn't execute query");
		}
	}

	public <R> R readResultSetRow(Class<R> clazz, ResultSet resultSet) throws SQLException {
		Map<Class<?>, Map<Object, Object>> loadedReferences = new HashMap<>();
		return readResultSetRow(clazz, resultSet, loadedReferences);
	}

	public static final Map<Class<?>, Map<Object, Object>> DONT_LOAD_REFERENCES = null;
	
	@SuppressWarnings("unchecked")
	public <R> R readResultSetRow(Class<R> clazz, ResultSet resultSet, Map<Class<?>, Map<Object, Object>> loadedReferences) throws SQLException {
		if (FieldUtils.isAllowedPrimitive(clazz)) {
			return (R) sqlDialect.convertToFieldClass(clazz, resultSet.getObject(1));
		}
		
		Object id = null;
		Integer version = null;
		Integer position = null;
		
		HashMap<String, Property> columns = findColumnsUpperCase(clazz);
		
		// first read the resultSet completely then resolve references
		// some db mixes closing of resultSets.
		
		Map<Property, Object> values = new HashMap<>(resultSet.getMetaData().getColumnCount() * 3);
		for (int columnIndex = 1; columnIndex <= resultSet.getMetaData().getColumnCount(); columnIndex++) {
			String columnName = resultSet.getMetaData().getColumnLabel(columnIndex).toUpperCase();
			if ("ID".equals(columnName)) {
				id = resultSet.getObject(columnIndex);
				continue;
			} else if ("VERSION".equals(columnName)) {
				version = resultSet.getInt(columnIndex);
				continue;
			} else if ("POSITION".equals(columnName)) {
				position = resultSet.getInt(columnIndex);
				continue;				
			}
			
			Property property = columns.get(columnName);
			if (property == null) {
				logger.log(Level.FINE, "Column not found: " + columnName);
				continue;
			}
			
			Class<?> fieldClass = property.getClazz();
			boolean isByteArray = fieldClass.isArray() && fieldClass.getComponentType() == Byte.TYPE;

			Object value;
			if (isByteArray) {
				value = resultSet.getBytes(columnIndex);
			} else if (fieldClass == BigDecimal.class) {
				// MS Sql getObject returns float
				value = resultSet.getBigDecimal(columnIndex);
			} else {
				value = resultSet.getObject(columnIndex);
			}
			if (value == null) continue;
			values.put(property, value);
		}
		
		R result;
		if (!Codes.isCode(clazz)) {
			result = CloneHelper.newInstance(clazz);
			IdUtils.setId(result, id);
		} else {
			// Self reference is allowed for Codes. Use a previously referenced instance.
			result = (R) Codes.getOrInstantiate((Class<? extends Code>) clazz, id);
		}
		if (version != null) {
			IdUtils.setVersion(result, version);
		}

		if (id != null && loadedReferences != DONT_LOAD_REFERENCES) {
			if (!loadedReferences.containsKey(clazz)) {
				loadedReferences.put(clazz, new HashMap<>());
			}
			Object key = position == null ? id : id + "-" + position;
			if (loadedReferences.get(clazz).containsKey(key)) {
				return (R) loadedReferences.get(clazz).get(key);
			} else {
				loadedReferences.get(clazz).put(key, result);
			}
		}
		
		for (Map.Entry<Property, Object> entry : values.entrySet()) {
			Property property = entry.getKey();
			Object value = entry.getValue();
			if (value != null && !(property instanceof MethodProperty)) {
				Class<?> fieldClass = property.getClazz();
				if (Codes.isCode(fieldClass)) {
					Class<? extends Code> codeClass = (Class<? extends Code>) fieldClass;
					value = Codes.get(codeClass, value);
				} else if (IdUtils.hasId(fieldClass)) {
					value = loadReference(value, fieldClass, loadedReferences);
				} else if (AbstractTable.isDependable(property)) {
					continue;
				} else if (fieldClass == Set.class) {
					Set<?> set = (Set<?>) property.getValue(result);
					Class<?> enumClass = property.getGenericClass();
					EnumUtils.fillSet((int) value, enumClass, set);
					continue; // skip setValue, it's final
				} else {
					value = sqlDialect.convertToFieldClass(fieldClass, value);
				}
				property.setValue(result, value);
			}
		}
		return result;
	}

	protected <C> C loadReference(Object value, Class<C> fieldClass, Map<Class<?>, Map<Object, Object>> loadedReferences) {
		if (loadedReferences != DONT_LOAD_REFERENCES) {
			Map<Object, Object> loadedReferencesOfClass = loadedReferences.computeIfAbsent(fieldClass, c -> new HashMap<>());
			if (loadedReferencesOfClass.containsKey(value)) {
				value = loadedReferencesOfClass.get(value);
			} else {
				Object referencedValue;
				if (View.class.isAssignableFrom(fieldClass)) {
					Class<?> viewedClass = ViewUtils.getViewedClass(fieldClass);
					if (Codes.isCode(viewedClass)) {
						Class<? extends Code> codeClass = (Class<? extends Code>) viewedClass;
						referencedValue = ViewUtils.view(Codes.get(codeClass, value), CloneHelper.newInstance(fieldClass));
					} else {
						Table<?> referenceTable = getTable(viewedClass);
						referencedValue = referenceTable.readView(fieldClass, value, loadedReferences);
					}
				} else {
					Table<?> referenceTable = getTable(fieldClass);
					referencedValue = referenceTable.read(value, loadedReferences);
				}
				loadedReferencesOfClass.put(value, referencedValue);
				value = referencedValue;
			}
		} else {
			Object shallowObject = CloneHelper.newInstance(fieldClass);
			IdUtils.setId(shallowObject, value);
			value = shallowObject;
		}
		return (C) value;
	}
	
	@SuppressWarnings("unchecked")
	public <R> R readResultSetRowPrimitive(Class<R> clazz, ResultSet resultSet) throws SQLException {
		Object value = resultSet.getObject(1);
		return (R) sqlDialect.convertToFieldClass(clazz, value);
	}
	
	//
	
	<U> void addClass(Class<U> clazz) {
		if (!tables.containsKey(clazz)) {
			tables.put(clazz, null); // break cycle. at some point it is checked if a clazz is already in the tables map.
			Table<U> table = createTable(clazz);
			tables.put(table.getClazz(), table);
		}
	}
	
	protected <U> Table<U> createTable(Class<U> clazz) {
		return new Table<>(this, clazz);
	}
	
	protected void beforeCreateTables() {
		// for extensions
	}
	
	void afterCreateTables() {
		afterCreateTables(Collections.unmodifiableCollection(tables.values()));
	}
	
	protected void afterCreateTables(Collection<AbstractTable<?>> tables) {
		// for extensions
	}
	
	void dropTables() {
		Collection<AbstractTable<?>> tables = Collections.unmodifiableCollection(this.tables.values());
		beforeDropTables(tables);
		for (AbstractTable<?> table : tables) {
			table.dropConstraints(sqlDialect);
		}
		for (AbstractTable<?> table : tables) {
			table.dropTable(sqlDialect);
		}
	}

	protected void beforeDropTables(Collection<AbstractTable<?>> tables) {
		// for extensions
	}

	protected BiFunction<Class<?>, Object, Object> getObjectProvider() {
		return new BiFunction<Class<?>, Object, Object>() {
			@Override
			public Object apply(Class<?> clazz, Object id) {
				return read(clazz, id);
			}
		};
	}
	
	@SuppressWarnings("unchecked")
	public <U> AbstractTable<U> getAbstractTable(Class<U> clazz) {
		if (!tables.containsKey(clazz)) {
			if (View.class.isAssignableFrom(clazz)) {
				throw new IllegalArgumentException(clazz.getName() + " is a View and cannot be inserted directly. This happens if the id in the View Object is not set.");
			} else {
				throw new IllegalArgumentException("No (Sql)Table available for + " + clazz.getName() + ". May be missing in Application.getEntitiyClasses()");
			}
		}
		return (AbstractTable<U>) tables.get(clazz);
	}

	public <U> Table<U> getTable(Class<U> clazz) {
		AbstractTable<U> table = getAbstractTable(clazz);
		if (!(table instanceof Table)) throw new IllegalArgumentException(clazz.getName());
		return (Table<U>) table;
	}

	@SuppressWarnings("unchecked")
	public <U> Table<U> getTable(String className) {
		for (Entry<Class<?>, AbstractTable<?>> entry : tables.entrySet()) {
			if (entry.getKey().getName().equals(className)) {
				return (Table<U>) entry.getValue();
			}
		}
		return null;
	}
	
	public String name(Object classOrKey) {
		if (classOrKey instanceof Class) {
			// TODO
			// CrossTable liefern die gleich Klasse wie die Parent - Klasse, was dann die Parent-Klasse manchmal überdeckt
			return tableByName.entrySet().stream().filter(e -> !(e.getValue() instanceof CrossTable)).filter(e -> e.getValue().getClazz() == classOrKey).findAny().get().getKey();
		} else if (!Keys.isKeyObject(classOrKey) && classOrKey instanceof Enum) {
			return sqlDialect.enumSql((Enum<?>) classOrKey);
		} else {
			return column(classOrKey);
		}
	}

	public String table(Class<?> clazz) {
		AbstractTable<?> table = getAbstractTable(clazz);
		return table.getTableName();
	}
	
	public String column(Object key) {
		Property property = Keys.getProperty(key);
		Class<?> declaringClass;
		if (property instanceof ChainedProperty) {
			ChainedProperty chainedProperty = (ChainedProperty) property;
			declaringClass = chainedProperty.getChain().get(0).getDeclaringClass();
		} else {
			declaringClass = property.getDeclaringClass();
		}
		Map<String, Property> columns = findColumns(declaringClass);
		for (Map.Entry<String, Property> entry : columns.entrySet()) {
			if (StringUtils.equals(entry.getValue().getPath(), property.getPath())) {
				return entry.getKey();
			}
		}
		columns = findColumnsUpperCase(declaringClass);
		for (Map.Entry<String, Property> entry : columns.entrySet()) {
			if (StringUtils.equals(entry.getValue().getPath(), property.getPath())) {
				return entry.getKey();
			}
		}
		return null;
	}
	
	public boolean tableExists(Class<?> clazz) {
		return tables.containsKey(clazz);
	}
	
	public int getMaxIdentifierLength() {
		return sqlDialect.getMaxIdentifierLength();
	}

	public Map<String, AbstractTable<?>> getTableByName() {
		return tableByName;
	}
}