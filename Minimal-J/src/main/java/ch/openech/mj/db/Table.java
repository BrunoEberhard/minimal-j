package ch.openech.mj.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;
import java.util.logging.Level;

import ch.openech.mj.model.PropertyInterface;
import ch.openech.mj.util.GenericUtils;
import ch.openech.mj.util.StringUtils;

@SuppressWarnings("rawtypes")
public class Table<T> extends AbstractTable<T> {

	protected final String selectByIdQuery;
	protected final String selectAllQuery;
	protected final String updateQuery;
	protected final Map<String, AbstractTable<?>> subTables;
	
	private final Map<Connection, WeakHashMap<Object, Integer>> objectIds = new HashMap<Connection, WeakHashMap<Object, Integer>>();

	public Table(DbPersistence dbPersistence, Class<T> clazz) {
		super(dbPersistence, null, clazz);
		this.subTables = findSubTables();
		
		this.selectByIdQuery = selectByIdQuery();
		this.selectAllQuery = selectAllQuery();
		this.updateQuery = updateQuery();
	}
	
	@Override
	public void create(Connection connection) throws SQLException {
		super.create(connection);
		for (AbstractTable<?> subTable : subTables.values()) {
			subTable.create(connection);
		}
	}

	@Override
	protected ObjectWithId<T> readResultSetRow(ResultSet resultSet, Integer time)
			throws SQLException {
		ObjectWithId<T> resultObject = super.readResultSetRow(resultSet, time);
		Connection connection = resultSet.getStatement().getConnection();
		registerObjectId(connection, resultObject.object, resultObject.id);
		return resultObject;
	}

	protected void registerObjectId(Connection connection, Object object, Integer id) {
		if (!objectIds.containsKey(connection)) {
			objectIds.put(connection, new WeakHashMap<Object, Integer>(2048));
		}
		Map<Object, Integer> objectIdsForConnection = objectIds.get(connection);
		objectIdsForConnection.put(object, Integer.valueOf(id));
	}

	public Integer getId(Connection connection, T object) {
		if (!objectIds.containsKey(connection)) {
			objectIds.put(connection, new WeakHashMap<Object, Integer>(2048));
		}
		Map<Object, Integer> objectIdsForConnection = objectIds.get(connection);
		return objectIdsForConnection.get(object);
	}

	public int insert(T object) {
		return insert(dbPersistence.getAutoCommitConnection(), object);
	}
	
	public int insert(Connection connection, T object) {
		try {
			PreparedStatement insertStatement = getStatement(connection, insertQuery, true);
			int id = executeInsertWithAutoIncrement(insertStatement, object);
			for (Entry<String, AbstractTable<?>> subTableEntry : subTables.entrySet()) {
				SubTable subTable = (SubTable) subTableEntry.getValue();
				List list;
				try {
					list = (List)getLists().get(subTableEntry.getKey()).getValue(object);
					if (list != null && !list.isEmpty()) {
						subTable.insert(connection, id, list);
					}
				} catch (IllegalArgumentException e) {
					throw new RuntimeException(e);
				}
			}
			registerObjectId(connection, object, id);
			return id;
		} catch (SQLException x) {
			sqlLogger.log(Level.SEVERE, "Couldn't insert object into " + getTableName(), x);
			sqlLogger.log(Level.FINE, "Object: " + object);
			throw new RuntimeException("Couldn't insert object into " + getTableName() + " / Object: " + object);
		}
	}

	public void update(T object) {
		update(dbPersistence.getAutoCommitConnection(), object);
	}
	
	public void update(Connection connection, T object) {
		Integer id = getId(connection, object);
		if (id == null) throw new IllegalArgumentException("Not a read object: " + object);
		try {
			update(connection, id.intValue(), object);
		} catch (SQLException x) {
			sqlLogger.log(Level.SEVERE, "Couldn't update object on " + getTableName(), x);
			sqlLogger.log(Level.FINE, "Object: " + object);
			throw new RuntimeException("Couldn't update object on " + getTableName() + " / Object: " + object);
		}
	}
	
	public void clear(Connection connection) {
		for (AbstractTable<?> table : subTables.values()) {
			table.clear(connection);
		}
		super.clear(connection);
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
	
	private void update(Connection connection, int id, T object) throws SQLException {
		PreparedStatement updateStatement = getStatement(connection, updateQuery, false);
		int parameterPos = setParameters(updateStatement, object, false, true);
		helper.setParameterInt(updateStatement, parameterPos++, id);
		updateStatement.execute();
		
		for (Entry<String, AbstractTable<?>> subTableEntry : subTables.entrySet()) {
			SubTable subTable = (SubTable) subTableEntry.getValue();
			List list;
			try {
				list = (List) getLists().get(subTableEntry.getKey()).getValue(object);
			} catch (IllegalArgumentException e) {
				throw new RuntimeException(e);
			}
			subTable.update(connection, id, list);
		}
	}

	public T read(int id) {
		return read(dbPersistence.getAutoCommitConnection(), id);
	}
	
	public T read(Connection connection, int id) {
		if (id < 1) throw new IllegalArgumentException(String.valueOf(id));

		try {
			PreparedStatement selectByIdStatement = getStatement(connection, selectByIdQuery, false);
			selectByIdStatement.setInt(1, id);
			T object = executeSelect(selectByIdStatement);
			if (object != null) {
				loadRelations(connection, object, id);
			}
			return object;
		} catch (SQLException x) {
			sqlLogger.log(Level.SEVERE, "Couldn't read " + getTableName() + " with ID " + id, x);
			throw new RuntimeException("Couldn't read " + getTableName() + " with ID " + id);
		}
	}

	@SuppressWarnings("unchecked")
	private void loadRelations(Connection connection, T object, int id) throws SQLException {
		for (Entry<String, AbstractTable<?>> subTableEntry : subTables.entrySet()) {
			SubTable subTable = (SubTable) subTableEntry.getValue();
			List list = (List) getLists().get(subTableEntry.getKey()).getValue(object);
			list.addAll(subTable.read(connection, id));
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

}
