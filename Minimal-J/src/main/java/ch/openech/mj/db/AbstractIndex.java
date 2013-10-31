package ch.openech.mj.db;//

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import ch.openech.mj.model.PropertyInterface;

public abstract class AbstractIndex<T> implements Index<T> {
	public static final Logger sqlLogger = Logger.getLogger("SQL");

	protected final DbPersistence dbPersistence;
	protected final DbPersistenceHelper helper;
	protected final AbstractTable<T> table;
	protected final PropertyInterface property;
	protected final String column;
	
	protected PreparedStatement selectByColumnStatement;

	protected AbstractIndex(DbPersistence dbPersistence, AbstractTable<T> table, PropertyInterface property, String column) {
		this.dbPersistence = dbPersistence;
		this.helper = new DbPersistenceHelper(dbPersistence);
		this.table = table;
		this.property = property;
		this.column = column;
	}

	public T lookup(Integer id) {
		if (id != null) {
			if (table instanceof ImmutableTable) {
				return ((ImmutableTable<T>) table).read(id);
			} else if (table instanceof Table) {
				return ((Table<T>) table).read(id);
			} else {
				throw new IllegalStateException();
			}
		} else {
			return null;
		}
	}

	@Override
	public String getColumn() {
		return column;
	}
	
	@Override
	public void initialize() throws SQLException {
		selectByColumnStatement = prepare(selectQuery());
	}
	
	protected String selectQuery() {
		StringBuilder query = new StringBuilder();
		query.append("SELECT id FROM "); query.append(table.getTableName()); 
		query.append(" WHERE "); query.append(column); query.append(" = ?");
		if (table instanceof HistorizedTable) {
			query.append(" AND version = 0");
		}
		return query.toString();
	}
	
	protected Integer executeSelectId(PreparedStatement preparedStatement) throws SQLException {
		try (ResultSet resultSet = preparedStatement.executeQuery()) {
			if (resultSet.next()) {
				return resultSet.getInt(1);
			} else {
				return null;
			}
		}
	}

	protected Connection getConnection() {
		return dbPersistence.getConnection();
	}

	protected PreparedStatement prepare(String statement) throws SQLException {
		if (sqlLogger.isLoggable(Level.FINE)) {
			return new LoggingPreparedStatement(getConnection(), statement, sqlLogger);
		} else {
			return getConnection().prepareStatement(statement);
		}
	}

	@Override
	public void closeStatements() throws SQLException {
		selectByColumnStatement.close();
	}

}
