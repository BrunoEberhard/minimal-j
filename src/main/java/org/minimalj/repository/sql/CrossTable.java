package org.minimalj.repository.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.minimalj.model.ViewUtils;
import org.minimalj.model.properties.FlatProperties;
import org.minimalj.model.properties.Property;
import org.minimalj.repository.list.RelationList;
import org.minimalj.util.IdUtils;
import org.minimalj.util.LoggingRuntimeException;

/**
 * Minimal-J internal
 * 
 * note: the id column in this table is the parentId. The id of the elements is called elementId.
 */
public class CrossTable<PARENT, ELEMENT> extends SubTable<PARENT, ELEMENT> implements ListTable<PARENT, ELEMENT> {

	public CrossTable(SqlRepository sqlRepository, String name, Class<ELEMENT> clazz, Property idProperty) {
		super(sqlRepository, name, clazz, idProperty);
	}
	
	@Override
	protected void createConstraints(SqlDialect dialect) {
		super.createIdConstraint(dialect);
		Class<?> referencedClass = ViewUtils.resolve(getClazz());
		AbstractTable<?> referencedTable = sqlRepository.getAbstractTable(referencedClass);
		createConstraint(dialect, "elementId", referencedTable);
	}

	@Override
	protected LinkedHashMap<String, Property> getColumns() {
		return new LinkedHashMap<>();
	}
	
	@Override
	protected void findIndexes() {
		// no one is pointing to a CrossTable
	}
	
	@Override
	protected void createColumnComments() {
		// the columns don't exist in the CrossTable
	}
	
	@Override
	public void addList(PARENT parent, List<ELEMENT> objects) {
		Object parentId = IdUtils.getId(parent);
		try (PreparedStatement insertStatement = createStatement(sqlRepository.getConnection(), insertQuery, false)) {
			for (int position = 0; position<objects.size(); position++) {
				ELEMENT element = objects.get(position);
				Object elementId = getOrCreateId(element);
				insertStatement.setObject(1, elementId);
				insertStatement.setObject(2, parentId);
				insertStatement.setInt(3, position);
				insertStatement.execute();
			}
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, sqlLogger, "addList failed");
		}
	}
	
	@Override
	protected void update(Object parentId, int position, Object element) throws SQLException {
		try (PreparedStatement updateStatement = createStatement(sqlRepository.getConnection(), updateQuery, false)) {
			Object elementId = getOrCreateId(element);
			updateStatement.setObject(1, elementId);
			updateStatement.setObject(2, parentId);
			updateStatement.setInt(3, position);
			updateStatement.execute();
		}
	}
	
	@Override
	protected void insert(Object parentId, int position, Object object) throws SQLException {
		try (PreparedStatement insertStatement = createStatement(sqlRepository.getConnection(), insertQuery, false)) {
			Object element = IdUtils.getId(object);
			insertStatement.setObject(1, element);
			insertStatement.setObject(2, parentId);
			insertStatement.setInt(3, position);
			insertStatement.execute();
		}
	}
	
	@Override
	protected void delete(Object parentId, int position) throws SQLException {
		try (PreparedStatement deleteStatement = createStatement(sqlRepository.getConnection(), deleteQuery, false)) {
			deleteStatement.setObject(1, parentId);
			deleteStatement.setInt(2, position);
			deleteStatement.execute();
		}
	}

	@Override
	public List<ELEMENT> getList(PARENT parent, Map<Class<?>, Map<Object, Object>> loadedReferences) {
		return new RelationList<>(sqlRepository, getClazz(), parent, name);
	}
	
	public List<ELEMENT> readAll(Object parentId) {
		try (PreparedStatement statement = createStatement(sqlRepository.getConnection(), selectByIdQuery, false)) {
			statement.setObject(1, parentId);
			List<ELEMENT> result = new ArrayList<>();
			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					Object id = resultSet.getObject(1);
					ELEMENT element = sqlRepository.getTable(getClazz()).read(id);
					result.add(element);
				}
			}
			return result;
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, sqlLogger, "readAll failed");
		}
	}
	
	// Queries
	
	@Override
	protected String selectByIdQuery() {
		return "SELECT elementId FROM " + getTableName() + " WHERE id = ? ORDER BY position";
	}

	@Override
	protected String insertQuery() {
		return "INSERT INTO " + getTableName() + " (elementId, id, position) VALUES (?, ?, ?)";
	}

	@Override
	protected String updateQuery() {
		return "UPDATE " + getTableName() + " SET " + "elementId = ? WHERE id = ? AND position = ?";
	}

	@Override
	protected void addFieldColumns(SqlDialect dialect, StringBuilder s) {
		s.append(",\n elementId ");
		Property elementIdProperty = FlatProperties.getProperty(clazz, "id");
		dialect.addColumnDefinition(s, elementIdProperty);
		s.append(" NOT NULL");
	}

}
