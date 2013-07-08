package ch.openech.mj.db;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.derby.client.am.Types;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.ReadablePartial;

import ch.openech.mj.db.model.ColumnProperties;
import ch.openech.mj.edit.value.CloneHelper;
import ch.openech.mj.model.EnumUtils;
import ch.openech.mj.model.InvalidValues;
import ch.openech.mj.model.PropertyInterface;
import ch.openech.mj.util.DateUtils;
import ch.openech.mj.util.FieldUtils;
import ch.openech.mj.util.GenericUtils;

/**
 * Minimal-J internal<p>
 *
 * Base class of all table representing classes in this persistence layer.
 * Normally you should not need to extend from this class directly. Use
 * the existing subclasses or only the methods in DbPersistence.
 * 
 */
public abstract class AbstractTable<T> {
	public static final Logger logger = Logger.getLogger(AbstractTable.class.getName());
	
	protected final DbPersistence dbPersistence;
	protected final Class<T> clazz;
	protected final List<String> columnNames;
	
	protected final String name;
	
	protected PreparedStatement selectByIdStatement;
	protected PreparedStatement insertStatement;
	protected PreparedStatement selectMaxIdStatement;
	protected PreparedStatement clearStatement;

	public AbstractTable(DbPersistence dbPersistence, String prefix, Class<T> clazz) {
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
		initializeImmutables();
		
		create();
		prepareStatements();
	}

	public int getMaxId() {
		try (ResultSet resultSet = selectMaxIdStatement.executeQuery()) {
			if (resultSet.next()) {
				return resultSet.getInt(1);
			} else {
				return 0;
			}
		} catch (SQLException x) {
			logger.log(Level.SEVERE, "Couldn't get max Id of " + getTableName(), x);
			throw new RuntimeException("Couldn't get max Id of " + getTableName());
		}
	}
	
	private void create() throws SQLException {
		DbCreator creator = new DbCreator(dbPersistence);
		creator.create(this);
	}
	
	public void clear() {
		try {
			clearStatement.execute();
		} catch (SQLException x) {
			logger.log(Level.SEVERE, "Clear of Table " + getTableName() + " failed", x);
			throw new RuntimeException("Clear of Table " + getTableName() + " failed");
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
	
	private void initializeImmutables() throws SQLException {
		Map<String, PropertyInterface> properties = ColumnProperties.getProperties(clazz);
		for (PropertyInterface property : properties.values()) {
			if ("id".equals(property.getFieldName())) continue;
			if (ColumnProperties.isReference(property)) {
				AbstractTable<?> refTable = dbPersistence.getTable(property.getFieldClazz());
				if (refTable == null) {
					if (property.getFieldClazz().equals(List.class)) {
						throw new IllegalArgumentException("Table: " + getTableName());
					}
					refTable = dbPersistence.addImmutableClass(property.getFieldClazz());
					refTable.initialize();
				}
			}
		}
	}
	
	protected void prepareStatements() throws SQLException {
		insertStatement = prepareReturnGeneratedKeys(insertQuery());
		selectByIdStatement = prepare(selectByIdQuery());
		selectMaxIdStatement = prepare(selectMaxIdQuery());
		clearStatement = prepare(clearQuery());
	}
	
	protected PreparedStatement prepare(String statement) throws SQLException {
		if (logger.isLoggable(Level.FINE)) {
			return new LoggingPreparedStatement(getConnection(), statement, logger);
		} else {
			return getConnection().prepareStatement(statement);
		}
	}
	
	protected PreparedStatement prepareReturnGeneratedKeys(String statement) throws SQLException {
		if (logger.isLoggable(Level.FINE)) {
			return new LoggingPreparedStatement(getConnection(), statement, Statement.RETURN_GENERATED_KEYS, logger);
		} else {
			return getConnection().prepareStatement(statement, Statement.RETURN_GENERATED_KEYS);
		}
	}

	public void closeStatements() throws SQLException {
		selectByIdStatement.close();
		insertStatement.close();
		selectMaxIdStatement.close();
		clearStatement.close();
	}
	
	protected Connection getConnection() {
		return dbPersistence.getConnection();
	}

	// execution helpers
	
	protected int executeInsertWithAutoIncrement(PreparedStatement statement, T object) throws SQLException {
		setParameters(statement, object, false, true);
		statement.execute();
		try (ResultSet autoIncrementResultSet = statement.getGeneratedKeys()) {
			autoIncrementResultSet.next();
			Integer id = autoIncrementResultSet.getInt(1);
			logger.finer("AutoIncrement is " + id);
			return id;
		}
	}
	
	protected void executeInsert(PreparedStatement statement, T object) throws SQLException {
		setParameters(statement, object);
		statement.execute();
	}

	protected T executeSelect(PreparedStatement preparedStatement) throws SQLException {
		return executeSelect(preparedStatement, null);
	}
	
	protected T executeSelect(PreparedStatement preparedStatement, Integer time) throws SQLException {
		try (ResultSet resultSet = preparedStatement.executeQuery()) {
			if (resultSet.next()) {
				return readResultSetRow(resultSet, time).object;
			} else {
				return null;
			}
		}
	}

	protected List<T> executeSelectAll(PreparedStatement preparedStatement) throws SQLException {
		List<T> result = new ArrayList<T>();
		try (ResultSet resultSet = preparedStatement.executeQuery()) {
			while (resultSet.next()) {
				T object = readResultSetRow(resultSet, null).object;
				result.add(object);
			}
		}
		return result;
	}
	
	/**
	 * Internal helper class. Needed by readResultSetRow. Allows the returning of
	 * both the object and the id.
	 */
	protected static class ObjectWithId<S> {
		public Integer id;
		public S object;
	}
	
	protected ObjectWithId<T> readResultSetRow(ResultSet resultSet, Integer time) throws SQLException {
		ObjectWithId<T> result = new ObjectWithId<>();
		result.object = CloneHelper.newInstance(clazz);
		
		for (int columnIndex = 1; columnIndex <= resultSet.getMetaData().getColumnCount(); columnIndex++) {
			Object value = resultSet.getObject(columnIndex);
			String columnName = resultSet.getMetaData().getColumnName(columnIndex);
			boolean isId = columnName.equalsIgnoreCase("id");
			if (isId) result.id = (Integer) value;
			PropertyInterface property = ColumnProperties.getPropertyIgnoreCase(clazz, columnName);
			if (property == null) continue;
			if (isId) {
				property.setValue(result.object, value);
				continue;
			}
			
			if (value != null) {
				Class<?> fieldClass = property.getFieldClazz();
				if (ColumnProperties.isReference(property)) {
					value = dereference(fieldClass, (Integer) value, time);
				} else if (Set.class == fieldClass) {
					Set set = (Set) property.getValue(result.object);
					Class enumClass = GenericUtils.getGenericClass(property.getType());
					EnumUtils.fillSet((int) value, enumClass, set);
					continue; // skip setValue, it's final
				} else {
					value = convertToFieldClass(fieldClass, value);
				}
				property.setValue(result.object, value);
			}
		}
		return result;
	}
	
	protected <D> Object dereference(Class<D> clazz, int id, Integer time) {
		AbstractTable<D> table = dbPersistence.getTable(clazz);
		if (table instanceof ImmutableTable) {
			return ((ImmutableTable<?>) table).read(id);
		} else if (table instanceof HistorizedTable<?>) {
			return ((HistorizedTable<?>) table).read(id, time);			
		} else if (table instanceof Table) {
			return ((Table<?>) table).read(id);
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
			} else {
				throw new IllegalArgumentException(value.getClass().getSimpleName());
			}
		} else if (fieldClass == LocalTime.class) {
			if (value instanceof java.sql.Time) {
				value = new LocalTime((java.sql.Time) value);
			} else {
				throw new IllegalArgumentException(value.getClass().getSimpleName());
			}
		} else if (fieldClass == LocalDateTime.class) {
			if (value instanceof java.sql.Timestamp) {
				value = new LocalDateTime((java.sql.Timestamp) value);
			} else if (value instanceof java.sql.Date) {
				value = new LocalDateTime((java.sql.Date) value);
			} else {
				throw new IllegalArgumentException(value.getClass().getSimpleName());
			}
		} else if (fieldClass == ReadablePartial.class) {
			if (value instanceof String) {
				String text = ((String) value).trim(); // cut the spaces from CHAR - Column
				value = DateUtils.parsePartial(text);
			} else if (value instanceof java.sql.Date) {
				// this should not happen, but is maybe usefull for migrating a DB
				value = new LocalDate(((java.sql.Date) value).getTime());
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
		int parameterPos = 1;
		for (String key : columnNames) {
			PropertyInterface property = ColumnProperties.getProperties(clazz).get(key);
			
			Object value = property.getValue(object);
			if (value != null) {
				if (ColumnProperties.isReference(property)) {
					try {
						value = lookupReference(value, insert);
					} catch (IllegalArgumentException e) {
						logger.severe(object.getClass().getName() + " / " + property.getFieldName());
						throw e;
					}
				} 
			}
			setParameter(statement, parameterPos++, value, property);
			if (doubleValues) setParameter(statement, parameterPos++, value, property);
		}
		return parameterPos;
	}
			
	protected void setParameter(PreparedStatement preparedStatement, int param, Object value, PropertyInterface property) throws SQLException {
		if (value == null) {
			setParameterNull(preparedStatement, param, property.getFieldClazz());
		} else {
			if (value instanceof Enum<?>) {
				Enum<?> e = (Enum<?>) value;
				if (!InvalidValues.isInvalid(e)) {
					value = e.ordinal();
				} else {
					setParameterNull(preparedStatement, param, property.getFieldClazz());
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
			} else if (value instanceof ReadablePartial) {
				value = DateUtils.formatPartial((ReadablePartial) value);
			} else if (value instanceof Set<?>) {
				Set<?> set = (Set<?>) value;
				Class<?> enumClass = GenericUtils.getGenericClass(property.getType());
				value = EnumUtils.getInt(set, enumClass);
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
		} else if (clazz == LocalTime.class) {
			preparedStatement.setNull(param, Types.TIME);
		} else if (clazz == LocalDateTime.class) {
			preparedStatement.setNull(param, Types.DATE);
		} else if (dbPersistence.getTable(clazz) != null) {
			preparedStatement.setNull(param, Types.INTEGER);
		} else if (clazz == ReadablePartial.class) {
			preparedStatement.setNull(param, Types.CHAR);
		} else {
			throw new IllegalArgumentException(clazz.getSimpleName());
		}
	}
	
	protected static void setParameterInt(PreparedStatement preparedStatement, int param, int value) throws SQLException {
		preparedStatement.setInt(param, value);
	}
	
	protected abstract String insertQuery();

	protected abstract String selectByIdQuery();

	protected String selectMaxIdQuery() {
		StringBuilder query = new StringBuilder();
		query.append("SELECT MAX(id) FROM "); query.append(getTableName()); 
		return query.toString();
	}
	
	protected String clearQuery() {
		StringBuilder query = new StringBuilder();
		query.append("DELETE FROM "); query.append(getTableName()); 
		return query.toString();
	}
	
	protected String selectIdQuery() {
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
		
		return query.toString();
	}

}
