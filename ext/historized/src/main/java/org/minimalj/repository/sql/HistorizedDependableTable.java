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
public class HistorizedDependableTable<PARENT, ELEMENT> extends AbstractTable<ELEMENT> {

	protected final PropertyInterface parentIdProperty;

	public HistorizedDependableTable(SqlRepository sqlRepository, String name, Class<ELEMENT> clazz, PropertyInterface parentIdProperty) {
		super(sqlRepository, name, clazz);

		this.parentIdProperty = parentIdProperty;
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

	public ELEMENT read(Object parentId) {
		try (PreparedStatement selectByIdStatement = createStatement(sqlRepository.getConnection(), selectByIdQuery, false)) {
			selectByIdStatement.setObject(1, parentId);
			ELEMENT object = executeSelect(selectByIdStatement);
			return object;
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, sqlLogger, "Couldn't read " + getTableName() + " with ID " + parentId);
		}
	}

	protected void update(Object parentId, ELEMENT object) {
		try (PreparedStatement updateStatement = createStatement(sqlRepository.getConnection(), updateQuery, false)) {
			setParameters(updateStatement, object, ParameterMode.UPDATE, parentId);
			updateStatement.execute();
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	protected void insert(Object parentId, ELEMENT object) {
		try (PreparedStatement insertStatement = createStatement(sqlRepository.getConnection(), insertQuery, false)) {
			setParameters(insertStatement, object, ParameterMode.INSERT, parentId);
			insertStatement.execute();
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	protected void delete(Object parentId) {
		try (PreparedStatement deleteStatement = createStatement(sqlRepository.getConnection(), deleteQuery, false)) {
			deleteStatement.setObject(1, parentId);
			deleteStatement.execute();
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	// Queries

	@Override
	protected String selectByIdQuery() {
		return "SELECT * FROM " + getTableName() + " WHERE id = ?, version = ?";
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
		s.append("id, version) VALUES (");
		for (int i = 0; i < getColumns().size(); i++) {
			s.append("?, ");
		}
		s.append("?, ?)");

		return s.toString();
	}

	@Override
	protected String updateQuery() {
		StringBuilder s = new StringBuilder();

		s.append("UPDATE ").append(getTableName()).append(" SET ");
		for (Object columnNameObject : getColumns().keySet()) {
			s.append((String) columnNameObject).append("= ?, ");
		}
		s.delete(s.length() - 2, s.length());
		s.append(" WHERE id = ?");

		return s.toString();
	}

	@Override
	protected String deleteQuery() {
		return "DELETE FROM " + getTableName() + " WHERE id = ?";
	}

	@Override
	protected void addSpecialColumns(SqlDialect dialect, StringBuilder s) {
		s.append(" id ");
		dialect.addColumnDefinition(s, parentIdProperty);
		s.append(",\n startVersion INTEGER NOT NULL");
		s.append(",\n endVersion INTEGER NOT NULL");
	}

}