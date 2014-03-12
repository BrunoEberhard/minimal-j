package ch.openech.mj.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ch.openech.mj.model.PropertyInterface;
import ch.openech.mj.util.GenericUtils;
import ch.openech.mj.util.IdUtils;
import ch.openech.mj.util.LoggingRuntimeException;
import ch.openech.mj.util.StringUtils;

@SuppressWarnings("rawtypes")
public class Table<T> extends AbstractTable<T> {

	protected final String selectByIdQuery;
	protected final String selectAllQuery;
	protected final String updateQuery;
	protected final String deleteQuery;
	protected final Map<String, AbstractTable<?>> subTables;
	
	public Table(DbPersistence dbPersistence, Class<T> clazz) {
		super(dbPersistence, null, clazz);
		this.subTables = findSubTables();
		
		this.selectByIdQuery = selectByIdQuery();
		this.selectAllQuery = selectAllQuery();
		this.updateQuery = updateQuery();
		this.deleteQuery = deleteQuery();
	}
	
	@Override
	public void create() throws SQLException {
		super.create();
		for (AbstractTable<?> subTable : subTables.values()) {
			subTable.create();
		}
	}

	public final long insert(T object) {
		Long newId = dbPersistence.transaction(new InsertTransaction(object), "Insert object in " + getTableName() + " / Object: " + object);
		IdUtils.setId(object, newId);
		return newId;
	}
	
	private class InsertTransaction implements Transaction<Long> {
		private final T object;
		
		public InsertTransaction(T object) {
			this.object = object;
		}

		@Override
		public Long execute() {
			try {
				return doInsert(object);
			} catch (SQLException x) {
				throw new LoggingRuntimeException(x, sqlLogger, "Couldn't insert in " + getTableName() + " with " + object);
			}
		}
	}

	long doInsert(T object) throws SQLException {
		PreparedStatement insertStatement = getStatement(dbPersistence.getConnection(), insertQuery, true);
		long id = executeInsertWithAutoIncrement(insertStatement, object);
		for (Entry<String, AbstractTable<?>> subTableEntry : subTables.entrySet()) {
			SubTable subTable = (SubTable) subTableEntry.getValue();
			List list;
			try {
				list = (List) getLists().get(subTableEntry.getKey()).getValue(object);
				if (list != null && !list.isEmpty()) {
					subTable.insert(id, list);
				}
			} catch (IllegalArgumentException e) {
				throw new RuntimeException(e);
			}
		}
		return id;
	}

	public void update(T object) {
		long id = IdUtils.getId(object);
		dbPersistence.transaction(new UpdateTransaction(id, object), "Update object on " + getTableName() + " / Object: " + object);
	}

	public void delete(T object) {
		long id = IdUtils.getId(object);
		PreparedStatement updateStatement;
		try {
			updateStatement = getStatement(dbPersistence.getConnection(), deleteQuery, false);
			updateStatement.setLong(1, id);
			updateStatement.execute();
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, sqlLogger, "Couldn't delete " + getTableName() + " with ID " + id);
		}
	}

	private Map<String, AbstractTable<?>> findSubTables() {
		Map<String, AbstractTable<?>> subTables = new HashMap<String, AbstractTable<?>>();
		Map<String, PropertyInterface> properties = getLists();
		for (PropertyInterface property : properties.values()) {
			Class<?> clazz = GenericUtils.getGenericClass(property.getType());
			subTables.put(property.getFieldName(), createSubTable(property, clazz));
		}
		return subTables;
	}

	AbstractTable createSubTable(PropertyInterface property, Class<?> clazz) {
		return new SubTable(dbPersistence, buildSubTableName(property), clazz);
	}

	protected String buildSubTableName(PropertyInterface property) {
		StringBuilder b = new StringBuilder();
		b.append(getTableName());
		String fieldName = StringUtils.upperFirstChar(property.getFieldName());
		b.append('_'); b.append(fieldName); 
		return b.toString();
	}
	
	class UpdateTransaction implements Transaction<Void> {
		private final long id;
		private final T object;
		
		public UpdateTransaction(long id, T object) {
			super();
			this.id = id;
			this.object = object;
		}

		@Override
		public Void execute() {
			try {
				return doUpdate(id, object);
			} catch (SQLException x) {
				throw new LoggingRuntimeException(x, sqlLogger, "Couldn't update in " + getTableName() + " with " + object);
			}
		}
	}

	Void doUpdate(long id, T object) throws SQLException {
		PreparedStatement updateStatement = getStatement(dbPersistence.getConnection(), updateQuery, false);
		int parameterPos = setParameters(updateStatement, object, false, true);
		updateStatement.setLong(parameterPos++, id);
		updateStatement.execute();
		
		for (Entry<String, AbstractTable<?>> subTableEntry : subTables.entrySet()) {
			SubTable subTable = (SubTable) subTableEntry.getValue();
			List list;
			try {
				list = (List) getLists().get(subTableEntry.getKey()).getValue(object);
			} catch (IllegalArgumentException e) {
				throw new RuntimeException(e);
			}
			subTable.update(id, list);
		}
		return null;
	}

	public T read(long id) {
		try {
			PreparedStatement selectByIdStatement = getStatement(dbPersistence.getConnection(), selectByIdQuery, false);
			selectByIdStatement.setLong(1, id);
			T object = executeSelect(selectByIdStatement);
			if (object != null) {
				loadRelations(object, id);
			}
			return object;
		} catch (SQLException x) {
			throw new LoggingRuntimeException(x, sqlLogger, "Couldn't read " + getTableName() + " with ID " + id);
		}
	}

	@SuppressWarnings("unchecked")
	private void loadRelations(T object, long id) throws SQLException {
		for (Entry<String, AbstractTable<?>> subTableEntry : subTables.entrySet()) {
			SubTable subTable = (SubTable) subTableEntry.getValue();
			List list = (List) getLists().get(subTableEntry.getKey()).getValue(object);
			list.addAll(subTable.read(id));
		}
	}
	
	// Statements

	@Override
	protected String selectByIdQuery() {
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM "); query.append(getTableName()); 
		query.append(" WHERE id = ?");
		return query.toString();
	}
	
	protected String selectAllQuery() {
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM "); query.append(getTableName()); 
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
		s.delete(s.length()-2, s.length());
		s.append(") VALUES (");
		for (int i = 0; i<getColumns().size(); i++) {
			s.append("?, ");
		}
		s.delete(s.length()-2, s.length());
		s.append(")");

		return s.toString();
	}
	
	protected String updateQuery() {
		StringBuilder s = new StringBuilder();
		
		s.append("UPDATE "); s.append(getTableName()); s.append(" SET ");
		for (Object columnNameObject : getColumns().keySet()) {
			s.append((String) columnNameObject);
			s.append("= ?, ");
		}
		s.delete(s.length()-2, s.length());
		s.append(" WHERE id = ?");

		return s.toString();
	}
	
	protected String deleteQuery() {
		StringBuilder s = new StringBuilder();
		s.append("DELETE FROM "); s.append(getTableName()); s.append(" WHERE id = ?");
		return s.toString();
	}

}
