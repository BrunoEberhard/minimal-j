package org.minimalj.backend.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.minimalj.model.ViewUtil;
import org.minimalj.model.properties.Properties;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.IdUtils;

/**
 * Minimal-J internal
 * 
 */
@SuppressWarnings("rawtypes")
public class ViewSubTable extends SubTable {

	public ViewSubTable(SqlPersistence sqlPersistence, String prefix, Class viewClass, PropertyInterface idProperty) {
		super(sqlPersistence, prefix, viewClass, idProperty);
	}
	
	@Override
	protected void createConstraints(SqlSyntax syntax) {
		Class<?> referencedClass = ViewUtil.resolve(getClazz());
		AbstractTable<?> referencedTable = sqlPersistence.getAbstractTable(referencedClass);

		String s = syntax.createConstraint(getTableName(), "elementId", referencedTable.getTableName(), referencedTable instanceof HistorizedTable);
		if (s != null) {
			execute(s.toString());
		}
	}

	@Override
	protected void findIndexes() {
		// no one is pointing to a ViewSubTable
	}
	
	@Override
	public void insert(Object parentId, List objects) throws SQLException {
		try (PreparedStatement insertStatement = createStatement(sqlPersistence.getConnection(), insertQuery, false)) {
			for (int position = 0; position<objects.size(); position++) {
				Object object = objects.get(position);
				Object elementId = getOrCreateId(object);
				insertStatement.setObject(1, elementId);
				insertStatement.setObject(2, parentId);
				insertStatement.setInt(3, position);
				insertStatement.execute();
			}
		}
	}

	protected void update(Object parentId, int position, Object object) throws SQLException {
		try (PreparedStatement updateStatement = createStatement(sqlPersistence.getConnection(), updateQuery, false)) {
			Object id = IdUtils.getId(object);
			updateStatement.setObject(1, id);
			updateStatement.setObject(2, parentId);
			updateStatement.setInt(3, position);
			updateStatement.execute();
		}
	}

	protected void insert(Object parentId, int position, Object object) throws SQLException {
		try (PreparedStatement insertStatement = createStatement(sqlPersistence.getConnection(), insertQuery, false)) {
			Object element = IdUtils.getId(object);
			insertStatement.setObject(1, element);
			insertStatement.setObject(2, parentId);
			insertStatement.setInt(2, position);
			insertStatement.execute();
		}
	}
	
	protected void delete(Object parentId, int position) throws SQLException {
		try (PreparedStatement deleteStatement = createStatement(sqlPersistence.getConnection(), deleteQuery, false)) {
			deleteStatement.setObject(1, parentId);
			deleteStatement.setInt(2, position);
			deleteStatement.execute();
		}
	}

	@Override
	public List read(Object parentId) throws SQLException {
		try (PreparedStatement selectByIdStatement = createStatement(sqlPersistence.getConnection(), selectByIdQuery, false)) {
			selectByIdStatement.setObject(1, parentId);
			List result = new ArrayList();
			Table table = sqlPersistence.getTable(clazz);
			try (ResultSet resultSet = selectByIdStatement.executeQuery()) {
				while (resultSet.next()) {
					Object elementid = resultSet.getObject(1);
					result.add(table.read(elementid, false));
					
				}
			}
			return result;
		}
	}

	// Queries
	
	@Override
	protected String selectByIdQuery() {
		StringBuilder query = new StringBuilder();
		query.append("SELECT elementId FROM ").append(getTableName()).append(" WHERE id = ? ORDER BY position");
		return query.toString();
	}
	
	@Override
	protected String insertQuery() {
		StringBuilder s = new StringBuilder();
		s.append("INSERT INTO ").append(getTableName());
		s.append(" (elementId, id, position) VALUES (?, ?, ?)");
		return s.toString();
	}

	@Override
	protected String updateQuery() {
		StringBuilder s = new StringBuilder();
		s.append("UPDATE ").append(getTableName()).append(" SET ");
		s.append("elementId = ? WHERE id = ? AND position = ?");
		return s.toString();
	}
	
	@Override
	protected void addFieldColumns(SqlSyntax syntax, StringBuilder s) {
		s.append(",\n elementId "); 
		PropertyInterface elementIdProperty = Properties.getProperty(clazz, "id");
		syntax.addColumnDefinition(s, elementIdProperty);
		s.append(" NOT NULL");
	}
}
