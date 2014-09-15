package org.minimalj.backend.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

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
	
	protected final String checkByUuid;

	public ImmutableTable(DbPersistence dbPersistence, Class<T> clazz) {
		super(dbPersistence, null, clazz, null);
		if (clazz.equals(List.class)) {
			throw new IllegalArgumentException();
		}
		checkByUuid = checkByUuid();
	}

	protected void addSpecialColumns(DbSyntax syntax, StringBuilder s) {
		syntax.addIdColumn(s, Object.class, 0);
	}
	
	/**
	 * If the immutable already exists returns the id of the
	 * existing id. If the immutable not yet exists creates it
	 * and returns the id of the created immutable
	 * 
	 * @param object
	 * @return <code>null</code> for the empty immutable or the id given immutable
	 */
	public String getId(T object) {
		if (EmptyObjects.isEmpty(object)) return null;

		String id = HashUtils.getUuid(object).toString();
		try {
			boolean exists = check(id);
			if (!exists) {
				PreparedStatement insertStatement = getStatement(dbPersistence.getConnection(), insertQuery, false);
				executeInsert(insertStatement, object, id);
			}
			return id.toString();
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, sqlLogger, "Couldn't not getId in " + getTableName() + " / Object: " + object);
		}
	}
	
	private boolean check(String id) throws SQLException {
		PreparedStatement checkByUuidStatement = getStatement(dbPersistence.getConnection(), checkByUuid, false);
		checkByUuidStatement.setString(1, id);
		try (ResultSet resultSet = checkByUuidStatement.executeQuery()) {
			return resultSet.next();
		}
	}

	public T read(String id) {
		if (id == null) return EmptyObjects.getEmptyObject(getClazz());
		
		Connection connection = dbPersistence.getConnection();
		try {
			PreparedStatement selectByIdStatement = getStatement(connection, selectByIdQuery, true);
			selectByIdStatement.setString(1, id);
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
	
	protected String checkByUuid() {
		StringBuilder query = new StringBuilder();
		query.append("SELECT 1 FROM "); query.append(getTableName()); query.append(" WHERE id = ?");
		
		return query.toString();
	}
	
	@Override
	protected String insertQuery() {
		StringBuilder s = new StringBuilder();
		
		s.append("INSERT INTO "); s.append(getTableName()); s.append(" (");
		for (String columnName : getColumns().keySet()) {
			s.append(columnName);
			s.append(", ");
		}
		s.append("id) VALUES (");
		for (int i = 0; i<getColumns().size(); i++) {
			s.append("?, ");
		}
		s.append("?)");

		return s.toString();
	}
}
