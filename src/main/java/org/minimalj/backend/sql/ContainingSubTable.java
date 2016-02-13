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

	protected final String selectByParentAndPositionQuery;
	protected final String countQuery;
	protected final String nextPositionQuery;
	protected final String deleteByParentQuery;
	
	public ContainingSubTable(SqlPersistence sqlPersistence, String name, Class<ELEMENT> elementClass) {
		super(sqlPersistence, name, elementClass);
		selectByParentAndPositionQuery = selectByParentAndPositionQuery();
		countQuery = countQuery();
		nextPositionQuery = nextPositionQuery();
		deleteByParentQuery = deleteByParentQuery();
	}	
	
	@Override
	public List<ELEMENT> readAll(PARENT parent) {
		return new LazyList<PARENT, ELEMENT>(sqlPersistence, clazz, parent, getTableName());
	}
	
	public int size(Object parentId) {
		try (PreparedStatement statement = createStatement(sqlPersistence.getConnection(), countQuery, false)) {
			statement.setObject(1, parentId);
			try (ResultSet resultSet = statement.executeQuery()) {
				resultSet.next();
				return resultSet.getInt(1);
			}
		} catch (SQLException x) {
			throw new RuntimeException(x);
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

	public ELEMENT read(Object parentId, int position) {
		try (PreparedStatement statement = createStatement(sqlPersistence.getConnection(), selectByParentAndPositionQuery, false)) {
			statement.setObject(1, parentId);
			statement.setInt(2, position);
			ELEMENT object = executeSelect(statement);
			if (object != null) {
				IdUtils.setId(object, new ElementId(IdUtils.getId(object), getTableName(), position));
				loadLists(object);
			}
			return object;
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, sqlLogger, "Couldn't read " + getTableName() + " with parent " + parentId + " on position " + position);
		}
	}

	private ElementId insertElement(PreparedStatement statement, ELEMENT element, Object parentId, int position) throws SQLException {
		ElementId elementId = new ElementId(IdUtils.createId(), getTableName(), position);
		int parameterPos = setParameters(statement, element, false, ParameterMode.INSERT, elementId.getId());
		statement.setObject(parameterPos++, parentId);
		statement.setInt(parameterPos, position);
		statement.execute();
		IdUtils.setId(element, elementId);
		insertLists(element);
		return elementId;
	}
	
	public void add(Object parentId, ELEMENT element) {
		int position = nextPosition(parentId);
		try (PreparedStatement statement = createStatement(sqlPersistence.getConnection(), insertQuery, false)) {
			insertElement(statement, element, parentId, position);
		} catch (SQLException x) {
			throw new RuntimeException(x.getMessage());
		}
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
	public void addAll(PARENT parent, List<ELEMENT> elements) {
		Object parentId = IdUtils.getId(parent);
		int position = nextPosition(parentId);
		try (PreparedStatement statement = createStatement(sqlPersistence.getConnection(), insertQuery, false)) {
			for (ELEMENT element : elements) {
				insertElement(statement, element, parentId, position++);
			}
		} catch (SQLException x) {
			throw new RuntimeException(x.getMessage());
		}
	}

	public void update(ElementId id, ELEMENT object) {
		try (PreparedStatement updateStatement = createStatement(sqlPersistence.getConnection(), updateQuery, false)) {
			setParameters(updateStatement, object, false, ParameterMode.UPDATE, id.getId());
			updateStatement.execute();
			for (Entry<PropertyInterface, ListTable> listTableEntry : lists.entrySet()) {
				List list  = (List) listTableEntry.getKey().getValue(object);
				listTableEntry.getValue().replaceAll(object, list);
			}
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, sqlLogger, "Couldn't update in " + getTableName() + " with " + object);
		}
	}

	
	@Override
	// TODO more efficient implementation.
	public void replaceAll(PARENT parent, List<ELEMENT> elements) {
		Object parentId = IdUtils.getId(parent);
		try (PreparedStatement statement = createStatement(sqlPersistence.getConnection(), deleteByParentQuery, false)) {
			statement.setObject(1, parentId);
			statement.execute();
			addAll(parent, elements);
		} catch (SQLException x) {
			throw new RuntimeException(x.getMessage());
		}
	}
	
	// Queries

	protected String countQuery() {
		StringBuilder query = new StringBuilder();
		query.append("SELECT COUNT(*) FROM ").append(getTableName()).append(" WHERE parent = ?");
		return query.toString();
	}

	protected String nextPositionQuery() {
		StringBuilder query = new StringBuilder();
		query.append("SELECT MAX(position + 1) FROM ").append(getTableName()).append(" WHERE parent = ?");
		return query.toString();
	}

	protected String selectByParentAndPositionQuery() {
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM ").append(getTableName()).append(" WHERE parent = ? AND position = ?");
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
	
	protected String deleteByParentQuery() {
		return "DELETE FROM " + getTableName() + " WHERE parent = ?";
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
