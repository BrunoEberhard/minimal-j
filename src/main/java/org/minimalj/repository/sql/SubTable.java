package org.minimalj.repository.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.minimalj.model.properties.Property;
import org.minimalj.util.IdUtils;
import org.minimalj.util.LoggingRuntimeException;

/**
 * Minimal-J internal
 * 
 * - In this tables the parentId is used as id
 * - An additional column named position
 * - has no sub tables
 */
public class SubTable<PARENT, ELEMENT> extends AbstractTable<ELEMENT> implements ListTable<PARENT, ELEMENT> {

	protected final Property parentIdProperty;
	private final boolean hasId;
	
	public SubTable(SqlRepository sqlRepository, String name, Class<ELEMENT> clazz, Property parentIdProperty) {
		super(sqlRepository, name, clazz);
		
		this.parentIdProperty = parentIdProperty;
		this.hasId = IdUtils.hasId(clazz);
	}
	
	@Override
	protected void createConstraints(SqlDialect dialect) {
		super.createConstraints(dialect);
		createIdConstraint(dialect);
	}

	protected void createIdConstraint(SqlDialect dialect) {
		Class<?> parentClass = parentIdProperty.getDeclaringClass();
		Table<?> parentTable = sqlRepository.getTable(parentClass);
		createConstraint(dialect, "ID", parentTable);
	}

	@Override
	public void addList(PARENT parent, List<ELEMENT> objects) {
		try (PreparedStatement insertStatement = createStatement(sqlRepository.getConnection(), insertQuery, false)) {
			for (int position = 0; position<objects.size(); position++) {
				ELEMENT object = objects.get(position);
				int parameterPos = setParameters(insertStatement, object, ParameterMode.INSERT, IdUtils.getId(parent));
				insertStatement.setInt(parameterPos++, position);
				insertStatement.execute();
			}
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, sqlLogger, "addList failed");
		}
	}

	@Override
	public void replaceList(PARENT parent, List<ELEMENT> objects) {
		Object parentId = IdUtils.getId(parent);
		List<ELEMENT> objectsInDb = getList(parent, null);
		int position = 0;
		try {
			while (position < Math.max(objects.size(), objectsInDb.size())) {
				if (position < objects.size()) {
					ELEMENT object = objects.get(position);
					if (hasId && IdUtils.getId(object) == null) {
						// don't call insert on sqlRepository directly because we need the id set on the
						// object.
						sqlRepository.getTable((Class<ELEMENT>) object.getClass()).insert(object);
					}
					if (position < objectsInDb.size()) {
						update(parentId, position, object);
					} else {
						insert(parentId, position, object);
					}
				} else {
					// delete all beginning from this position with one delete statement
					delete(parentId, position);
					break; 
				}
				position++;
			}
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, sqlLogger, "replaceList failed");
		}
	}
	
	protected void update(Object parentId, int position, ELEMENT object) throws SQLException {
		try (PreparedStatement updateStatement = createStatement(sqlRepository.getConnection(), updateQuery, false)) {
			int parameterPos = setParameters(updateStatement, object, ParameterMode.UPDATE, parentId);
			updateStatement.setInt(parameterPos++, position);
			updateStatement.execute();
		}
	}

	protected void insert(Object parentId, int position, ELEMENT object) throws SQLException {
		try (PreparedStatement insertStatement = createStatement(sqlRepository.getConnection(), insertQuery, false)) {
			int parameterPos = setParameters(insertStatement, object, ParameterMode.INSERT, parentId);
			insertStatement.setInt(parameterPos++, position);
			insertStatement.execute();
		}
	}
	
	protected void delete(Object parentId, int position) throws SQLException {
		try (PreparedStatement deleteStatement = createStatement(sqlRepository.getConnection(), deleteQuery, false)) {
			deleteStatement.setObject(1, parentId);
			deleteStatement.setInt(2, position);
			deleteStatement.execute();
		}
	}

	@Override
	public List<ELEMENT> getList(PARENT parent, Map<Class<?>, Map<Object, Object>> loadedReferences) {
		try (PreparedStatement selectByIdStatement = createStatement(sqlRepository.getConnection(), selectByIdQuery, false)) {
			selectByIdStatement.setObject(1, IdUtils.getId(parent));
			return executeSelectAll(selectByIdStatement);
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, sqlLogger, "getList failed");
		}
	}

	// Queries
	
	@Override
	protected String selectByIdQuery() {
		return "SELECT * FROM " + getTableName() + " WHERE id = ? ORDER BY position";
	}
	
	@Override
	protected String insertQuery() {
		StringBuilder s = new StringBuilder();
		
		s.append("INSERT INTO ").append(getTableName()).append(" (");
		for (Object columnNameObject : getColumns().keySet()) {
			// myst, direkt auf columnNames zugreiffen funktionert hier nicht
			String columnName = (String) columnNameObject;
			s.append(columnName).append(", ");
		}
		s.append("id, position) VALUES (");
		for (int i = 0; i<getColumns().size(); i++) {
			s.append("?, ");
		}
		s.append("?, ?)");

		return s.toString();
	}

	@Override
	protected String updateQuery() {
		return updateQuery(false) + " AND position = ?";
	}

	@Override
	protected String deleteQuery() {
		return "DELETE FROM " + getTableName() + " WHERE id = ? AND position >= ?";
	}

	@Override
	protected void addSpecialColumns(SqlDialect dialect, StringBuilder s) {
		s.append(" id ");
		dialect.addColumnDefinition(s, parentIdProperty);
		s.append(",\n position INTEGER NOT NULL");
	}
	
	@Override
	protected void addPrimaryKey(SqlDialect dialect, StringBuilder s) {
		dialect.addPrimaryKey(s, "id, position");
	}

}