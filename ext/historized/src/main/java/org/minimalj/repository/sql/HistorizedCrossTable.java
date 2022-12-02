package org.minimalj.repository.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.minimalj.model.properties.FlatProperties;
import org.minimalj.model.properties.Property;
import org.minimalj.util.EqualsHelper;
import org.minimalj.util.IdUtils;

class HistorizedCrossTable<PARENT, ELEMENT> extends SubTable<PARENT, ELEMENT> implements HistorizedListTable<PARENT, ELEMENT> {

	protected final String selectByIdAndTimeQuery;
	protected final String endQuery;

	public HistorizedCrossTable(SqlRepository sqlRepository, String prefix, Class<ELEMENT> clazz, Property parentIdProperty) {
		super(sqlRepository, prefix, clazz, parentIdProperty);

		selectByIdAndTimeQuery = selectByIdAndTimeQuery();
		endQuery = endQuery();
	}

	@Override
	protected void createConstraints(SqlDialect dialect) {
		// skip, doesn't work on most database because of composed keys
	}

	@Override
	public void addList(PARENT parent, List<ELEMENT> objects) {
		int version = 0;
		Object parentId = IdUtils.getId(parent);
		try (PreparedStatement insertStatement = createStatement(sqlRepository.getConnection(), insertQuery, false)) {
			for (int position = 0; position < objects.size(); position++) {
				insertStatement.setObject(1, parentId);
				insertStatement.setInt(2, position);
				insertStatement.setInt(3, version);
				ELEMENT element = objects.get(position);
				Object elementId = getOrCreateId(element);
				insertStatement.setObject(4, elementId);
				insertStatement.execute();
			}
		} catch (SQLException x) {
			throw new RuntimeException(x.getMessage());
		}
	}

	public void replaceList(PARENT parent, List<ELEMENT> objects, int version) {
		int oldVersion = version - 1;
		List<Object> idsInDb = readIds(parent, oldVersion);
		Object parentId = IdUtils.getId(parent);
		int position = 0;
		while (position < Math.max(objects.size(), idsInDb.size())) {
			boolean end = false;
			boolean insert = false;
			if (position < idsInDb.size() && position < objects.size()) {
				Object object = objects.get(position);
				Object objectId = IdUtils.getId(object);
				Object idInDb = idsInDb.get(position);
				end = insert = !EqualsHelper.equals(objectId, idInDb);
			} else if (position < idsInDb.size()) {
				end = true;
			} else /* if (position < objects.size()) */ {
				insert = true;
			}

			if (end) {
				try (PreparedStatement endStatement = createStatement(sqlRepository.getConnection(), endQuery, false)) {
					endStatement.setInt(1, version);
					endStatement.setObject(2, parentId);
					endStatement.setInt(3, position);
					endStatement.execute();
				} catch (SQLException x) {
					throw new RuntimeException(x.getMessage());
				}
			}

			if (insert) {
				try (PreparedStatement insertStatement = createStatement(sqlRepository.getConnection(), insertQuery, false)) {
					insertStatement.setObject(1, parentId);
					insertStatement.setInt(2, position);
					insertStatement.setInt(3, version);
					ELEMENT element = objects.get(position);
					insertStatement.setObject(4, IdUtils.getId(element));
					insertStatement.execute();
				} catch (SQLException x) {
					throw new RuntimeException(x.getMessage());
				}
			}
			position++;
		}
	}

	private List<Object> readIds(PARENT parent, Integer time) {
		try (PreparedStatement selectByIdAndTimeStatement = createStatement(sqlRepository.getConnection(), selectByIdAndTimeQuery, false)) {
			selectByIdAndTimeStatement.setObject(1, IdUtils.getId(parent));
			selectByIdAndTimeStatement.setInt(2, time);
			selectByIdAndTimeStatement.setInt(3, time);
			return executeSelectIds(selectByIdAndTimeStatement);
		} catch (SQLException x) {
			throw new RuntimeException(x.getMessage());
		}
	}

	private List<Object> executeSelectIds(PreparedStatement preparedStatement) throws SQLException {
		List<Object> result = new ArrayList<>();
		try (ResultSet resultSet = preparedStatement.executeQuery()) {
			while (resultSet.next()) {
				result.add(resultSet.getObject(1));
			}
		}
		return result;
	}

	@Override
	public List<ELEMENT> getList(PARENT parent, Integer time) {
		if (time == null) {
			return getList(parent);
		}
		List<Object> ids = readIds(parent, time);
		Table<ELEMENT> tableElement = sqlRepository.getTable(clazz);
		return ids.stream().map(id -> tableElement.read(id)).collect(Collectors.toList());
	}

	@Override
	public List<ELEMENT> getList(PARENT parent) {
		try (PreparedStatement selectByIdStatement = createStatement(sqlRepository.getConnection(), selectByIdQuery, false)) {
			selectByIdStatement.setObject(1, IdUtils.getId(parent));
			List<Object> ids = executeSelectIds(selectByIdStatement);
			Table<ELEMENT> tableElement = sqlRepository.getTable(clazz);
			return ids.stream().map(id -> tableElement.read(id)).collect(Collectors.toList());
		} catch (SQLException x) {
			throw new RuntimeException(x.getMessage());
		}
	}

	// Queries

	protected String getElementColumn() {
		return "elementId";
	}

	protected String selectByIdAndTimeQuery() {
		return "SELECT " + getElementColumn() + " FROM " + getTableName() + " WHERE id = ? AND startVersion <= ? AND endVersion > ? ORDER BY position";
	}

	@Override
	protected String selectByIdQuery() {
		return "SELECT " + getElementColumn() + " FROM " + getTableName() + " WHERE id = ? AND endVersion = " + Integer.MAX_VALUE + " ORDER BY position";
	}

	@Override
	protected String insertQuery() {
		return "INSERT INTO " + getTableName() + " (id, position, startVersion, endVersion, " + getElementColumn() + ") VALUES (?, ?, ?, " + Integer.MAX_VALUE
				+ ", ?)";
	}

	private String endQuery() {
		return "UPDATE " + getTableName() + " SET endVersion = ? WHERE id = ? AND position = ? AND endVersion = " + Integer.MAX_VALUE;
	}

	@Override
	protected void addSpecialColumns(SqlDialect dialect, StringBuilder s) {
		s.append(" id ");
		dialect.addColumnDefinition(s, parentIdProperty);
		s.append(",\n startVersion INTEGER NOT NULL");
		s.append(",\n endVersion INTEGER NOT NULL");
		s.append(",\n position INTEGER NOT NULL");
		s.append(",\n elementId ");
		Property elementIdProperty = FlatProperties.getProperty(clazz, "id");
		dialect.addColumnDefinition(s, elementIdProperty);
		s.append(" NOT NULL");
	}

	@Override
	protected void addPrimaryKey(SqlDialect dialect, StringBuilder s) {
		dialect.addPrimaryKey(s, "id, startVersion, position");
	}

}