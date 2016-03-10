package org.minimalj.backend.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map.Entry;

import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.IdUtils;
import org.minimalj.util.LoggingRuntimeException;

/**
 * Minimal-J internal
 * 
 * - elements have own id
 * - Additional columns named parent and position
 */
public class ContainingSubTable<PARENT, ELEMENT> extends Table<ELEMENT> implements ListTable<PARENT, ELEMENT> {

	protected final String selectByParentQuery;
	protected final String nextPositionQuery;
	protected final String deleteByParentAndPositionQuery;
	protected final String readIdByParentQuery;
	
	public ContainingSubTable(SqlPersistence sqlPersistence, String name, Class<ELEMENT> elementClass) {
		super(sqlPersistence, name, elementClass);
		selectByParentQuery = selectByParentQuery();
		nextPositionQuery = nextPositionQuery();
		deleteByParentAndPositionQuery = deleteByParentAndPositionQuery();
		readIdByParentQuery = readIdByParentQuery();
	}	
	
	@Override
	public List<ELEMENT> getList(PARENT parent) {
		return new LazyList<PARENT, ELEMENT>(sqlPersistence, clazz, parent, getTableName());
	}
	
	public List<ELEMENT> readAll(Object parentId) {
		try (PreparedStatement selectByIdStatement = createStatement(sqlPersistence.getConnection(), selectByParentQuery, false)) {
			selectByIdStatement.setObject(1, parentId);
			return executeSelectAll(selectByIdStatement);
		} catch (SQLException x) {
			throw new RuntimeException(x.getMessage());
		}
	}
	
	protected int nextPosition(Object parentId) {
		try (PreparedStatement statement = createStatement(sqlPersistence.getConnection(), nextPositionQuery, false)) {
			statement.setObject(1, parentId);
			try (ResultSet resultSet = statement.executeQuery()) {
				resultSet.next();
				return resultSet.getInt(1);
			}
		} catch (SQLException x) {
			throw new RuntimeException(x);
		}
	}

	@Override
	public ELEMENT read(Object id) {
		ELEMENT element = super.read(id);
		IdUtils.setId(element, new ElementId(IdUtils.getId(element), getTableName()));
		return element;
	}
	
	private Object insertElement(PreparedStatement statement, ELEMENT element, Object parentId, int position) throws SQLException {
		Object objectId = IdUtils.createId();
		int parameterPos = setParameters(statement, element, false, ParameterMode.INSERT, objectId);
		statement.setObject(parameterPos++, parentId);
		statement.setInt(parameterPos, position);
		statement.execute();
		IdUtils.setId(element, objectId);
		insertLists(element);
		return objectId;
	}
	
	public ELEMENT addElement(Object parentId, ELEMENT element) {
		int position = nextPosition(parentId);
		try (PreparedStatement statement = createStatement(sqlPersistence.getConnection(), insertQuery, false)) {
			Object elementId = insertElement(statement, element, parentId, position);
			return read(elementId);
		} catch (SQLException x) {
			throw new RuntimeException(x.getMessage());
		}
	}

	@Override
	public void addList(PARENT parent, List<ELEMENT> elements) {
		Object parentId = IdUtils.getId(parent);
		int position = nextPosition(parentId);
		try (PreparedStatement statement = createStatement(sqlPersistence.getConnection(), insertQuery, false)) {
			for (ELEMENT element : elements) {
				Object objectId = insertElement(statement, element, parentId, position++);
				IdUtils.setId(element, new ElementId(objectId, getTableName()));
			}
		} catch (SQLException x) {
			throw new RuntimeException(x.getMessage());
		}
	}

	public void update(ElementId id, ELEMENT object) {
		try (PreparedStatement updateStatement = createStatement(sqlPersistence.getConnection(), updateQuery, false)) {
			setParameters(updateStatement, object, false, ParameterMode.UPDATE, id.getId());
			updateStatement.execute();
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, sqlLogger, "Couldn't update in " + getTableName() + " with " + object);
		}
		for (Entry<PropertyInterface, ListTable> listTableEntry : lists.entrySet()) {
			List list  = (List) listTableEntry.getKey().getValue(object);
			listTableEntry.getValue().replaceList(object, list);
		}
	}

	
	@Override
	public void replaceList(PARENT parent, List<ELEMENT> elements) {
		Object parentId = IdUtils.getId(parent);
		// delete all elements from db with have a position that not exists anymore in the new list
		try (PreparedStatement statement = createStatement(sqlPersistence.getConnection(), deleteByParentAndPositionQuery, false)) {
			statement.setObject(1, parentId);
			statement.setInt(2, elements.size());
			statement.execute();
		} catch (SQLException x) {
			throw new RuntimeException(x.getMessage());
		}
		// for each existing id check if it is the same as in the new list
		try (PreparedStatement statement = createStatement(sqlPersistence.getConnection(), readIdByParentQuery, false)) {
			statement.setObject(1, parentId);
			int position = 0;
			try (ResultSet resultSet = statement.executeQuery()) {
				while (resultSet.next()) {
					Object oldId = resultSet.getObject(1);
					ELEMENT element = elements.get(position);
					Object newId = IdUtils.getId(element);
					if (!oldId.equals(newId)) {
						delete(oldId);
						try (PreparedStatement insertStatement = createStatement(sqlPersistence.getConnection(), insertQuery, false)) {
							Object objectId = insertElement(statement, element, parentId, position);
							IdUtils.setId(element, new ElementId(objectId, getTableName()));
						}
					}
					position++;
				}
				while (position < elements.size()) {
					ELEMENT element = elements.get(position);
					try (PreparedStatement insertStatement = createStatement(sqlPersistence.getConnection(), insertQuery, false)) {
						Object objectId = insertElement(statement, element, parentId, position);
						IdUtils.setId(element, new ElementId(objectId, getTableName()));
					}
				}
			}
		} catch (SQLException x) {
			throw new RuntimeException(x.getMessage());
		}
	}
	
	// Queries

	protected String selectByParentQuery() {
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM ").append(getTableName()).append(" WHERE parent = ? ORDER BY position");
		return query.toString();
	}

	protected String nextPositionQuery() {
		StringBuilder query = new StringBuilder();
		query.append("SELECT MAX(position + 1) FROM ").append(getTableName()).append(" WHERE parent = ?");
		return query.toString();
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
		s.append("id, parent, position) VALUES (");
		for (int i = 0; i<getColumns().size(); i++) {
			s.append("?, ");
		}
		s.append("?, ?, ?)");

		return s.toString();
	}

	@Override
	protected String updateQuery() {
		StringBuilder s = new StringBuilder();
		
		s.append("UPDATE ").append(getTableName()).append(" SET ");
		for (Object columnNameObject : getColumns().keySet()) {
			s.append((String) columnNameObject).append("= ?, ");
		}
		s.delete(s.length()-2, s.length());
		s.append(" WHERE id = ?");

		return s.toString();
	}
	
	protected String deleteByParentAndPositionQuery() {
		return "DELETE FROM " + getTableName() + " WHERE parent = ? AND position >= ?";
	}

	protected String readIdByParentQuery() {
		return "SELECT id FROM " + getTableName() + " WHERE parent = ? ORDER BY position";
	}
	
	@Override
	protected void addSpecialColumns(SqlSyntax syntax, StringBuilder s) {
		syntax.addIdColumn(s, Object.class, 36);
		s.append(",\n parent CHAR(36) NOT NULL");
		s.append(",\n position INTEGER NOT NULL");
	}
	
	@Override
	protected void addPrimaryKey(SqlSyntax syntax, StringBuilder s) {
		syntax.addPrimaryKey(s, "id");
	}
}
