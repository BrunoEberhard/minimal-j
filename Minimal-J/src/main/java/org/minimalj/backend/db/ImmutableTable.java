package org.minimalj.backend.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.minimalj.model.PropertyInterface;
import org.minimalj.util.HashUtils;
import org.minimalj.util.LoggingRuntimeException;

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

	public Long getId(T object) {
		return getId(object, false);
	}

	public Long getOrCreateId(T object) {
		return getId(object, true);
	}
	
	private Long getId(T object, boolean createIfNotExists) {
		if (EmptyObjects.isEmpty(object)) return null;

		int hash = HashUtils.getHash(object);
		try {
			Long id = getId(object, hash);
			if (id == null && createIfNotExists) {
				PreparedStatement insertStatement = getStatement(dbPersistence.getConnection(), insertQuery, true);
				id = executeInsertWithAutoIncrement(insertStatement, object, hash);
			}
			return id;
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, sqlLogger, "Couldn't not getOrCreateId in " + getTableName() + " / Object: " + object);
		}
	}
	
	private Long getId(T object, int hash) throws SQLException {
		Long result;
		
		PreparedStatement selectIdByHashStatement = getStatement(dbPersistence.getConnection(), selectIdByHashQuery, true);
		selectIdByHashStatement.setInt(1, hash);
		try (ResultSet resultSet = selectIdByHashStatement.executeQuery()) {
			if (!resultSet.next()) {
				return null;
			}
			result = resultSet.getLong(1);
			boolean resultByHashUnique = !resultSet.next();
			if (resultByHashUnique) {
				return result;
			}
		}
		
		PreparedStatement selectIdStatement = getStatement(dbPersistence.getConnection(), selectIdQuery, true);
		int parameterPos = setParameters(selectIdStatement, object, true, false);
		selectIdStatement.setInt(parameterPos, hash);
		try (ResultSet resultSet = selectIdStatement.executeQuery()) {
			result = resultSet.next() ? resultSet.getLong(1) : null;
			return result;
		}
	}

	public T read(Long id) {
		if (id == null) return EmptyObjects.getEmptyObject(getClazz());
		
		Connection connection = dbPersistence.getConnection();
		try {
			PreparedStatement selectByIdStatement = getStatement(connection, selectByIdQuery, true);
			selectByIdStatement.setLong(1, id);
			return executeSelect(selectByIdStatement, 0);
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
