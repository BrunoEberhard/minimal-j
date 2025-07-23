package org.minimalj.repository.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.minimalj.model.properties.Property;
import org.minimalj.util.EqualsHelper;
import org.minimalj.util.IdUtils;

// case: historized parent has historized elements
class HistorizedCrossHistorizedTable<PARENT, ELEMENT> extends HistorizedCrossTable<PARENT, ELEMENT> {

	public HistorizedCrossHistorizedTable(SqlRepository sqlRepository, String prefix, Class<ELEMENT> clazz, Property parentIdProperty) {
		super(sqlRepository, prefix, clazz, parentIdProperty);
	}

	@Override
	public void addList(PARENT parent, List<ELEMENT> objects) {
		int version = 0;
		Object parentId = IdUtils.getId(parent);
		try (Connection connection = sqlRepository.getConnection(); PreparedStatement insertStatement = createStatement(connection, insertQuery, false)) {
			for (int position = 0; position < objects.size(); position++) {
				insertStatement.setObject(1, parentId);
				insertStatement.setInt(2, position);
				insertStatement.setInt(3, version);
				ELEMENT element = objects.get(position);
				Object elementId = getOrCreateId(element);
				insertStatement.setObject(4, elementId);
				insertStatement.setObject(5, IdUtils.getVersion(element));

				insertStatement.execute();
			}
		} catch (SQLException x) {
			throw new RuntimeException(x.getMessage());
		}
	}

	public void replaceList(PARENT parent, List<ELEMENT> objects, int version) {
		int oldVersion = version - 1;
		List<ObjectWithVersion> idsInDb = readIds(parent, oldVersion);
		Object parentId = IdUtils.getId(parent);
		int position = 0;
		while (position < Math.max(objects.size(), idsInDb.size())) {
			boolean end = false;
			boolean insert = false;
			if (position < idsInDb.size() && position < objects.size()) {
				Object object = objects.get(position);
				Object objectId = IdUtils.getId(object);
				int newVersion = IdUtils.getVersion(object);
				Object idInDb = idsInDb.get(position).id;
				int versionInDb = idsInDb.get(position).version;
				end = insert = !EqualsHelper.equals(objectId, idInDb) || newVersion != versionInDb;
			} else if (position < idsInDb.size()) {
				end = true;
			} else /* if (position < objects.size()) */ {
				insert = true;
			}

			if (end) {
				try (Connection connection = sqlRepository.getConnection(); PreparedStatement endStatement = createStatement(connection, endQuery, false)) {
					endStatement.setInt(1, version);
					endStatement.setObject(2, parentId);
					endStatement.setInt(3, position);
					endStatement.execute();
				} catch (SQLException x) {
					throw new RuntimeException(x.getMessage());
				}
			}

			if (insert) {
				try (Connection connection = sqlRepository.getConnection(); PreparedStatement insertStatement = createStatement(connection, insertQuery, false)) {
					insertStatement.setObject(1, parentId);
					insertStatement.setInt(2, position);
					insertStatement.setInt(3, version);
					ELEMENT element = objects.get(position);
					insertStatement.setObject(4, IdUtils.getId(element));
					insertStatement.setInt(5, IdUtils.getVersion(element));
					insertStatement.execute();
				} catch (SQLException x) {
					throw new RuntimeException(x.getMessage());
				}
			}
			position++;
		}
	}

	private static class ObjectWithVersion {
		public Object id;
		public int version;
	}

	private List<ObjectWithVersion> readIds(PARENT parent, Integer time) {
		try (Connection connection = sqlRepository.getConnection(); PreparedStatement selectByIdAndTimeStatement = createStatement(connection, selectByIdAndTimeQuery, false)) {
			selectByIdAndTimeStatement.setObject(1, IdUtils.getId(parent));
			selectByIdAndTimeStatement.setInt(2, time);
			selectByIdAndTimeStatement.setInt(3, time);
			return executeSelectIds(selectByIdAndTimeStatement);
		} catch (SQLException x) {
			throw new RuntimeException(x.getMessage());
		}
	}

	private List<ObjectWithVersion> executeSelectIds(PreparedStatement preparedStatement) throws SQLException {
		List<ObjectWithVersion> result = new ArrayList<>();
		try (ResultSet resultSet = preparedStatement.executeQuery()) {
			while (resultSet.next()) {
				ObjectWithVersion id = new ObjectWithVersion();
				id.id = resultSet.getObject(1);
				id.version = resultSet.getInt(2);
				result.add(id);
			}
		}
		return result;
	}

	@Override
	public List<ELEMENT> getList(PARENT parent, Map<Class<?>, Map<Object, Object>> loadedReferences) {
		try (Connection connection = sqlRepository.getConnection(); PreparedStatement selectByIdStatement = createStatement(connection, selectByIdQuery, false)) {
			selectByIdStatement.setObject(1, IdUtils.getId(parent));
			List<ObjectWithVersion> ids = executeSelectIds(selectByIdStatement);
			HistorizedTable<ELEMENT> tableElement = (HistorizedTable<ELEMENT>) sqlRepository.getTable(clazz);
			return ids.stream().map(id -> tableElement.read(id.id, id.version)).collect(Collectors.toList());
		} catch (SQLException x) {
			throw new RuntimeException(x.getMessage());
		}
	}

	// Queries

	@Override
	protected String getElementColumn() {
		return "elementId, elementVersion";
	}

	@Override
	protected String insertQuery() {
		return "INSERT INTO " + getTableName() + " (id, position, startVersion, endVersion, " + getElementColumn() + ") VALUES (?, ?, ?, " + Integer.MAX_VALUE
				+ ", ?, ?)";
	}

	@Override
	protected void addSpecialColumns(SqlDialect dialect, StringBuilder s) {
		super.addSpecialColumns(dialect, s);
		s.append(",\n elementVersion INTEGER NOT NULL");
	}

}