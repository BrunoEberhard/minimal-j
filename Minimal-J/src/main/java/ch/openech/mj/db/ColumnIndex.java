package ch.openech.mj.db;//

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import ch.openech.mj.model.PropertyInterface;
import ch.openech.mj.util.LoggingRuntimeException;


public class ColumnIndex<T> extends AbstractIndex<T> {

	private final ColumnIndex<?> innerIndex;
	
	ColumnIndex(DbPersistence dbPersistence, AbstractTable<T> table, PropertyInterface property, String column, ColumnIndex<?> innerIndex) {
		super(dbPersistence, table, property, column);
		this.innerIndex = innerIndex;
	}

	private List<Long> findIds(List<Long> ids) throws SQLException {
		PreparedStatement selectStatement = table.getStatement(dbPersistence.getConnection(), selectQuery, false);
		List<Long> result = new ArrayList<>(ids.size());
		for (Long i : ids) {
			helper.setParameter(selectStatement, 1, i, property);
			result.addAll(executeSelectIds(selectStatement));
		}
		return result;
	}
	
	public List<Long> findIds(Object query) {
		Connection connection = dbPersistence.getConnection();
		try {
			if (innerIndex != null) {
				List<Long> queryIds = innerIndex.findIds(query);
				return findIds(queryIds);
			}
			PreparedStatement selectStatement = table.getStatement(connection, selectQuery, false);
			helper.setParameter(selectStatement, 1, query, property);
			List<Long> result = executeSelectIds(selectStatement);
			return result;
		} catch (SQLException x) {
			String message = "Couldn't use index of column " + column + " of table " + table.getTableName() + " with query " + query;
			throw new LoggingRuntimeException(x, sqlLogger, message);
		}
	}

	public List<T> findObjects(Object query) {
		List<Long> ids = findIds(query);
		List<T> result = new ArrayList<>(ids.size());
		for (Long id : ids) {
			result.add(lookup(id));
		}
		return result;
	}
	
	private List<Long> executeSelectIds(PreparedStatement preparedStatement) throws SQLException {
		List<Long> result = new ArrayList<>();
		try (ResultSet resultSet = preparedStatement.executeQuery()) {
			while (resultSet.next()) {
				result.add(resultSet.getLong(1));
			}
		}
		return result;
	}

}
