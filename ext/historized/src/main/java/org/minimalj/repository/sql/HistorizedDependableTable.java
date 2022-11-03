package org.minimalj.repository.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.LoggingRuntimeException;

/**
 * Minimal-J internal
 * 
 * - In this tables the parentId is used as id 
 * - has no sub tables
 */
public class HistorizedDependableTable<PARENT, ELEMENT> extends DependableTable<PARENT, ELEMENT> {

	protected final String selectByIdAndTimeQuery;
	
	
	public HistorizedDependableTable(SqlRepository sqlRepository, String name, Class<ELEMENT> clazz, PropertyInterface parentIdProperty) {
		super(sqlRepository, name, clazz, parentIdProperty);
		selectByIdAndTimeQuery = selectByIdAndTimeQuery();
	}

	@Override
	protected void createConstraints(SqlDialect dialect) {
		super.createConstraints(dialect);
		createIdConstraint(dialect);
	}

	protected void createIdConstraint(SqlDialect dialect) {
		Class<?> parentClass = parentIdProperty.getDeclaringClass();
		Table<?> parentTable = sqlRepository.getTable(parentClass);
		createConstraint(dialect, "ID", parentTable);
	}

	@Override
	public ELEMENT read(Object parentId, Integer version) {
		if (version == null) {
			try (PreparedStatement selectByIdStatement = createStatement(sqlRepository.getConnection(), selectByIdQuery, false)) {
				selectByIdStatement.setObject(1, parentId);
				ELEMENT object = executeSelect(selectByIdStatement);
				return object;
			} catch (SQLException x) {
				throw new LoggingRuntimeException(x, sqlLogger, "Couldn't read " + getTableName() + " with ID " + parentId);
			}
		} else {
			try (PreparedStatement selectByIdStatement = createStatement(sqlRepository.getConnection(), selectByIdAndTimeQuery, false)) {
				selectByIdStatement.setObject(1, parentId);
				selectByIdStatement.setObject(2, version);
				selectByIdStatement.setObject(3, version);
				ELEMENT object = executeSelect(selectByIdStatement);
				return object;
			} catch (SQLException x) {
				throw new LoggingRuntimeException(x, sqlLogger, "Couldn't read " + getTableName() + " with ID " + parentId);
			}
		}
	}

	@Override
	protected void doUpdate(Object parentId, ELEMENT object, Integer version) {
		delete(parentId, version);
		insert(parentId, object, version);
	}
	
	@Override
	protected void insert(Object parentId, ELEMENT object, Integer version) {
		try (PreparedStatement insertStatement = createStatement(sqlRepository.getConnection(), insertQuery, false)) {
			int parameterPos = setParameters(insertStatement, object, ParameterMode.INSERT, parentId);
			insertStatement.setInt(parameterPos, version);
			insertStatement.execute();
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	@Override
	protected void delete(Object parentId, Integer version) {
		try (PreparedStatement deleteStatement = createStatement(sqlRepository.getConnection(), deleteQuery, false)) {
			deleteStatement.setObject(1, version);
			deleteStatement.setObject(2, parentId);
			deleteStatement.execute();
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	// Queries

	@Override
	protected String selectByIdQuery() {
		return "SELECT * FROM " + getTableName() + " WHERE id = ? AND endVersion IS NULL";
	}

	protected String selectByIdAndTimeQuery() {
		return "SELECT * FROM " + getTableName() + " WHERE id = ? AND startVersion <= ? AND (endVersion > ? OR endVersion IS NULL)";
	}

	@Override
	protected String insertQuery() {
		StringBuilder s = new StringBuilder();

		s.append("INSERT INTO ").append(getTableName()).append(" (");
		for (Object columnNameObject : getColumns().keySet()) {
			// myst, direkt auf columnNames zugreiffen funktioniert hier nicht
			String columnName = (String) columnNameObject;
			s.append(columnName).append(", ");
		}
		s.append("id, startVersion) VALUES (");
		for (int i = 0; i < getColumns().size(); i++) {
			s.append("?, ");
		}
		s.append("?, ?)");

		return s.toString();
	}

	@Override
	protected String updateQuery() {
		return null;
	}

	@Override
	protected String deleteQuery() {
		return "UPDATE " + getTableName() + " SET endVersion = ? WHERE id = ? AND endVersion IS NULL";
	}

	@Override
	protected void addSpecialColumns(SqlDialect dialect, StringBuilder s) {
		s.append(" id ");
		dialect.addColumnDefinition(s, parentIdProperty);
		s.append(",\n startVersion INTEGER NOT NULL");
		s.append(",\n endVersion INTEGER");
	}

}