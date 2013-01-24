package ch.openech.mj.db;

import java.math.BigDecimal;
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
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import ch.openech.mj.db.model.ColumnProperties;
import ch.openech.mj.db.model.ListColumnAccess;
import ch.openech.mj.model.EnumUtils;
import ch.openech.mj.model.InvalidValues;
import ch.openech.mj.model.PropertyInterface;
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
	protected PreparedStatement clearStatement;

	public AbstractTable(DbPersistence dbPersistence, String prefix, Class<T> clazz) {
		logger.setLevel(Level.FINEST);
		
		this.dbPersistence = dbPersistence;
		this.name = prefix;
		this.clazz = clazz;
		this.columnNames = findColumnNames(clazz);
	}

	private static List<String> findColumnNames(Class<?> clazz) {
		Map<String, PropertyInterface> propertiesForClass = ColumnProperties.getProperties(clazz);
		List<String> columnNames = new ArrayList<String>();
		for (Map.Entry<String, PropertyInterface> entry : propertiesForClass.entrySet()) {
			if (entry.getKey().equals("id")) continue;
			if (!FieldUtils.isList(entry.getValue().getFieldClazz())) {
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
		creator.create(this);
	}
	
	public void clear() throws SQLException {
		clearStatement.execute();
		for (AbstractTable<?> table : subTables.values()) {
			table.clear();
		}
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
		Map<String, PropertyInterface> properties = ListColumnAccess.getProperties(clazz);
		for (PropertyInterface property : properties.values()) {
			Class<?> clazz = GenericUtils.getGenericClass(property.getType());
			subTables.put(property.getFieldName(), new SubTable(dbPersistence, buildSubTableName(property), clazz));
		}
	}
	
	public void collectImmutables() throws SQLException {
		Map<String, PropertyInterface> properties = ColumnProperties.getProperties(clazz);
		for (PropertyInterface property : properties.values()) {
			if ("id".equals(property.getFieldName())) continue;
			if (ColumnProperties.isReference(property)) {
				AbstractTable<?> refTable = dbPersistence.getTable(property.getFieldClazz());
				if (refTable == null) {
					if (property.getFieldClazz().equals(List.class)) {
						throw new IllegalArgumentException("Table: " + getTableName());
					}
					refTable = dbPersistence.addImmutableTable(property.getFieldClazz());
					refTable.collectImmutables();
				}
			}
		}
	}

	private String buildSubTableName(PropertyInterface property) {
		StringBuilder b = new StringBuilder();
		b.append(getTableName());
		String fieldName = StringUtils.upperFirstChar(property.getFieldName());
		b.append('_'); b.append(fieldName); 
		return b.toString();
	}
	
	protected void prepareStatements() throws SQLException {
		insertStatement = prepareInsert();
		selectByIdStatement = prepareSelectById();
		selectMaxIdStatement = prepareSelectMaxId();
		clearStatement = prepareClear();
	}
	
	public void closeStatements() throws SQLException {
		selectByIdStatement.close();
		insertStatement.close();
		selectMaxIdStatement.close();
		clearStatement.close();
		
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
	
	protected T readResultSetRow(ResultSet resultSet, Integer time) throws SQLException {
		T result;
		try {
			result = clazz.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		for (int columnIndex = 1; columnIndex <= resultSet.getMetaData().getColumnCount(); columnIndex++) {
			Object value = resultSet.getObject(columnIndex);
			String columnName = resultSet.getMetaData().getColumnName(columnIndex);
			PropertyInterface property = ColumnProperties.getPropertyIgnoreCase(clazz, columnName);
			if (property == null) continue;
			if (columnName.equalsIgnoreCase("id")) {
				property.setValue(result, value);
				continue;
			}
			
			Class<?> fieldClass = property.getFieldClazz();
			if (ColumnProperties.isReference(property)) {
				int id = value != null ? ((Integer) value).intValue() : 0;
				value = dereference(fieldClass, id, time);
			} else {
				value = convertToFieldClass(fieldClass, value);
			}
			property.setValue(result, value);
		}
		return result;
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
		if (value == null) return null;
		
		if (fieldClass == BigDecimal.class) {
//			if (value instanceof Double) {
//				value = new BigDecimal((Double) value);
//			} else if (value instanceof Long) {
//				value = new BigDecimal((Long) value);
//			} else {
//				throw new IllegalArgumentException(value.getClass().getSimpleName());
//			}
		} else if (fieldClass == LocalDate.class) {
			if (value instanceof java.sql.Date) {
				value = new LocalDate((java.sql.Date) value);
			} else if (value != null) {
				throw new IllegalArgumentException(value.getClass().getSimpleName());
			}
		} else if (fieldClass == LocalTime.class) {
			if (value instanceof java.sql.Time) {
				value = new LocalTime((java.sql.Time) value);
			} else if (value != null) {
				throw new IllegalArgumentException(value.getClass().getSimpleName());
			}
		} else if (fieldClass == LocalDateTime.class) {
			if (value instanceof java.sql.Timestamp) {
				value = new LocalDateTime((java.sql.Timestamp) value);
			} else if (value instanceof java.sql.Date) {
				value = new LocalDateTime((java.sql.Date) value);
			} else if (value != null) {
				throw new IllegalArgumentException(value.getClass().getSimpleName());
			}
		} else if (fieldClass == Boolean.class) {
			if (value instanceof Integer) {
				value = Boolean.valueOf(((int) value) == 1);
			} else if (value != null) {
				throw new IllegalArgumentException(value.getClass().getSimpleName());
			}
		} else if (Enum.class.isAssignableFrom(fieldClass)) {
			value = EnumUtils.valueList((Class<Enum>)fieldClass).get((Integer) value);
		}
		return value;
	}

	
	protected int setParameters(PreparedStatement statement, T object) throws SQLException {
		return setParameters(statement, object, false, false);
	}

	protected int setParameters(PreparedStatement statement, T object, boolean doubleValues, boolean insert) throws SQLException {
		logger.fine("Set Parameters: " + object + " DoubleValues: " + doubleValues);
		final StringBuilder loggerStringBuilder = logger.isLoggable(Level.FINER) ? new StringBuilder() : null;

		int parameterPos = 1;
		for (String key : columnNames) {
			PropertyInterface property = ColumnProperties.getProperties(clazz).get(key);
			
			Object value = property.getValue(object);
			if (value != null) {
				if (ColumnProperties.isReference(property)) {
					try {
						value = lookupReference(value, insert);
					} catch (IllegalArgumentException e) {
						System.out.println(object.getClass());
						System.out.println(property.getFieldName());
						throw e;
					}
				} 
				
				if (loggerStringBuilder != null) {
					loggerStringBuilder.append(property.getFieldName());
					loggerStringBuilder.append('=');
					loggerStringBuilder.append(value);
					loggerStringBuilder.append(' ');
				}
			}
			setParameter(statement, parameterPos++, value, property.getFieldClazz());
			if (doubleValues) setParameter(statement, parameterPos++, value, property.getFieldClazz());
		}

		if (loggerStringBuilder != null) {
			logger.finer(loggerStringBuilder.toString());
		}
		return parameterPos;
	}
			
	protected void setParameter(PreparedStatement preparedStatement, int param, Object value, Class<?> clazz) throws SQLException {
		if (value == null) {
			setParameterNull(preparedStatement, param, clazz);
		} else {
			if (value instanceof Enum<?>) {
				Enum<?> e = (Enum<?>) value;
				if (!InvalidValues.isInvalid(e)) {
					value = e.ordinal();
				} else {
					setParameterNull(preparedStatement, param, clazz);
					return;
				}
			} else if (value instanceof LocalDate) {
				value = new java.sql.Date(((LocalDate) value).toDate().getTime());
			} else if (value instanceof LocalTime) {
				value = new java.sql.Time(((LocalTime) value).toDateTimeToday().getMillis());
			} else if (value instanceof LocalDateTime) {
				if (dbPersistence.isDerbyDb()) {
					value = new java.sql.Timestamp(((LocalDateTime) value).toDate().getTime());
				} else {
					value = new java.sql.Date(((LocalDateTime) value).toDate().getTime());
				}
			}
			preparedStatement.setObject(param, value);
		} 
	}

	protected void setParameterNull(PreparedStatement preparedStatement, int param, Class<?> clazz) throws SQLException {
		if (clazz == String.class) {
			preparedStatement.setNull(param, Types.VARCHAR);
		} else if (clazz == Integer.class) {
			preparedStatement.setNull(param, Types.INTEGER);
		} else if (clazz == Boolean.class) {
			preparedStatement.setNull(param, Types.INTEGER);
		} else if (clazz == BigDecimal.class) {
			preparedStatement.setNull(param, Types.DECIMAL);
		} else if (Enum.class.isAssignableFrom(clazz)) {
			preparedStatement.setNull(param, Types.INTEGER);
		} else if (clazz == LocalDate.class) {
			preparedStatement.setNull(param, Types.DATE);
		} else if (dbPersistence.getTable(clazz) != null) {
			preparedStatement.setNull(param, Types.INTEGER);
		} else {
			throw new IllegalArgumentException(clazz.getSimpleName());
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
	
	protected PreparedStatement prepareClear() throws SQLException {
		StringBuilder query = new StringBuilder();
		query.append("DELETE FROM "); query.append(getTableName()); 
		return getConnection().prepareStatement(query.toString());
	}
	
	protected PreparedStatement prepareSelectId() throws SQLException {
		StringBuilder where = new StringBuilder();
	
		boolean first = true;	
		
		for (String key : ColumnProperties.getNonListKeys(getClazz())) {

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
