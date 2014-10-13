package org.minimalj.backend.db;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.minimalj.model.EnumUtils;
import org.minimalj.model.Keys;
import org.minimalj.model.PropertyInterface;
import org.minimalj.model.ViewUtil;
import org.minimalj.model.annotation.Code;
import org.minimalj.model.annotation.Required;
import org.minimalj.model.properties.ChainedProperty;
import org.minimalj.model.properties.SimpleProperty;
import org.minimalj.transaction.criteria.Criteria;
import org.minimalj.transaction.criteria.CriteriaOperator;
import org.minimalj.util.CloneHelper;
import org.minimalj.util.Codes;
import org.minimalj.util.EqualsHelper;
import org.minimalj.util.FieldUtils;
import org.minimalj.util.GenericUtils;
import org.minimalj.util.IdUtils;
import org.minimalj.util.LoggingRuntimeException;
import org.minimalj.util.StringUtils;

/**
 * Minimal-J internal<p>
 *
 * Base class of all table representing classes in this persistence layer.
 * Normally you should not need to extend from this class directly. Use
 * the existing subclasses or only the methods in DbPersistence.
 * 
 */
public abstract class AbstractTable<T> {
	public static final Logger sqlLogger = Logger.getLogger("SQL");
	
	private static final Map<Class<?>, LinkedHashMap<String, PropertyInterface>> columnsForClass = new HashMap<>(200);
	
	protected final DbPersistence dbPersistence;
	protected final DbPersistenceHelper helper;
	protected final Class<T> clazz;
	protected final LinkedHashMap<String, PropertyInterface> columns;
	protected final LinkedHashMap<String, PropertyInterface> lists;
	
	protected final String name;

	protected final PropertyInterface idProperty;

	protected final List<String> indexes = new ArrayList<>();
	
	protected final Map<Connection, Map<String, PreparedStatement>> statements = new HashMap<>();

	protected final String selectByIdQuery;
	protected final String insertQuery;
	protected final String clearQuery;
	
	// TODO: its a little bit strange to pass the idProperty here. Also because the property
	// is not allways a property of clazz. idProperty is only necessary because the clazz AND the
	// size of the idProperty is needed
	protected AbstractTable(DbPersistence dbPersistence, String name, Class<T> clazz, PropertyInterface idProperty) {
		this.dbPersistence = dbPersistence;
		this.helper = new DbPersistenceHelper(dbPersistence);
		this.name = name != null ? name : StringUtils.toDbName(clazz.getSimpleName());
		this.clazz = clazz;
		this.idProperty = idProperty;
		this.columns = findColumns(clazz);
		this.lists = findLists(clazz);
		
		this.selectByIdQuery = selectByIdQuery();
		this.insertQuery = insertQuery();
		this.clearQuery = clearQuery();
		
		findCodes();
		findDependables();
		findIndexes();
	}

	protected static LinkedHashMap<String, PropertyInterface> findColumns(Class<?> clazz) {
		if (columnsForClass.containsKey(clazz)) {
			return columnsForClass.get(clazz);
		}
		
		LinkedHashMap<String, PropertyInterface> columns = new LinkedHashMap<String, PropertyInterface>();
		for (Field field : clazz.getFields()) {
			if (!FieldUtils.isPublic(field) || FieldUtils.isStatic(field) || FieldUtils.isTransient(field)) continue;
			String fieldName = StringUtils.toDbName(field.getName());
			if (StringUtils.equals(fieldName, "ID", "VERSION")) continue;
			if (FieldUtils.isList(field)) continue;
			if (FieldUtils.isFinal(field) && !FieldUtils.isSet(field) && !Codes.isCode(field.getType())) {
				Map<String, PropertyInterface> inlinePropertys = findColumns(field.getType());
				boolean hasClassName = FieldUtils.hasClassName(field);
				for (String inlineKey : inlinePropertys.keySet()) {
					String key = inlineKey;
					if (!hasClassName) {
						key = fieldName + "_" + inlineKey;
					}
					columns.put(key, new ChainedProperty(clazz, field, inlinePropertys.get(inlineKey)));
				}
			} else {
				columns.put(fieldName, new SimpleProperty(clazz, field));
			}
		}
		columnsForClass.put(clazz, columns);
		return columns;
	}	
	
	protected static LinkedHashMap<String, PropertyInterface> findLists(Class<?> clazz) {
		LinkedHashMap<String, PropertyInterface> properties = new LinkedHashMap<String, PropertyInterface>();
		
		for (Field field : clazz.getFields()) {
			if (!FieldUtils.isPublic(field) || FieldUtils.isStatic(field) || FieldUtils.isTransient(field)) continue;
			if (FieldUtils.isFinal(field) && !FieldUtils.isList(field)) {
				// This is needed to check if an inline Property contains a List
				Map<String, PropertyInterface> inlinePropertys = findLists(field.getType());
				boolean hasClassName = FieldUtils.hasClassName(field);
				for (String inlineKey : inlinePropertys.keySet()) {
					String key = inlineKey;
					if (!hasClassName) {
						key = field.getName() + StringUtils.upperFirstChar(inlineKey);
					}
					properties.put(key, new ChainedProperty(clazz, field, inlinePropertys.get(inlineKey)));
				}
			} else if (FieldUtils.isList(field)) {
				properties.put(field.getName(), new SimpleProperty(clazz, field));
			}
		}
		return properties; 
	}
	
	protected LinkedHashMap<String, PropertyInterface> getColumns() {
		return columns;
	}

	protected LinkedHashMap<String, PropertyInterface> getLists() {
		return lists;
	}
	
	protected Collection<String> getIndexes() {
		return indexes;
	}
	
	protected PreparedStatement getStatement(Connection connection, String query, boolean returnGeneratedKeys) throws SQLException {
		if (!statements.containsKey(connection)) {
			statements.put(connection, new HashMap<String, PreparedStatement>());
		}
		Map<String, PreparedStatement> statementsForConnection = statements.get(connection);
		if (!statementsForConnection.containsKey(query)) {
			statementsForConnection.put(query, createStatement(connection, query, returnGeneratedKeys));
		}
		return statementsForConnection.get(query);
	}
	
	static PreparedStatement createStatement(Connection connection, String query, boolean returnGeneratedKeys) throws SQLException {
		if (returnGeneratedKeys) {
			if (sqlLogger.isLoggable(Level.FINE)) {
				return new LoggingPreparedStatement(connection, query, Statement.RETURN_GENERATED_KEYS, sqlLogger);
			} else {
				return connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			}
		} else {
			if (sqlLogger.isLoggable(Level.FINE)) {
				return new LoggingPreparedStatement(connection, query, sqlLogger);
			} else {
				return connection.prepareStatement(query);
			}
		}
	}
	
	protected void execute(String s) {
		try (PreparedStatement statement = createStatement(dbPersistence.getConnection(), s.toString(), false)) {
			statement.execute();
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, sqlLogger, "Statement failed: \n" + s.toString());
		}
	}

	protected void createTable(DbSyntax syntax) {
		StringBuilder s = new StringBuilder();
		syntax.addCreateStatementBegin(s, getTableName());
		addSpecialColumns(syntax, s);
		addFieldColumns(syntax, s);
		addPrimaryKey(syntax, s);
		syntax.addCreateStatementEnd(s);
		
		execute(s.toString());
	}
	
	protected abstract void addSpecialColumns(DbSyntax syntax, StringBuilder s);
	
	protected void addFieldColumns(DbSyntax syntax, StringBuilder s) {
		for (Map.Entry<String, PropertyInterface> column : getColumns().entrySet()) {
			s.append(",\n "); s.append(column.getKey()); s.append(" "); 

			PropertyInterface property = column.getValue();
			syntax.addColumnDefinition(s, property);
			boolean isRequired = property.getAnnotation(Required.class) != null;
			s.append(isRequired ? " NOT NULL" : " DEFAULT NULL");
		}
	}

	protected void addPrimaryKey(DbSyntax syntax, StringBuilder s) {
		syntax.addPrimaryKey(s, "ID");
	}
	
	protected void createIndexes(DbSyntax syntax) {
		for (String index : indexes) {
			String s = syntax.createIndex(getTableName(), index, this instanceof HistorizedTable);
			execute(s.toString());
		}
	}
	
	protected void createConstraints(DbSyntax syntax) {
		for (Map.Entry<String, PropertyInterface> column : getColumns().entrySet()) {
			PropertyInterface property = column.getValue();
			
			if (DbPersistenceHelper.isReference(property)) {
				Class<?> fieldClass = ViewUtil.resolve(property.getFieldClazz());
				AbstractTable<?> referencedTable = dbPersistence.table(fieldClass);

				String s = syntax.createConstraint(getTableName(), column.getKey(), referencedTable.getTableName(), referencedTable instanceof HistorizedTable);
				if (s != null) {
					execute(s.toString());
				}
			}
		}
	}
	
	public void clear() {
		try {
			PreparedStatement statement = getStatement(dbPersistence.getConnection(), clearQuery, false);
			statement.execute();
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, sqlLogger, "Clear of Table " + getTableName() + " failed");
		}
	}

	private String findColumn(String fieldPath) {
		for (Map.Entry<String, PropertyInterface> entry : columns.entrySet()) {
			if (entry.getValue().getFieldPath().equals(fieldPath)) {
				return entry.getKey();
			}
		}
		return null;
	}

	protected String getTableName() {
		return name;
	}
	
	public Class<T> getClazz() {
		return clazz;
	}
	
	private void findCodes() {
		for (Map.Entry<String, PropertyInterface> column : getColumns().entrySet()) {
			PropertyInterface property = column.getValue();
			Class<?> fieldClazz = property.getFieldClazz();
			if (Code.class.isAssignableFrom(fieldClazz) && !dbPersistence.tableExists(fieldClazz)) {
				dbPersistence.addClass(fieldClazz);
			}
		}
	}
	
	private void findDependables() {
		for (Map.Entry<String, PropertyInterface> column : getColumns().entrySet()) {
			PropertyInterface property = column.getValue();
			if (ViewUtil.isView(property)) continue;
			Class<?> fieldClazz = property.getFieldClazz();
			if (DbPersistenceHelper.isReference(property) && !dbPersistence.tableExists(fieldClazz) ) {
				dbPersistence.addClass(fieldClazz);
			}
		}
	}

	protected void findIndexes() {
		for (Map.Entry<String, PropertyInterface> column : columns.entrySet()) {
			PropertyInterface property = column.getValue();
			if (ViewUtil.isView(property)) {
				createIndex(property, property.getFieldPath());
			}
		}
	}
	
	protected String whereStatement(final String wholeFieldPath, CriteriaOperator criteriaOperator) {
		String fieldPath = wholeFieldPath;
		String column;
		while (true) {
			column = findColumn(fieldPath);
			if (column != null) break;
			int pos = fieldPath.lastIndexOf('.');
			if (pos < 0) throw new IllegalArgumentException("FieldPath " + wholeFieldPath + " not even partially found in " + getTableName());
			fieldPath = fieldPath.substring(0, pos);
		}
		if (fieldPath.length() < wholeFieldPath.length()) {
			String restOfFieldPath = wholeFieldPath.substring(fieldPath.length() + 1);
			if ("id".equals(restOfFieldPath)) {
				return column + " " + criteriaOperator.getOperatorAsString() + " ?";
			} else {
				PropertyInterface subProperty = columns.get(column);
				AbstractTable<?> subTable = dbPersistence.table(ViewUtil.resolve(subProperty.getFieldClazz()));
				return column + " = select (ID from " + subTable.getTableName() + " where " + subTable.whereStatement(restOfFieldPath, criteriaOperator) + ")";
			}
		} else {
			return column + " " + criteriaOperator.getOperatorAsString() + " ?";
		}
	}

	// execution helpers

	protected T executeSelect(PreparedStatement preparedStatement) throws SQLException {
		return executeSelect(preparedStatement, null);
	}
	
	protected T executeSelect(PreparedStatement preparedStatement, Integer time) throws SQLException {
		try (ResultSet resultSet = preparedStatement.executeQuery()) {
			if (resultSet.next()) {
				return readResultSetRow(resultSet, time);
			} else {
				return null;
			}
		}
	}

	protected List<T> executeSelectAll(PreparedStatement preparedStatement) throws SQLException {
		return executeSelectAll(preparedStatement, Long.MAX_VALUE);
	}
	
	protected List<T> executeSelectAll(PreparedStatement preparedStatement, long maxResults) throws SQLException {
		List<T> result = new ArrayList<T>();
		try (ResultSet resultSet = preparedStatement.executeQuery()) {
			while (resultSet.next() && result.size() < maxResults) {
				T object = readResultSetRow(resultSet, null);
				if (this instanceof Table) {
					Object id = IdUtils.getId(object);
					((Table<T>) this).loadRelations(object, id);
				}
				result.add(object);
			}
		}
		return result;
	}
	
	protected T readResultSetRow(ResultSet resultSet, Integer time) throws SQLException {
		return readResultSetRow(dbPersistence, clazz,  resultSet, time);
	}
	
	protected <R> R readResultSetRow(DbPersistence dbPersistence, Class<R> clazz, ResultSet resultSet, Integer time) throws SQLException {
		R result = CloneHelper.newInstance(clazz);
		
		DbPersistenceHelper helper = new DbPersistenceHelper(dbPersistence);
		LinkedHashMap<String, PropertyInterface> columns = findColumns(clazz);
		
		// first read the resultSet completly then resolve references
		// derby db mixes closing of resultSets.
		
		Map<PropertyInterface, Object> values = new HashMap<>(resultSet.getMetaData().getColumnCount() * 3);
		for (int columnIndex = 1; columnIndex <= resultSet.getMetaData().getColumnCount(); columnIndex++) {
			String columnName = resultSet.getMetaData().getColumnName(columnIndex);
			if ("ID".equalsIgnoreCase(columnName)) {
				IdUtils.setIdSafe(result, resultSet.getObject(columnIndex));
				continue;
			} else if ("VERSION".equalsIgnoreCase(columnName)) {
				IdUtils.setVersion(result, resultSet.getInt(columnIndex));
				continue;
			}
			
			PropertyInterface property = columns.get(columnName);
			if (property == null) continue;
			
			Object value = resultSet.getObject(columnIndex);
			if (value == null) continue;
			values.put(property, value);
		}
		
		for (Map.Entry<PropertyInterface, Object> entry : values.entrySet()) {
			Object value = entry.getValue();
			PropertyInterface property = entry.getKey();
			if (value != null) {
				Class<?> fieldClass = property.getFieldClazz();
				if (Code.class.isAssignableFrom(fieldClass)) {
					value = dbPersistence.getCode(fieldClass, value);
				} else if (ViewUtil.isView(property)) {
					Class<?> viewedClass = ViewUtil.getViewedClass(property);
					Table<?> referenceTable = dbPersistence.getTable(viewedClass);
					Object referenceObject = referenceTable.read(value, false); // false -> subEntities not loaded
					
					value = CloneHelper.newInstance(fieldClass);
					ViewUtil.view(referenceObject, value);
				} else if (DbPersistenceHelper.isReference(property)) {
					value = dereference(dbPersistence, fieldClass, value, time);
				} else if (fieldClass == Set.class) {
					Set<?> set = (Set<?>) property.getValue(result);
					Class<?> enumClass = GenericUtils.getGenericClass(property.getType());
					EnumUtils.fillSet((int) value, enumClass, set);
					continue; // skip setValue, it's final
				} else {
					value = helper.convertToFieldClass(fieldClass, value);
				}
				property.setValue(result, value);
			}
		}
		return result;
	}
	
	protected static Object dereference(DbPersistence dbPersistence, Class<?> clazz, Object value, Integer time) {
		AbstractTable<?> table = dbPersistence.table(clazz);
		if (table instanceof Table) {
			return ((Table<?>) table).read(value);
		} else {
			throw new IllegalArgumentException("Clazz: " + clazz);
		}
	}

	/**
	 * creates or updates the dependable values
	 * 
	 * @param value the object from which to get the reference.
	 * @param copyOnchange
	 * @return <code>if value not found and parameter insert is false
	 * @throws SQLException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Object getReferenceValue(Object value, boolean copyOnchange) throws SQLException {
		Class<?> clazz = (Class<?>) value.getClass();
		AbstractTable<?> abstractTable = dbPersistence.table(clazz);
		if (abstractTable == null) {
			throw new IllegalArgumentException(clazz.getName());
		}
		if (abstractTable instanceof Table) {
			Table table = (Table) abstractTable;
			Object id = IdUtils.getId(value);
			Object objectInDb = id != null ? table.read(id) : null;
			if (objectInDb != null) {
				if (!EqualsHelper.equals(value, objectInDb)) {
					if (copyOnchange) {
						IdUtils.setId(value, null);
						id = table.insert(value);
					} else {
						table.update(id, value);
					}
				}
			} else {
				id = table.insert(value);
			}
			return id;
		} else {
			throw new IllegalArgumentException(clazz.getName());
		}
	}
	
	protected int setParameters(PreparedStatement statement, T object, boolean doubleValues, boolean copyReferencesOnchange, Object id) throws SQLException {
		int parameterPos = 1;
		for (Map.Entry<String, PropertyInterface> column : columns.entrySet()) {
			PropertyInterface property = column.getValue();
			Object value = property.getValue(object);
			if (value != null) {
				if (value instanceof Code) {
					value = findId((Code) value);
				} else if (ViewUtil.isView(property)) {
					value = IdUtils.getId(value);
				} else if (DbPersistenceHelper.isReference(property)) {
					value = getReferenceValue(value, copyReferencesOnchange);
				} 
			}
			helper.setParameter(statement, parameterPos++, value, property);
			if (doubleValues) helper.setParameter(statement, parameterPos++, value, property);
		}
		statement.setObject(parameterPos++, id);
		if (doubleValues) statement.setObject(parameterPos++, id);
		return parameterPos;
	}
	
	private Object findId(Code code) {
		Object id = IdUtils.getId(code);
		if (id != null) {
			return id;
		}
		List<?> codes = dbPersistence.getTable(code.getClass()).read(Criteria.all(), 1000);
		for (Object c : codes) {
			if (code.equals(c)) {
				return IdUtils.getId(c);
			}
		}
		return null;
	}
			
	protected abstract String insertQuery();

	protected abstract String selectByIdQuery();

	protected String clearQuery() {
		StringBuilder query = new StringBuilder();
		query.append("DELETE FROM "); query.append(getTableName()); 
		return query.toString();
	}
	
	//

	public void createIndex(Object key) {
		PropertyInterface property = Keys.getProperty(key);
		String fieldPath = property.getFieldPath();
		createIndex(property, fieldPath);
	}
	
	public void createIndex(PropertyInterface property, String fieldPath) {
		Map.Entry<String, PropertyInterface> entry = findX(fieldPath);
		if (indexes.contains(entry.getKey())) {
			return;
		}
		
		String myFieldPath = entry.getValue().getFieldPath();
		if (fieldPath.length() > myFieldPath.length()) {
			String rest = fieldPath.substring(myFieldPath.length() + 1);
			AbstractTable<?> innerTable = dbPersistence.table(entry.getValue().getFieldClazz());
			innerTable.createIndex(property, rest);
		}
		indexes.add(entry.getKey());
	}
	
	//
	
	protected Entry<String, PropertyInterface> findX(String fieldPath) {
		while (true) {
			for (Map.Entry<String, PropertyInterface> entry : columns.entrySet()) {
				String columnFieldPath = entry.getValue().getFieldPath();
				if (columnFieldPath.equals(fieldPath)) {
					return entry;
				}
			}
			int index = fieldPath.lastIndexOf('.');
			if (index < 0) throw new IllegalArgumentException();
			fieldPath = fieldPath.substring(0, index);
		}
	}

}
