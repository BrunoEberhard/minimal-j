package ch.openech.mj.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.derby.client.am.Types;

import ch.openech.mj.db.model.AccessorInterface;
import ch.openech.mj.db.model.ColumnAccess;
import ch.openech.mj.db.model.ListColumnAccess;
import ch.openech.mj.edit.value.Reference;
import ch.openech.mj.edit.value.Required;
import ch.openech.mj.util.FieldUtils;
import ch.openech.mj.util.GenericUtils;
import ch.openech.mj.util.StringUtils;

/**
 * Typen von Tabellen:
 * 
 * <OL>
 * <LI>(Historisierte) In der Persistence vermerkte und damit bearbeitbare Tabellen
 * <LI>Von diesen Tabellen abhängige Tabellen. Können nicht direkt verändert werden.
 * <LI>Unveränderbare Tabellen, die von verschiedenen Top-Level Tabellen gleichzeitig
 * genutzt werden. Konstanten-Werte
 * </OL>
 * 
 */
public abstract class AbstractTable<T> {
	public static final Logger logger = Logger.getLogger(AbstractTable.class.getName());
	
	protected final DbPersistence dbPersistence;
	protected final Class<T> clazz;
	protected final List<String> columnNames;
	
	protected final String name;
	protected Map<String, AbstractTable<?>> subTables = new HashMap<String, AbstractTable<?>>();
	
	protected PreparedStatement selectByIdStatement;
	protected PreparedStatement insertStatement;
	protected PreparedStatement selectMaxIdStatement;

	public AbstractTable(DbPersistence dbPersistence, String prefix, Class<T> clazz) {
		this.dbPersistence = dbPersistence;
		this.name = prefix;
		this.clazz = clazz;
		this.columnNames = findColumnNames(clazz);
	}

	private static List<String> findColumnNames(Class<?> clazz) {
		Map<String, AccessorInterface> accessorsForClass = ColumnAccess.getAccessors(clazz);
		List<String> columnNames = new ArrayList<String>();
		for (Map.Entry<String, AccessorInterface> entry : accessorsForClass.entrySet()) {
			if (entry.getKey().equals("id")) continue;
			if (!FieldUtils.isList(entry.getValue().getClazz())) {
				columnNames.add(entry.getKey());
			}
		}
		return columnNames;
	}	
	
	public List<String> getColumnNames() {
		return columnNames;
	}
	
	public void initialize() throws SQLException {
		findSubTables();
		create();
		prepareStatements();
		
		for (AbstractTable<?> table : subTables.values()) {
			table.initialize();
		}
	}
	
	public int getMaxId() throws SQLException {
		ResultSet resultSet = selectMaxIdStatement.executeQuery();
		try {
			if (resultSet.next()) {
				return resultSet.getInt(1);
			} else {
				return 0;
			}
		} finally {
			resultSet.close();
		}
	}
	
	private void create() throws SQLException {
		DbCreator creator = new DbCreator(dbPersistence);
		creator.createDb(this);
	}
	
	protected String getTableName() {
		if (name != null) {
			return name;
		} else {
			return clazz.getSimpleName();
		}
	}
	
	public Class<T> getClazz() {
		return clazz;
	}
			
	public void findSubTables() throws SQLException {
		Map<String, AccessorInterface> accessors = ListColumnAccess.getAccessors(clazz);
		for (AccessorInterface accessor : accessors.values()) {
			Class<?> clazz = GenericUtils.getGenericClass(accessor.getType());
			subTables.put(accessor.getName(), new SubTable(dbPersistence, buildSubTableName(accessor), clazz));
		}
	}
	
	public void collectImmutables() throws SQLException {
		Map<String, AccessorInterface> accessors = ColumnAccess.getAccessors(clazz);
		for (AccessorInterface accessor : accessors.values()) {
			if ("id".equals(accessor.getName())) continue;
			if (isReference(accessor)) {
				AbstractTable<?> refTable = dbPersistence.getTable(accessor.getClazz());
				if (refTable == null) {
					if (accessor.getClazz().equals(List.class)) {
						throw new IllegalArgumentException("Table: " + getTableName());
					}
					refTable = dbPersistence.addImmutableTable(accessor.getClazz());
					refTable.collectImmutables();
				}
			}
		}
	}

	private String buildSubTableName(AccessorInterface accessor) {
		StringBuilder b = new StringBuilder();
		b.append(getTableName());
		String fieldName = StringUtils.upperFirstChar(accessor.getName());
		b.append('_'); b.append(fieldName); 
		return b.toString();
	}
	
	protected void prepareStatements() throws SQLException {
		insertStatement = prepareInsert();
		selectByIdStatement = prepareSelectById();
		selectMaxIdStatement = prepareSelectMaxId();
	}
	
	public void closeStatements() throws SQLException {
		selectByIdStatement.close();
		insertStatement.close();
		selectMaxIdStatement.close();
		
		for (AbstractTable<?> table : subTables.values()) {
			table.closeStatements();
		}
	}
	
	protected Connection getConnection() {
		return dbPersistence.getConnection();
	}

	// execution helpers
	
	protected int executeInsertWithAutoIncrement(PreparedStatement statement, T object) throws SQLException {
		setParameters(statement, object, false, true);

		logger.fine("Insert (autoIncrement) into " + clazz.getSimpleName());
		statement.execute();
		ResultSet autoIncrementResultSet = statement.getGeneratedKeys();
		autoIncrementResultSet.next();
		Integer id = autoIncrementResultSet.getInt(1);
		logger.finer("AutoIncrement is " + id);
		autoIncrementResultSet.close();
		return id;
	}
	
	protected void executeInsert(PreparedStatement statement, T object) throws SQLException {
		setParameters(statement, object);

		logger.fine("Insert into " + clazz.getSimpleName());
		statement.execute();
	}

	protected T executeSelect(PreparedStatement preparedStatement) throws SQLException {
		return executeSelect(preparedStatement, null);
	}
	
	protected T executeSelect(PreparedStatement preparedStatement, Integer time) throws SQLException {
		T result;
		
		ResultSet resultSet = preparedStatement.executeQuery();
		if (resultSet.next()) {
			result = readResultSetRow(resultSet, time);
		} else {
			result = null;
		}
		
		resultSet.close();
		return result;
	}

	protected List<T> executeSelectAll(PreparedStatement preparedStatement) throws SQLException {
		List<T> result = new ArrayList<T>();
		
		ResultSet resultSet = preparedStatement.executeQuery();
		while (resultSet.next()) {
			result.add(readResultSetRow(resultSet, null));
		}
		
		resultSet.close();
		return result;
	}
	
	private T readResultSetRow(ResultSet resultSet, Integer time) throws SQLException {
		T result;
		try {
			result = clazz.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		for (int columnIndex = 1; columnIndex <= resultSet.getMetaData().getColumnCount(); columnIndex++) {
			Object value = resultSet.getObject(columnIndex);
			String columnName = resultSet.getMetaData().getColumnName(columnIndex);
			AccessorInterface accessor = ColumnAccess.getAccessorIgnoreCase(clazz, columnName);
			if (accessor == null) continue;
			if (columnName.equalsIgnoreCase("id")) {
				accessor.setValue(result, value);
				continue;
			}
			
			Class<?> fieldClass = accessor.getClazz();
			if (isReference(accessor)) {
				int id = value != null ? ((Integer) value).intValue() : 0;
				value = dereference(fieldClass, id, time);
			} else {
				value = convertToFieldClass(fieldClass, value);
			}
			accessor.setValue(result, value);
		}
		return result;
	}
	
	public boolean isReference(AccessorInterface accessor) {
		if (accessor.getClazz().getName().startsWith("java.lang")) return false;
		if (accessor.getAnnotation(Reference.class) != null) return true;
		return !accessor.isFinal();
	}
	
	public boolean isRequired(AccessorInterface accessor) {
		return accessor.getAnnotation(Required.class) != null;
	}
	
	protected <D> Object dereference(Class<D> clazz, int id, Integer time) throws SQLException{
		AbstractTable<D> table = dbPersistence.getTable(clazz);
		if (table instanceof ImmutableTable) {
			return ((ImmutableTable<?>) table).selectById(id);
		} else if (table instanceof Table) {
			return ((Table<?>) table).read(id, time);
		} else {
			throw new IllegalArgumentException("Clazz: " + clazz);
		}
	}

	/**
	 * Sucht (oder kreiert) eine Reference auf den Wert value.
	 * Achtung: Diese Methode soll nicht verwendet werden, um Sub-Werte aufzulösen
	 * 
	 * @param value Der Wert zu dem die Referenz ermittelt werden soll.
	 * @param insertIfNotExisting true => Falls der Wert noch nicht existiert wird er erstellt
	 * @return <code>if value not found and parameter insert is false
	 * @throws SQLException
	 */
	protected <D> Integer lookupReference(D value, boolean insertIfNotExisting) throws SQLException{
		@SuppressWarnings("unchecked")
		Class<D> clazz = (Class<D>) value.getClass();
		logger.fine("Clazz: " + clazz.getSimpleName() + " Value: " + value);
		AbstractTable<D> abstractTable = dbPersistence.getTable(clazz);
		if (abstractTable == null) {
			throw new IllegalArgumentException(clazz.getName());
		}
		if (abstractTable instanceof ImmutableTable) {
			return ((ImmutableTable<D>) abstractTable).getOrCreateId(value);
		} else {
			Table<D> table = (Table<D>) abstractTable;
			Integer id = table.getId(value);
			if (id != null) {
				// eben das sollte nur gemacht werden, wenn ein update nötig ist
				table.update(value);
				return id;
			} else {
				if (insertIfNotExisting) {
					id = table.insert(value);
					return id;
				} else {
					return null;
				}
			}
		}
	}
	
	// TODO configuration of conversation to DB
	protected Object convertToFieldClass(Class<?> fieldClass, Object value) {
		if (value instanceof Integer) {
			if (fieldClass.equals(String.class)) {
				return value.toString();
			}
		}
		return value;
	}

	
	protected int setParameters(PreparedStatement statement, T object) throws SQLException {
		return setParameters(statement, object, false, false);
	}

	protected int setParameters(PreparedStatement statement, T object, boolean doubleValues, boolean insert) throws SQLException {
		logger.fine("Set Parameters: " + object + " DoubleValues: " + doubleValues);
		StringBuilder loggerStringBuilder = null;
		if (logger.isLoggable(Level.FINER)) {
			loggerStringBuilder = new StringBuilder();
		}
		int parameterPos = 1;
		for (String key : columnNames) {
			AccessorInterface accessor = ColumnAccess.getAccessors(clazz).get(key);
			
			Object value = accessor.getValue(object);
			if (value != null) {
				if (isReference(accessor)) {
					try {
						value = lookupReference(value, insert);
					} catch (IllegalArgumentException e) {
						System.out.println(object.getClass());
						System.out.println(accessor.getName());
						throw e;
					}
				} 
				
				if (loggerStringBuilder != null) {
					loggerStringBuilder.append(accessor.getName());
					loggerStringBuilder.append('=');
					loggerStringBuilder.append(value);
					loggerStringBuilder.append(' ');
				}
			}
			setParameter(statement, parameterPos++, value, accessor.getClazz());
			if (doubleValues) setParameter(statement, parameterPos++, value, accessor.getClazz());
		}

		if (loggerStringBuilder != null) {
			logger.finer(loggerStringBuilder.toString());
		}
		return parameterPos;
	}
			
	protected static void setParameter(PreparedStatement preparedStatement, int param, Object value, Class<?> clazz) throws SQLException {
		if (value != null) {
			if (clazz.equals(Integer.class) && value instanceof String) {
				value = Integer.parseInt((String) value); 
			}
			preparedStatement.setObject(param, value);
		} else {
			if (clazz.equals(Integer.class)) {
				preparedStatement.setNull(param, Types.INTEGER);
			} else {
				preparedStatement.setNull(param, Types.VARCHAR);
			}
		}
	}
	
	protected static void setParameterInt(PreparedStatement preparedStatement, int param, int value) throws SQLException {
		preparedStatement.setInt(param, value);
	}
	
	protected abstract PreparedStatement prepareInsert() throws SQLException;

	protected abstract PreparedStatement prepareSelectById() throws SQLException;

	protected PreparedStatement prepareSelectMaxId() throws SQLException {
		StringBuilder query = new StringBuilder();
		query.append("SELECT MAX(id) FROM "); query.append(getTableName()); 
		return getConnection().prepareStatement(query.toString());
	}
	
	protected PreparedStatement prepareSelectId() throws SQLException {
		StringBuilder where = new StringBuilder();
	
		boolean first = true;	
		
		for (String key : ColumnAccess.getNonListKeys(getClazz())) {

			if (!first) where.append(" AND "); else first = false;
			
			// where.append(column.getName()); where.append(" = ?");
			// doesnt work for null so pattern is:
			// ((? IS NULL AND col1 IS NULL) OR col1 = ?)
			where.append("((? IS NULL AND "); where.append(key); where.append(" IS NULL) OR ");
			where.append(key); where.append(" = ?)");
		}
		
		if (this instanceof Table) {
			where.append(" AND ((? IS NULL) OR event <= ?)");
			where.append(" AND (endEvent IS NULL OR (endEvent IS NOT NULL AND (? IS NULL OR ? < endEvent)))");
		}
		
		StringBuilder query = new StringBuilder();
		query.append("SELECT id FROM "); query.append(getTableName()); query.append(" WHERE ");
		query.append(where);
		
		String queryString = query.toString();
		return getConnection().prepareStatement(queryString);
	}

}
