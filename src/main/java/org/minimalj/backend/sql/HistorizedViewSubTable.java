package org.minimalj.backend.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.minimalj.model.properties.Properties;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.security.permissiontest.pkgrole.T;

/**
 * Minimal-J internal
 * 
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class HistorizedViewSubTable extends HistorizedSubTable {

	public HistorizedViewSubTable(SqlPersistence sqlPersistence, String prefix, Class viewClass, PropertyInterface idProperty) {
		super(sqlPersistence, prefix, viewClass, idProperty);
	}

	@Override
	protected void findIndexes() {
		// no one is pointing to a ViewSubTable
	}
	
	@Override
	protected int setParameters(PreparedStatement statement, Object object, boolean doubleValues, ParameterMode mode, Object parentId) throws SQLException {
		int parameterPos = 1;
		Object elementId = getOrCreateId(object);
		statement.setObject(parameterPos++, elementId);
		if (doubleValues) statement.setObject(parameterPos++, elementId);
		statement.setObject(parameterPos++, parentId);
		if (doubleValues) statement.setObject(parameterPos++, parentId);
		return parameterPos;
	}

	@Override
	public List read(Object parentId, Integer time) throws SQLException {
		if (time == null) {
			return read(parentId);
		}
		try (PreparedStatement selectByIdAndTimeStatement = createStatement(sqlPersistence.getConnection(), selectByIdAndTimeQuery, false)) {
			selectByIdAndTimeStatement.setObject(1, parentId);
			selectByIdAndTimeStatement.setInt(2, time);
			selectByIdAndTimeStatement.setInt(3, time);

			List result = new ArrayList();
			Table<T> table = sqlPersistence.getTable(clazz);
			try (ResultSet resultSet = selectByIdAndTimeStatement.executeQuery()) {
				while (resultSet.next()) {
					Object elementid = resultSet.getObject(1);
					result.add(table.read(elementid, false));
					
				}
			}
			return result;
		}
	}

	@Override
	protected List read(Object id) throws SQLException {
		try (PreparedStatement selectByIdStatement = createStatement(sqlPersistence.getConnection(), selectByIdQuery, false)) {
			selectByIdStatement.setObject(1, id);

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
	
	@Override
	protected String insertQuery() {
		StringBuilder s = new StringBuilder();
		s.append("INSERT INTO ").append(getTableName());
		s.append(" (elementId, id, position, startVersion, endVersion) VALUES (?, ?, ?, ?, 0)");
		return s.toString();
	}
	
	@Override
	protected String selectByIdAndTimeQuery() {
		StringBuilder query = new StringBuilder();
		query.append("SELECT elementId FROM ").append(getTableName()); 
		query.append(" WHERE id = ? AND (startVersion = 0 OR startVersion < ?) AND (endVersion = 0 OR endVersion >= ?) ORDER BY position");
		return query.toString();
	}
	
	@Override
	protected String selectByIdQuery() {
		StringBuilder query = new StringBuilder();
		query.append("SELECT elementId FROM ").append(getTableName()).append(" WHERE id = ?");
		query.append(" AND endVersion = 0 ORDER BY position");
		return query.toString();
	}
	
	@Override
	protected void addFieldColumns(SqlSyntax syntax, StringBuilder s) {
		s.append(",\n elementId "); 
		PropertyInterface elementIdProperty = Properties.getProperty(clazz, "id");
		syntax.addColumnDefinition(s, elementIdProperty);
		s.append(" NOT NULL");
	}
}
