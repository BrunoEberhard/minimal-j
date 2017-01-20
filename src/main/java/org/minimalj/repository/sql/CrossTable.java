package org.minimalj.repository.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.minimalj.model.ViewUtil;
import org.minimalj.model.properties.Properties;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.IdUtils;
import org.minimalj.util.LoggingRuntimeException;

/**
 * Minimal-J internal
 * 
 * note: the id column in this table is the parentId. The id of the elements is called elementId.
 */
public class CrossTable<PARENT, ELEMENT> extends SubTable<PARENT, ELEMENT> implements ListTable<PARENT, ELEMENT> {

	private final String maxPositionQuery;
	
	public CrossTable(SqlRepository sqlRepository, String name, Class<ELEMENT> clazz, PropertyInterface idProperty) {
		super(sqlRepository, name, clazz, idProperty);
		maxPositionQuery = maxPositionQuery();
	}
	
	@Override
	protected void createConstraints(SqlDialect dialect) {
		Class<?> referencedClass = ViewUtil.resolve(getClazz());
		AbstractTable<?> referencedTable = sqlRepository.getAbstractTable(referencedClass);

		String s = dialect.createConstraint(getTableName(), "elementId", referencedTable.getTableName(), referencedTable instanceof HistorizedTable);
		if (s != null) {
			execute(s.toString());
		}
	}

	@Override
	protected void findIndexes() {
		// no one is pointing to a CrossTable
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
	
	public ELEMENT addElement(Object parentId, ELEMENT element) {
		Object elementId = IdUtils.getId(element); 
		if (elementId == null) {
			elementId = sqlRepository.getTable(getClazz()).insert(element);
			IdUtils.setId(element, elementId);
		}
		try (PreparedStatement statement = createStatement(sqlRepository.getConnection(), maxPositionQuery, false)) {
			statement.setObject(1, parentId);
			ResultSet resultSet = statement.executeQuery();
			resultSet.next();
			int nextPosition = resultSet.getInt(1) + 1;
			insert(parentId, nextPosition, element);
			return element;
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, sqlLogger, "addElement failed");
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
	public List<ELEMENT> getList(PARENT parent) {
		return new LazyList<PARENT, ELEMENT>(sqlRepository, getClazz(), parent, getTableName());
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

	public List<ELEMENT> read(Object parentId, int index, int maxResults) throws SQLException {
		try (PreparedStatement selectByIdStatement = createStatement(sqlRepository.getConnection(), selectByIdQuery, false)) {
			selectByIdStatement.setObject(1, parentId);
			List<ELEMENT> result = new ArrayList<>();
			Table<ELEMENT> table = sqlRepository.getTable(clazz);
			try (ResultSet resultSet = selectByIdStatement.executeQuery()) {
				while (resultSet.next() && index > 0) {
					index = index - 1;
				}
				while (resultSet.next() && maxResults > 0) {
					Object elementid = resultSet.getObject(1);
					result.add(table.read(elementid));
					maxResults = maxResults - 1;
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

	protected String maxPositionQuery() {
		StringBuilder s = new StringBuilder();
		s.append("SELECT MAX(position) FROM ").append(getTableName()).append(" WHERE id = ?");
		return s.toString();
	}

	@Override
	protected void addFieldColumns(SqlDialect dialect, StringBuilder s) {
		s.append(",\n elementId "); 
		PropertyInterface elementIdProperty = Properties.getProperty(clazz, "id");
		dialect.addColumnDefinition(s, elementIdProperty);
		s.append(" NOT NULL");
	}

}
