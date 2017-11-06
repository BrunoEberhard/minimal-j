package org.minimalj.repository.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.repository.sql.SqlDialect;
import org.minimalj.repository.sql.SqlRepository;
import org.minimalj.repository.sql.SubTable;
import org.minimalj.repository.sql.AbstractTable.ParameterMode;
import org.minimalj.util.EqualsHelper;
import org.minimalj.util.IdUtils;

/**
 * Minimal-J internal<p>
 * 
 * HistorizedTable has one version columns, HistorizedSubTable has two.
 * One for startVersion and one for endVersion.<p>
 * 
 * For a new Entry there is startVersion and endVersion = 0.<p>
 * 
 * After an update endVersion contains the version from which the
 * entry is <i>not</i> active anymore. The startVersion of the new
 * row contains the version from which the entry is active.
 * 
 */
public class HistorizedSubTable<PARENT, ELEMENT> extends SubTable<PARENT, ELEMENT> {

	protected final String selectByIdAndTimeQuery;
	private final String endQuery;
	
	public HistorizedSubTable(SqlRepository sqlRepository, String prefix, Class<ELEMENT> clazz, PropertyInterface parentIdProperty) {
		super(sqlRepository, prefix, clazz, parentIdProperty);
		
		selectByIdAndTimeQuery = selectByIdAndTimeQuery();
		endQuery = endQuery();
	}
	
	@Override
	public void addList(PARENT parent, List<ELEMENT> objects) {
		int version = 0;
		try (PreparedStatement insertStatement = createStatement(sqlRepository.getConnection(), insertQuery, false)) {
			for (int position = 0; position<objects.size(); position++) {
				ELEMENT object = objects.get(position);
				int parameterPos = setParameters(insertStatement, object, false, ParameterMode.INSERT, IdUtils.getId(parent));
				insertStatement.setInt(parameterPos++, position);
				insertStatement.setInt(parameterPos++, version);
				insertStatement.execute();
			}
		} catch (SQLException x) {
			throw new RuntimeException(x.getMessage());
		}
	}

	public void replaceAll(PARENT parent, List<ELEMENT> objects, int version) {
		int oldVersion = version-1;
		List<ELEMENT> objectsInDb = read(parent, oldVersion);
		Object parentId = IdUtils.getId(parent);
		int position = 0;
		while (position < Math.max(objects.size(), objectsInDb.size())) {
			boolean end = false;
			boolean insert = false;
			if (position < objectsInDb.size() && position < objects.size()) {
				Object object = objects.get(position);
				Object objectInDb = objectsInDb.get(position);
				end = insert = !EqualsHelper.equals(object, objectInDb);
			} else if (position < objectsInDb.size()) {
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
					int parameterPos = setParameters(insertStatement, objects.get(position), false, ParameterMode.HISTORIZE, parentId);
					insertStatement.setInt(parameterPos++, position);
					insertStatement.setInt(parameterPos++, version);
					insertStatement.execute();
				} catch (SQLException x) {
					throw new RuntimeException(x.getMessage());
				}
			}
			position++;
		}
	}

	public List<ELEMENT> read(PARENT parent, Integer time) {
		if (time == null) {
			return getList(parent);
		}
		try (PreparedStatement selectByIdAndTimeStatement = createStatement(sqlRepository.getConnection(), selectByIdAndTimeQuery, false)) {
			selectByIdAndTimeStatement.setObject(1, IdUtils.getId(parent));
			selectByIdAndTimeStatement.setInt(2, time);
			selectByIdAndTimeStatement.setInt(3, time);
			return executeSelectAll(selectByIdAndTimeStatement);
		} catch (SQLException x) {
			throw new RuntimeException(x.getMessage());
		}
	}

	@Override
	public List<ELEMENT> getList(PARENT parent) {
		try (PreparedStatement selectByIdStatement = createStatement(sqlRepository.getConnection(), selectByIdQuery, false)) {
			selectByIdStatement.setObject(1, IdUtils.getId(parent));
			return executeSelectAll(selectByIdStatement);
		} catch (SQLException x) {
			throw new RuntimeException(x.getMessage());
		}
	}
	
	// Queries
	
	protected String selectByIdAndTimeQuery() {
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM ").append(getTableName()); 
		query.append(" WHERE id = ? AND startVersion <= ? AND endVersion > ? ORDER BY position");
		return query.toString();
	}
	
	@Override
	protected String selectByIdQuery() {
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM ").append(getTableName()).append(" WHERE id = ? AND endVersion = " + Integer.MAX_VALUE + " ORDER BY position");
		return query.toString();
	}
	
	@Override
	protected String insertQuery() {
		StringBuilder s = new StringBuilder();
		
		s.append("INSERT INTO ").append(getTableName()).append(" (");
		for (Object columnNameObject : getColumns().keySet()) {
			// myst, direkt auf columnNames zugreiffen funktionert hier nicht
			String columnName = (String) columnNameObject;
			s.append(columnName).append(", ");
		}
		s.append("id, position, startVersion, endVersion) VALUES (");
		for (int i = 0; i<getColumns().keySet().size(); i++) {
			s.append("?, ");
		}
		s.append("?, ?, ?, " + Integer.MAX_VALUE + ")");

		return s.toString();
	}
	
	private String endQuery() {
		StringBuilder s = new StringBuilder();
		s.append("UPDATE ").append(getTableName()).append(" SET endVersion = ? WHERE id = ? AND position = ? AND endVersion = " + Integer.MAX_VALUE);
		return s.toString();
	}
	
	@Override
	protected void addSpecialColumns(SqlDialect dialect, StringBuilder s) {
		s.append(" id ");
		dialect.addColumnDefinition(s, parentIdProperty);
		s.append(",\n startVersion INTEGER NOT NULL");
		s.append(",\n endVersion INTEGER NOT NULL");
		s.append(",\n position INTEGER NOT NULL");
	}
	
	@Override
	protected void addPrimaryKey(SqlDialect dialect, StringBuilder s) {
		dialect.addPrimaryKey(s, "id, startVersion, position");
	}	

}