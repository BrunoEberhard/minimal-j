package org.minimalj.backend.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.minimalj.model.properties.FlatProperties;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.IdUtils;
import org.minimalj.util.LoggingRuntimeException;

/**
 * Minimal-J internal
 * 
 * - elements have own id
 * - Additional columns named parent and position
 */
public class ContainingSubTable<PARENT, ELEMENT> extends SubTable<PARENT, ELEMENT> {
	public static final String PARENT = "parent";
	public static final String POSITION = "position";

	protected final Class<PARENT> parentClass;
	protected final String fieldPath;
	
	protected final String selectByParentAndPositionQuery;
	protected final String countQuery;
	protected final String maxQuery;
	
	protected final PropertyInterface parentProperty;
	protected final PropertyInterface positionProperty;
	
	public ContainingSubTable(SqlPersistence sqlPersistence, String name, Class<ELEMENT> elementClass, Class<PARENT> parentClass, PropertyInterface parentIdProperty, String fieldPath) {
		super(sqlPersistence, name, elementClass, parentIdProperty);
		selectByParentAndPositionQuery = selectByParentAndPositionQuery();
		countQuery = countQuery();
		maxQuery = maxQuery();
		
		this.parentClass = parentClass;
		
		this.parentProperty = FlatProperties.getProperty(elementClass, PARENT);
		this.positionProperty = FlatProperties.getProperty(elementClass, POSITION);
		
		this.fieldPath = fieldPath;
	}
	
	@Override
	public List<ELEMENT> read(PARENT parent) {
		return new LazyList<PARENT, ELEMENT>(sqlPersistence, clazz, parent, fieldPath);
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

	protected int maxPosition(Object parentId) {
		try (PreparedStatement statement = createStatement(sqlPersistence.getConnection(), maxQuery, false)) {
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
			IdUtils.setId(object, new ElementId(IdUtils.getId(object), parentClass.getName(), fieldPath));
//			if (object != null) {
//				loadLists(object);
//			}
			return object;
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, sqlLogger, "Couldn't read " + getTableName() + " with parent " + parentId + " on position " + position);
		}
	}

	public boolean add(Object parentId, ELEMENT element) {
		int position = maxPosition(parentId) + 1;
		try (PreparedStatement statement = createStatement(sqlPersistence.getConnection(), insertQuery, false)) {
			int parameterPos = setParameters(statement, element, false, ParameterMode.INSERT, IdUtils.createId());
			statement.setObject(parameterPos++, parentId);
			statement.setInt(parameterPos++, position);
			statement.execute();
			return true;
		} catch (SQLException x) {
			throw new RuntimeException(x.getMessage());
		}
	}

	@Override
	public void addAll(PARENT parent, List<ELEMENT> objects) {
		int position = maxPosition(IdUtils.getId(parent)) + 1;
		try (PreparedStatement insertStatement = createStatement(sqlPersistence.getConnection(), insertQuery, false)) {
			for (position = 0; position<objects.size(); position++) {
				ELEMENT element = objects.get(position);
				parentProperty.setValue(element, parent);
				positionProperty.setValue(element, position++);
				setParameters(insertStatement, element, false, ParameterMode.INSERT, IdUtils.createId());
				insertStatement.execute();
			}
		} catch (SQLException x) {
			throw new RuntimeException(x.getMessage());
		}
	}

	public void update(ElementId id, ELEMENT object) {
		try (PreparedStatement updateStatement = createStatement(sqlPersistence.getConnection(), updateQuery, false)) {
			setParameters(updateStatement, object, false, ParameterMode.UPDATE, id.getId());
			updateStatement.execute();
			
//			for (Entry<PropertyInterface, SubTable> listTableEntry : subTables.entrySet()) {
//				List list  = (List) listTableEntry.getKey().getValue(object);
//				listTableEntry.getValue().replaceAll(object, list);
//			}
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, sqlLogger, "Couldn't update in " + getTableName() + " with " + object);
		}
	}

	
	@Override
	// TODO more efficient implementation. For the add - Transaction this is extremly bad implementation
	public void replaceAll(PARENT parent, List<ELEMENT> objects) {
		// TODO
	}
	
	// Queries

	protected String countQuery() {
		StringBuilder query = new StringBuilder();
		query.append("SELECT COUNT(*) FROM ").append(getTableName()).append(" WHERE parent = ?");
		return query.toString();
	}

	protected String maxQuery() {
		StringBuilder query = new StringBuilder();
		query.append("SELECT MAX(position) FROM ").append(getTableName()).append(" WHERE parent = ?");
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
		s.append("id) VALUES (");
		for (int i = 0; i<getColumns().size(); i++) {
			s.append("?, ");
		}
		s.append("?)");

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
	
	@Override
	protected String deleteQuery() {
		return "DELETE FROM " + getTableName() + " WHERE id = ?";
	}

	@Override
	protected void addSpecialColumns(SqlSyntax syntax, StringBuilder s) {
		syntax.addIdColumn(s, Object.class, 36);
//		s.append(",\n position INTEGER NOT NULL");
	}
	
	@Override
	protected void addPrimaryKey(SqlSyntax syntax, StringBuilder s) {
		syntax.addPrimaryKey(s, "id");
	}
}
