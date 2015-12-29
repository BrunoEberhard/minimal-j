package org.minimalj.backend.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.minimalj.model.UnloadedList;
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
	public void insert(Object parentId, List objects) throws SQLException {
		try (PreparedStatement insertStatement = createStatement(sqlPersistence.getConnection(), insertQuery, false)) {
			for (int position = 0; position<objects.size(); position++) {
				Object object = objects.get(position);
				Object elementId = IdUtils.getId(object);
				if (elementId == null) {
					if (getClazz().isInstance(object)) {
						elementId = sqlPersistence.insert(object);
					}
				} else if (getClazz().isInstance(elementId)) {
					// Special case: if id is the object itself, then insert that object
					elementId = sqlPersistence.insert(elementId);
				}
				insertStatement.setObject(1, parentId);
				insertStatement.setInt(2, position);
				insertStatement.setObject(3, elementId);
				insertStatement.execute();
			}
		}
	}

	@Override
	protected void update(Object parentId, List objects) throws SQLException {
		List objectsInDb = read(parentId);
		int position = 0;
		while (position < Math.max(objects.size(), objectsInDb.size())) {
			if (position < objectsInDb.size() && position < objects.size()) {
				update(parentId, position, objects.get(position));
			} else if (position < objectsInDb.size()) {
				// delete all beginning from this position with one delete statement
				delete(parentId, position);
				break; 
			} else /* if (position < objects.size()) */ {
				insert(parentId, position, objects.get(position));
			}
			position++;
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
			insertStatement.setObject(1, parentId);
			insertStatement.setInt(2, position);
			Object element = IdUtils.getId(object);
			insertStatement.setObject(3, element);
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

	@SuppressWarnings("unchecked")
	@Override
	public List read(Object parentId) throws SQLException {
		return new UnloadedList(getClazz());
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
		s.append("INSERT INTO ").append(getTableName()).append(" (");
		s.append("id, position, elementId) VALUES (?, ?, ?)");
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
