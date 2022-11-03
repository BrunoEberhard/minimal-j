package org.minimalj.repository.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.EqualsHelper;
import org.minimalj.util.LoggingRuntimeException;

/**
 * Minimal-J internal
 * 
 * - In this tables the parentId is used as id 
 * - has no sub tables
 */
public class DependableTable<PARENT, ELEMENT> extends AbstractTable<ELEMENT> {

	protected final PropertyInterface parentIdProperty;

	public DependableTable(SqlRepository sqlRepository, String name, Class<ELEMENT> clazz, PropertyInterface parentIdProperty) {
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

	public ELEMENT read(Object parentId, Integer version) {
		try (PreparedStatement selectByIdStatement = createStatement(sqlRepository.getConnection(), selectByIdQuery, false)) {
			selectByIdStatement.setObject(1, parentId);
			ELEMENT object = executeSelect(selectByIdStatement);
			return object;
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, sqlLogger, "Couldn't read " + getTableName() + " with ID " + parentId);
		}
	}

	protected void update(Object parentId, ELEMENT object, Integer version) {
		if (object == null) {
			delete(parentId, version);
		} else {
			ELEMENT existing = read(parentId, version);
			if (existing == null) {
				insert(parentId, object, version);
			} else if (!EqualsHelper.equals(existing, object)) {
				doUpdate(parentId, object, version);
			}
		}
	}

	protected void doUpdate(Object parentId, ELEMENT object, Integer version) {
		try (PreparedStatement updateStatement = createStatement(sqlRepository.getConnection(), updateQuery, false)) {
			setParameters(updateStatement, object, ParameterMode.UPDATE, parentId);
			updateStatement.execute();
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}
	
	protected void insert(Object parentId, ELEMENT object, Integer version) {
		try (PreparedStatement insertStatement = createStatement(sqlRepository.getConnection(), insertQuery, false)) {
			setParameters(insertStatement, object, ParameterMode.INSERT, parentId);
			insertStatement.execute();
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	protected void delete(Object parentId, Integer version) {
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
		return "SELECT * FROM " + getTableName() + " WHERE id = ?";
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
		s.append("id) VALUES (");
		for (int i = 0; i < getColumns().size(); i++) {
			s.append("?, ");
		}
		s.append("?)");

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
	}

}