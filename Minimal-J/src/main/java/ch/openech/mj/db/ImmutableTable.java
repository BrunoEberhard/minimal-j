package ch.openech.mj.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import ch.openech.mj.model.PropertyInterface;
import ch.openech.mj.util.HashUtils;
import ch.openech.mj.util.LoggingRuntimeException;

/**
 * Minimal-J internal<p>
 * 
 * Rows in a immutable table are created and reused. But
 * they are never changed nor deleted.
 * 
 */
public class ImmutableTable<T> extends AbstractTable<T> {
	
	protected final String selectIdQuery;
	protected final String selectIdByHashQuery;

	public ImmutableTable(DbPersistence dbPersistence, Class<T> clazz) {
		super(dbPersistence, null, clazz);
		if (clazz.equals(List.class)) {
			throw new IllegalArgumentException();
		}

		selectIdQuery = selectIdQuery();
		selectIdByHashQuery = selectIdByHashQuery();
	}

	public Integer getId(T object) {
		return getId(object, false);
	}

	public Integer getOrCreateId(T object) {
		return getId(object, true);
	}
	
	private Integer getId(T object, boolean createIfNotExists) {
		if (EmptyObjects.isEmpty(object)) return null;

		int hash = HashUtils.getHash(object);
		try {
			Integer id = getId(object, hash);
			if (id == null && createIfNotExists) {
				PreparedStatement insertStatement = getStatement(dbPersistence.getConnection(), insertQuery, true);
				id = executeInsertWithAutoIncrement(insertStatement, object, hash);
			}
			return id;
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, sqlLogger, "Couldn't not getOrCreateId in " + getTableName() + " / Object: " + object);
		}
	}
	
	private Integer getId(T object, int hash) throws SQLException {
		Integer result;
		
		PreparedStatement selectIdByHashStatement = getStatement(dbPersistence.getConnection(), selectIdByHashQuery, true);
		selectIdByHashStatement.setInt(1, hash);
		try (ResultSet resultSet = selectIdByHashStatement.executeQuery()) {
			if (!resultSet.next()) {
				return null;
			}
			result = resultSet.getInt(1);
			boolean resultByHashUnique = !resultSet.next();
			if (resultByHashUnique) {
				return result;
			}
		}
		
		PreparedStatement selectIdStatement = getStatement(dbPersistence.getConnection(), selectIdQuery, true);
		int parameterPos = setParameters(selectIdStatement, object, true, false);
		selectIdStatement.setInt(parameterPos, hash);
		try (ResultSet resultSet = selectIdStatement.executeQuery()) {
			result = resultSet.next() ? resultSet.getInt(1) : null;
			return result;
		}
	}

	public T read(Integer id) {
		return read(dbPersistence.getConnection(), id);
	}
	
	private T read(Connection connection, Integer id) {
		if (id == null) return EmptyObjects.getEmptyObject(getClazz());
		
		try {
			PreparedStatement selectByIdStatement = getStatement(connection, selectByIdQuery, true);
			selectByIdStatement.setInt(1, id);
			return executeSelect(selectByIdStatement);
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, sqlLogger, "Couldn't read " + getTableName() + " with ID " + id);
		}
	}

	public void insert(Connection connection, T object) throws SQLException {
		PreparedStatement insertStatement = getStatement(connection, insertQuery, true);
		executeInsert(insertStatement, object);
	}
	
	// Statements

	@Override
	protected String selectByIdQuery() {
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM "); query.append(getTableName()); 
		query.append(" WHERE id = ?");
		return query.toString();
	}
	
	@Override
	protected String selectIdQuery() {
		StringBuilder where = new StringBuilder();
	
		boolean first = true;	
		for (String key : getColumns().keySet()) {
			if (!first) where.append(" AND "); else first = false;
			
			// where.append(column.getName()); where.append(" = ?");
			// doesnt work for null so pattern is:
			// ((? IS NULL AND col1 IS NULL) OR col1 = ?)
			where.append("((? IS NULL AND "); where.append(key); where.append(" IS NULL) OR ");
			where.append(key); where.append(" = ?)");
		}
		
		StringBuilder query = new StringBuilder();
		query.append("SELECT id FROM "); query.append(getTableName()); query.append(" WHERE ");
		query.append(where);
		query.append(" AND hash = ?");
		
		return query.toString();
	}
	
	protected String selectIdByHashQuery() {
		StringBuilder query = new StringBuilder();
		query.append("SELECT id FROM "); query.append(getTableName()); query.append(" WHERE hash = ?");
		
		return query.toString();
	}
	
	@Override
	protected String insertQuery() {
		StringBuilder s = new StringBuilder();
		
		s.append("INSERT INTO "); s.append(getTableName()); s.append(" (");
		Map<String, PropertyInterface> properties = getColumns();
		int size = properties.entrySet().size();
		for (String name : properties.keySet()) {
			s.append(name);
			s.append(", ");
		}
		s.append("hash) VALUES (");
		for (int j = 0; j<size; j++) {
			s.append("?");
			s.append(", ");
		}
		s.append(" ?)");
		
		return s.toString();
	}

}
