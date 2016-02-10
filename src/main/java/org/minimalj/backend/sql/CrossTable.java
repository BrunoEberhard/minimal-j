package org.minimalj.backend.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.IdUtils;
import org.minimalj.util.LoggingRuntimeException;

/**
 * Minimal-J internal
 * 
 * 3 columns:
 * parent (id to list owner)
 * position
 * id
 */
public class CrossTable<PARENT, ELEMENT> extends ContainingSubTable<PARENT, ELEMENT> {

	private final PropertyInterface parentIdProperty;
	
	public CrossTable(SqlPersistence sqlPersistence, String name, Class<ELEMENT> elementClass, Class<PARENT> parentClass, PropertyInterface parentIdProperty) {
		super(sqlPersistence, name, elementClass, parentClass);
		this.parentIdProperty = parentIdProperty;
	}
	
	@Override
	public ELEMENT read(Object parentId, int position) {
		try (PreparedStatement statement = createStatement(sqlPersistence.getConnection(), selectByParentAndPositionQuery, false)) {
			statement.setObject(1, parentId);
			statement.setInt(2, position);
			try (ResultSet resultSet = statement.executeQuery()) {
				resultSet.next();
				Object id = resultSet.getObject(1);
				return sqlPersistence.getTable(getClazz()).read(id);
			}
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, sqlLogger, "Couldn't read " + getTableName() + " with parent " + parentId + " on position " + position);
		}
	}
	
	@Override
	public boolean add(Object parentId, ELEMENT element) {
		Object elementId = IdUtils.getId(element);
		if (elementId == null) {
			elementId = sqlPersistence.getTable(clazz).insert(element);
		}
		try (PreparedStatement statement = createStatement(sqlPersistence.getConnection(), insertQuery, false)) {
			statement.setObject(1, parentId);
			statement.setInt(2, nextPosition(parentId));
			statement.setObject(3, elementId);
			statement.execute();
			return true;
		} catch (SQLException x) {
			throw new RuntimeException(x.getMessage());
		}
	}
	
	@Override
	public void addAll(PARENT parent, List<ELEMENT> objects) {
		try (PreparedStatement statement = createStatement(sqlPersistence.getConnection(), insertQuery, false)) {
			int position = nextPosition(IdUtils.getId(parent));
			for (ELEMENT element : objects) {
				Object elementId = IdUtils.getId(element);
				if (elementId == null) {
					elementId = sqlPersistence.getTable((Class<ELEMENT>) element.getClass()).insert(element);
				}
				statement.setObject(1, IdUtils.getId(parent));
				statement.setInt(2, position++);
				statement.setObject(3, elementId);
				statement.execute();
			}
		} catch (SQLException x) {
			throw new RuntimeException(x.getMessage());
		}
	}

	@Override
	// TODO more efficient implementation. For the add - Transaction this is extremly bad implementation
	public void replaceAll(PARENT parent, List<ELEMENT> objects) {
		// TODO
	}
	
	// Queries
	
	@Override
	protected String selectByParentAndPositionQuery() {
		StringBuilder query = new StringBuilder();
		query.append("SELECT id FROM ").append(getTableName()).append(" WHERE parent = ? AND position = ?");
		return query.toString();
	}

	@Override
	protected String insertQuery() {
		StringBuilder s = new StringBuilder();
		s.append("INSERT INTO ").append(getTableName());
		s.append(" (parent, position, id) VALUES (?, ?, ?)");
		return s.toString();
	}

	@Override
	protected String updateQuery() {
		StringBuilder s = new StringBuilder();
		s.append("UPDATE ").append(getTableName()).append(" SET id = ? WHERE parent = ? AND position = ?");
		return s.toString();
	}
	
	@Override
	protected String deleteQuery() {
		return "DELETE FROM " + getTableName() + " WHERE parent = ?";
	}

	@Override
	protected void addFieldColumns(SqlSyntax syntax, StringBuilder s) {
		// no field columns
	}
	
	@Override
	protected void addSpecialColumns(SqlSyntax syntax, StringBuilder s) {
		syntax.addIdColumn(s, Object.class, 36);
		s.append(",\n parent ");
		syntax.addColumnDefinition(s, parentIdProperty);
		s.append(" NOT NULL");
		s.append(",\n position INTEGER NOT NULL");
	}
	
	@Override
	protected void addPrimaryKey(SqlSyntax syntax, StringBuilder s) {
		syntax.addPrimaryKey(s, "parent, position");
	}

}
