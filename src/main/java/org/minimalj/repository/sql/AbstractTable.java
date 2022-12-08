package org.minimalj.repository.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.minimalj.model.Code;
import org.minimalj.model.Dependable;
import org.minimalj.model.Keys;
import org.minimalj.model.ViewUtils;
import org.minimalj.model.annotation.Comment;
import org.minimalj.model.annotation.NotEmpty;
import org.minimalj.model.annotation.TechnicalField;
import org.minimalj.model.annotation.TechnicalField.TechnicalFieldType;
import org.minimalj.model.properties.FlatProperties;
import org.minimalj.model.properties.Property;
import org.minimalj.repository.sql.SqlDialect.PostgresqlDialect;
import org.minimalj.security.Subject;
import org.minimalj.util.EqualsHelper;
import org.minimalj.util.IdUtils;
import org.minimalj.util.LoggingRuntimeException;

/**
 * Minimal-J internal<p>
 *
 * Base class of all table representing classes in this persistence layer.
 * Normally you should not need to extend from this class directly. Use
 * the existing subclasses or only the methods in SqlRepository.
 * 
 */
public abstract class AbstractTable<T> {
	public static final Logger sqlLogger = Logger.getLogger("SQL");
	
	protected final SqlRepository sqlRepository;
	protected final Class<T> clazz;
	protected final LinkedHashMap<String, Property> columns;
	
	protected final String name;

	protected final List<String> indexes = new ArrayList<>();
	protected final List<String> constraints = new ArrayList<>();

	protected final String selectByIdQuery;
	protected final String insertQuery;
	protected final String updateQuery;
	protected final String deleteQuery;
	protected final String clearQuery;
	
	protected AbstractTable(SqlRepository sqlRepository, String name, Class<T> clazz) {
		this.sqlRepository = sqlRepository;
		this.name = buildTableName(sqlRepository, name, clazz);
		this.clazz = clazz;
		this.columns = sqlRepository.findColumns(clazz);
		
		sqlRepository.getTableByName().put(this.name, this);
		
		this.selectByIdQuery = selectByIdQuery();
		this.insertQuery = insertQuery();
		this.updateQuery = updateQuery();
		this.deleteQuery = deleteQuery();
		this.clearQuery = clearQuery();
		
		findCodes();
		findDependables();
		findIndexes();
	}
	
	protected String buildTableName(SqlRepository repository, String name, Class<T> clazz) {
		if (name == null) {
			name = clazz.getSimpleName();
		}
		
		name = repository.sqlIdentifier.table(name, repository.getTableByName().keySet());

		// the repository adds the table name too late. For subtables it's important to add the table name here.
		repository.getTableByName().put(name, this);
		return name;
	}
	
	public boolean isHistorized() {
		return false;
	}
	
	protected LinkedHashMap<String, Property> getColumns() {
		return columns;
	}

	protected Collection<String> getIndexes() {
		return indexes;
	}
	
	public static PreparedStatement createStatement(Connection connection, String query, boolean returnGeneratedKeys) throws SQLException {
		int autoGeneratedKeys = returnGeneratedKeys ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS;
		if (sqlLogger.isLoggable(Level.FINE)) {
			return new LoggingPreparedStatement(connection, query, autoGeneratedKeys, sqlLogger);
		} else {
			return connection.prepareStatement(query, autoGeneratedKeys);
		}
	}
	
	protected void execute(String s) {
		try (PreparedStatement statement = createStatement(sqlRepository.getConnection(), s, false)) {
			statement.execute();
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, sqlLogger, "Statement failed: \n" + s);
		}
	}

	protected void createTable(SqlDialect dialect) {
		createEnums(dialect);
		
		StringBuilder s = new StringBuilder();
		dialect.addCreateStatementBegin(s, getTableName());
		addSpecialColumns(dialect, s);
		addFieldColumns(dialect, s);
		addPrimaryKey(dialect, s);
		dialect.addCreateStatementEnd(s);
		execute(s.toString());
		
		createTableComment();
		createColumnComments();
	}

	protected void createTableComment() {
		Comment commentAnnotation = clazz.getAnnotation(Comment.class);
		if (commentAnnotation != null) {
			if (commentAnnotation != null) {
				String comment = commentAnnotation.value();
				if (sqlRepository.getSqlDialect() instanceof PostgresqlDialect) {
					// Postgresql cannot handle the parameter. Don't know why.
					comment = comment.replace("'", "''");
					sqlRepository.execute("COMMENT ON TABLE " + getTableName() + " IS '" + comment + "'");
				} else {
					sqlRepository.execute("COMMENT ON TABLE " + getTableName() + " IS ?", comment);
				}
			}
		}
	}

	protected void createColumnComments() {
		for (Map.Entry<String, Property> entry : columns.entrySet()) {
			Comment commentAnnotation = entry.getValue().getAnnotation(Comment.class);
			if (commentAnnotation != null) {
				String comment = commentAnnotation.value();
				if (sqlRepository.getSqlDialect() instanceof PostgresqlDialect) {
					// Postgresql cannot handle the parameter. Don't know why.
					comment = comment.replace("'", "''");
					sqlRepository.execute("COMMENT ON COLUMN " + getTableName() +"." + entry.getKey() + " IS '" + comment + "'");
				} else {
					sqlRepository.execute("COMMENT ON COLUMN " + getTableName() +"." + entry.getKey() + " IS ?", comment);
				}
			}
		}
	}

	protected void dropTable(SqlDialect dialect) {
		StringBuilder s = new StringBuilder();
		s.append("DROP TABLE ").append(getTableName());

		execute(s.toString());
	}

	protected void dropConstraints(SqlDialect dialect) {
		for (String constraint : constraints) {
			execute("ALTER TABLE " + getTableName() + " DROP CONSTRAINT " + constraint);
		}
	}

	protected void createEnums(SqlDialect dialect) {
		for (Map.Entry<String, Property> column : getColumns().entrySet()) {
			Property property = column.getValue();
			Class<?> propertyClass = property.getClazz();
			if (propertyClass.isEnum()) {
				if (!sqlRepository.dbTypes.containsKey(propertyClass)) {
					StringBuilder s = new StringBuilder();
					String identifier = sqlRepository.sqlIdentifier.identifier(propertyClass.getSimpleName(), sqlRepository.dbTypes.values());
					String query = dialect.createType(propertyClass, identifier);
					if (query != null) {
						execute(query);
						sqlRepository.dbTypes.put(propertyClass, identifier);
					}
				}
			}
		}
	}
	
	protected abstract void addSpecialColumns(SqlDialect dialect, StringBuilder s);
	
	protected void addFieldColumns(SqlDialect dialect, StringBuilder s) {
		for (Map.Entry<String, Property> column : getColumns().entrySet()) {
			s.append(",\n ").append(column.getKey()).append(' '); 

			Property property = column.getValue();
			if (sqlRepository.dbTypes.containsKey(property.getClazz())) {
				s.append(sqlRepository.dbTypes.get(property.getClazz()));
			} else {
				dialect.addColumnDefinition(s, property);
			}
			boolean isNotEmpty = property.getAnnotation(NotEmpty.class) != null;
			s.append(isNotEmpty ? " NOT NULL" : " DEFAULT NULL");
		}
	}

	protected void addPrimaryKey(SqlDialect dialect, StringBuilder s) {
		dialect.addPrimaryKey(s, "ID");
	}
	
	protected void createIndexes(SqlDialect dialect) {
		for (String indexedColumn : indexes) {
			String indexName = sqlRepository.sqlIdentifier.index(getTableName(), indexedColumn);
			String s = sqlRepository.sqlDialect.createIndex(indexName, getTableName(), indexedColumn, isHistorized());
			execute(s);
		}
	}
	
	protected void createConstraints(SqlDialect dialect) {
		for (Map.Entry<String, Property> column : getColumns().entrySet()) {
			Property property = column.getValue();
			
			Class<?> fieldClass = property.getClazz();
			// TODO Contained könnte noch andere Felder enthalten
			if (IdUtils.hasId(fieldClass) && !Dependable.class.isAssignableFrom(clazz)) {
				fieldClass = ViewUtils.resolve(fieldClass);
				AbstractTable<?> referencedTable = sqlRepository.getAbstractTable(fieldClass);
				if (referencedTable.isHistorized()) {
					continue;
				}

				createConstraint(dialect, column.getKey(), referencedTable);
			}
		}
	}

	protected final void createConstraint(SqlDialect dialect, String column, AbstractTable<?> referencedTable) {
		String constraintName = sqlRepository.sqlIdentifier.constraint(getTableName(), column, referencedTable.getTableName());
		String constraint = dialect.createConstraint(constraintName, getTableName(), column, referencedTable.getTableName());
		if (constraint != null) {
			constraints.add(constraintName);
			execute(constraint);
		}
	}

	public void clear() {
		try (PreparedStatement statement = createStatement(sqlRepository.getConnection(), clearQuery, false)) {
			statement.execute();
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, sqlLogger, "Clear of Table " + getTableName() + " failed");
		}
	}

	protected String findColumn(String fieldPath) {
		for (Map.Entry<String, Property> entry : columns.entrySet()) {
			if (entry.getValue().getPath().equals(fieldPath)) {
				return entry.getKey();
			}
		}
		return null;
	}

	public String column(Property property) {
		return findColumn(property.getPath());
	}
	
	public String column(Object key) {
		return column(Keys.getProperty(key));
	}

	public String getTableName() {
		return name;
	}
	
	public Class<T> getClazz() {
		return clazz;
	}
	
	private void findCodes() {
		for (Map.Entry<String, Property> column : getColumns().entrySet()) {
			Property property = column.getValue();
			Class<?> fieldClazz = property.getClazz();
			if (Code.class.isAssignableFrom(fieldClazz) && fieldClazz != clazz) {
				sqlRepository.addClass(ViewUtils.resolve(fieldClazz));
			}
		}
	}
	
	private void findDependables() {
		for (Property property : FlatProperties.getListProperties(getClazz())) {
			Class<?> listType = property.getGenericClass();
			if (listType == null) {
				throw new IllegalArgumentException(getClazz().getSimpleName() + "." + property.getPath() + " has no type");
			}
			if (IdUtils.hasId(listType)) {
				sqlRepository.addClass(ViewUtils.resolve(listType));
			}
		}
	}

	protected void findIndexes() {
		for (Map.Entry<String, Property> column : columns.entrySet()) {
			Property property = column.getValue();
			if (IdUtils.hasId(property.getClazz())) {
				addIndex(property, property.getPath());
			}
		}
	}

	// execution helpers

	protected long executeSelectCount(PreparedStatement preparedStatement) {
		try (ResultSet resultSet = preparedStatement.executeQuery()) {
			resultSet.next();
			return resultSet.getLong(1);
		} catch (SQLException x) {
			throw new RuntimeException(x.getMessage());
		}
	}
	
	protected T executeSelect(PreparedStatement preparedStatement) {
		try (ResultSet resultSet = preparedStatement.executeQuery()) {
			if (resultSet.next()) {
				return sqlRepository.readResultSetRow(clazz,  resultSet);
			} else {
				return null;
			}
		} catch (SQLException x) {
			throw new RuntimeException(x);
		}
	}
	
	protected T executeSelect(PreparedStatement preparedStatement, Map<Class<?>, Map<Object, Object>> loadedReferences) {
		try (ResultSet resultSet = preparedStatement.executeQuery()) {
			if (resultSet.next()) {
				return sqlRepository.readResultSetRow(clazz,  resultSet, loadedReferences);
			} else {
				return null;
			}
		} catch (SQLException x) {
			throw new RuntimeException(x.getMessage());
		}
	}

	protected List<T> executeSelectAll(PreparedStatement preparedStatement) {
		return executeSelectAll(preparedStatement, new HashMap<>());
	}
	
	protected List<T> executeSelectAll(PreparedStatement preparedStatement, Map<Class<?>, Map<Object, Object>> loadedReferences) {
		List<T> result = new ArrayList<>();
		try (ResultSet resultSet = preparedStatement.executeQuery()) {
			while (resultSet.next()) {
				T object = sqlRepository.readResultSetRow(clazz,  resultSet, loadedReferences);
				if (this instanceof Table) {
					((Table<T>) this).loadLists(object, loadedReferences);
				}
				result.add(object);
			}
		} catch (SQLException x) {
			throw new RuntimeException(getTableName() + " / " + x.getMessage());
		}
		return result;
	}

	protected enum ParameterMode {
		INSERT, UPDATE, HISTORIZE, INSERT_AUTO_INCREMENT;
	}
	
	protected int setParameters(PreparedStatement statement, T object, ParameterMode mode, Object id) throws SQLException {
		int parameterPos = 1;
		boolean insert = mode == ParameterMode.INSERT || mode == ParameterMode.INSERT_AUTO_INCREMENT;
		for (Map.Entry<String, Property> column : columns.entrySet()) {
			Property property = column.getValue();
			Object value = property.getValue(object);
			TechnicalField technicalField = property.getAnnotation(TechnicalField.class);
			if (technicalField != null) {
				TechnicalFieldType type = technicalField.value();
				if (type == TechnicalFieldType.EDIT_DATE || type == TechnicalFieldType.CREATE_DATE && insert) {
					value = LocalDateTime.now();
				} else if (type == TechnicalFieldType.EDIT_USER || type == TechnicalFieldType.CREATE_USER && insert) {
					Subject subject = Subject.getCurrent();
					if (subject != null) {
						if (property.getClazz() == String.class) {
							value = subject.getName();
						} else {
							value = subject.getId();
						}
					}
				}
			} 
			if (value instanceof Code) {
				value = IdUtils.getId(value);
			} else if (value != null && IdUtils.hasId(value.getClass())) {
				Object referencedId = IdUtils.getId(value);
				if (referencedId != null) {
					value = referencedId;
				} else {
					Table referencedTable  = sqlRepository.getTable(property.getClazz());
					value = referencedTable.insert(value);
				}
			}
			if (value != null) {
				sqlRepository.getSqlDialect().setParameter(statement, parameterPos++, value);
			} else {
				sqlRepository.getSqlDialect().setParameterNull(statement, parameterPos++, property.getClazz());
			}
		}
		if (mode != ParameterMode.INSERT_AUTO_INCREMENT) {
			statement.setObject(parameterPos++, id);
		}
		return parameterPos;
	}

	protected Object updateDependable(Table dependableTable, Object dependableId, Object dependableObject, ParameterMode mode) {
		if (dependableId != null) {
			Object objectInDb = dependableTable.read(dependableId);
			if (!EqualsHelper.equals(dependableObject, objectInDb)) {
				if (mode == ParameterMode.HISTORIZE) {
					IdUtils.setId(dependableObject, null);
					dependableId = dependableTable.insert(dependableObject);
				} else {
					dependableTable.updateWithId(dependableObject, dependableId);
				}
			}
		} else {
			dependableId = dependableTable.insert(dependableObject);
		}
		return dependableId;
	}
	
	// TODO multiple dependables could be get with one (prepared) statement
	private Object getDependableId(Object id, String column) {
		String query = "SELECT " + column + " FROM " + getTableName() + " WHERE ID = ?";
		if (isHistorized()) {
			query += " AND VERSION = 0";
		}
		try (PreparedStatement preparedStatement = createStatement(sqlRepository.getConnection(), query, false)) {
			preparedStatement.setObject(1, id);
			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					return resultSet.getObject(1);
				} else {
					return null;
				}
			}
		} catch (SQLException x) {
			throw new RuntimeException(x.getMessage());
		}
	}

	protected abstract String insertQuery();

	protected abstract String updateQuery();

	protected abstract String deleteQuery();
	
	protected abstract String selectByIdQuery();

	protected String clearQuery() {
		return "DELETE FROM " + getTableName();
	}
	
	protected final Object getOrCreateId(Object object) {
		Object id = IdUtils.getId(object);
		if (id == null) {
			id = sqlRepository.insert(object);
		}
		return id;
	}
	
	//

	private void addIndex(Property property, String fieldPath) {
		Map.Entry<String, Property> entry = findX(fieldPath);
		if (indexes.contains(entry.getKey())) {
			return;
		}
		
		String myFieldPath = entry.getValue().getPath();
		if (fieldPath.length() > myFieldPath.length()) {
			String rest = fieldPath.substring(myFieldPath.length() + 1);
			AbstractTable<?> innerTable = sqlRepository.getAbstractTable(entry.getValue().getClazz());
			innerTable.addIndex(property, rest);
		}
		indexes.add(entry.getKey());
	}
	
	private Entry<String, Property> findX(String fieldPath) {
		while (true) {
			for (Map.Entry<String, Property> entry : columns.entrySet()) {
				String columnFieldPath = entry.getValue().getPath();
				if (columnFieldPath.equals(fieldPath)) {
					return entry;
				}
			}
			int index = fieldPath.lastIndexOf('.');
			if (index < 0) throw new IllegalArgumentException();
			fieldPath = fieldPath.substring(0, index);
		}
	}
	
	//
	
	/**
	 * @param property the property to check
	 * @return true if property isn't a base object like String, Integer, Date, enum but a dependable
	 */
	public static boolean isDependable(Property property) {
		if (!isDependable(property.getClazz())) return false;
		if (property.isFinal()) return false;
		return true;
	}

	public static boolean isDependable(Class<?> clazz) {
		if (clazz.isPrimitive()) return false;
		if (clazz.getName().startsWith("java")) return false;
		if (Enum.class.isAssignableFrom(clazz)) return false;
		if (clazz.isArray()) return false;
		if (IdUtils.hasId(clazz)) return false;
		return true;
	}

}
