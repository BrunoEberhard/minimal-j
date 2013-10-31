package ch.openech.mj.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.WeakHashMap;
import java.util.logging.Level;

import ch.openech.mj.model.PropertyInterface;

/**
 * Minimal-J internal<p>
 *
 * A HistorizedTable contains a column named version. In the actual valid row this
 * column is set to 0. After updates the row with the version 1 is the oldest row
 * the row with version 2 the second oldest and so on.<p>
 * 
 */
@SuppressWarnings("rawtypes")
public class HistorizedTable<T> extends Table<T> {

	private PreparedStatement selectByIdAndTimeStatement;
	private PreparedStatement updateStatement;
	private PreparedStatement endStatement;
	private PreparedStatement selectMaxVersionStatement;
	private PreparedStatement readVersionsStatement;
	
	private final WeakHashMap<Object, Integer> objectIds = new WeakHashMap<Object, Integer>(2048);

	public HistorizedTable(DbPersistence dbPersistence, Class<T> clazz) {
		super(dbPersistence, clazz);
	}
	
	@Override
	public void initialize() throws SQLException {
		super.initialize();
		selectByIdAndTimeStatement = prepare(selectByIdAndTimeQuery());
		endStatement = prepare(endQuery());
		updateStatement = prepare(updateQuery());
		selectMaxVersionStatement = prepare(selectMaxVersionQuery());
		readVersionsStatement = prepare(readVersionsQuery());
	}

	@Override
	public void closeStatements() throws SQLException {
		super.closeStatements();
		selectByIdAndTimeStatement.close();
		updateStatement.close();
		endStatement.close();
		selectMaxVersionStatement.close();
		readVersionsStatement.close();
	}
	
	@Override
	protected ObjectWithId<T> readResultSetRow(ResultSet resultSet, Integer time)
			throws SQLException {
		ObjectWithId<T> resultObject = super.readResultSetRow(resultSet, time);
		registerObjectId(resultObject.object, resultObject.id);
		return resultObject;
	}

	private void registerObjectId(Object object, Integer id) {
		objectIds.put(object, Integer.valueOf(id));
	}

	public Integer getId(T object) {
		return objectIds.get(object);
	}

	public int insert(T object) {
		try {
			int id = executeInsertWithAutoIncrement(insertStatement, object);
			for (Entry<String, AbstractTable<?>> subTableEntry : subTables.entrySet()) {
				HistorizedSubTable historizedSubTable = (HistorizedSubTable) subTableEntry.getValue();
				List list;
				try {
					list = (List)getLists().get(subTableEntry.getKey()).getValue(object);
					if (list != null && !list.isEmpty()) {
						historizedSubTable.insert(id, list, Integer.valueOf(0));
					}
				} catch (IllegalArgumentException e) {
					throw new RuntimeException(e);
				}
			}
			registerObjectId(object, id);
			return id;
		} catch (SQLException x) {
			sqlLogger.log(Level.SEVERE, "Couldn't insert object into " + getTableName(), x);
			sqlLogger.log(Level.FINE, "Object: " + object);
			throw new RuntimeException("Couldn't insert object into " + getTableName() + " / Object: " + object);
		}
	}

	AbstractTable createSubTable(PropertyInterface property, Class<?> clazz) {
		return new HistorizedSubTable(dbPersistence, buildSubTableName(property), clazz);
	}
	
	public void update(T object) {
		Integer id = getId(object);
		if (id == null) throw new IllegalArgumentException("Not a read object: " + object);
		try {
			update(id.intValue(), object);
		} catch (SQLException x) {
			sqlLogger.log(Level.SEVERE, "Couldn't update object on " + getTableName(), x);
			sqlLogger.log(Level.FINE, "Object: " + object);
			throw new RuntimeException("Couldn't update object on " + getTableName() + " / Object: " + object);
		}
	}
	
	private void update(int id, T object) throws SQLException {
		// TODO Update sollte erst mal prüfen, ob update nötig ist.
		// T oldObject = read(id);
		// na, ob dann das mit allen subTables noch stimmt??
		// if (ColumnAccess.equals(oldObject, object)) return;
		
		int version = findMaxVersion(id) + 1;
		
		endStatement.setInt(1, version);
		endStatement.setInt(2, id);
		endStatement.execute();	
		
		int parameterPos = setParameters(updateStatement, object, false, true);
		helper.setParameterInt(updateStatement, parameterPos++, id);
		updateStatement.execute();
		
		for (Entry<String, AbstractTable<?>> subTable : subTables.entrySet()) {
			HistorizedSubTable historizedSubTable = (HistorizedSubTable) subTable.getValue();
			List list;
			try {
				list = (List) getLists().get(subTable.getKey()).getValue(object);
			} catch (IllegalArgumentException e) {
				throw new RuntimeException(e);
			}
			historizedSubTable.update(id, list, version);
		}
	}
	
	private int findMaxVersion(int id) throws SQLException {
		int result = 0;
		selectMaxVersionStatement.setInt(1, id);
		try (ResultSet resultSet = selectMaxVersionStatement.executeQuery()) {
			if (resultSet.next()) {
				result = resultSet.getInt(1);
			} 
			return result;
		}
	}

	public T read(int id) {
		if (id < 1) throw new IllegalArgumentException(String.valueOf(id));

		try {
			selectByIdStatement.setInt(1, id);
			T object = executeSelect(selectByIdStatement);
			if (object != null) {
				loadRelations(object, id, null);
			}
			return object;
		} catch (SQLException x) {
			sqlLogger.log(Level.SEVERE, "Couldn't read " + getTableName() + " with ID " + id, x);
			throw new RuntimeException("Couldn't read " + getTableName() + " with ID " + id);
		}
	}

	public T read(int id, Integer time) {
		if (id < 1) throw new IllegalArgumentException(String.valueOf(id));
		
		if (time != null) {
			try {
				selectByIdAndTimeStatement.setInt(1, id);
				selectByIdAndTimeStatement.setInt(2, time);
				T object = executeSelect(selectByIdAndTimeStatement);
				loadRelations(object, id, time);
				// note: object is not registered, because its an old version
				// and cannot be updated
				return object;
			} catch (SQLException x) {
				sqlLogger.log(Level.SEVERE, "Couldn't read " + getTableName() + " with ID " + id + " on time " +  time, x);
				throw new RuntimeException("Couldn't read " + getTableName() + " with ID " + id + " on time " +  time);
			}
		} else {
			return read(id);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void loadRelations(T object, int id, Integer time) throws SQLException {
		for (Entry<String, AbstractTable<?>> subTableEntry : subTables.entrySet()) {
			HistorizedSubTable historizedSubTable = (HistorizedSubTable) subTableEntry.getValue();
			List list = (List)getLists().get(subTableEntry.getKey()).getValue(object);
			list.addAll(historizedSubTable.read(id, time));
		}
	}
	
	public List<Integer> readVersions(int id) {
		try {
			List<Integer> result = new ArrayList<Integer>();
			
			readVersionsStatement.setInt(1, id);
			ResultSet resultSet = readVersionsStatement.executeQuery();
			while (resultSet.next()) {
				int version = resultSet.getInt(1);
				if (!result.contains(version)) result.add(version);
			}
			resultSet.close();
			
			for (Entry<String, AbstractTable<?>> subTable : subTables.entrySet()) {
				HistorizedSubTable historizedSubTable = (HistorizedSubTable) subTable.getValue();
				historizedSubTable.readVersions(id, result);
			}
			
			result.remove(Integer.valueOf(0));
			Collections.sort(result);
			return result;
		} catch (SQLException x) {
			sqlLogger.log(Level.SEVERE, "Couldn't read version of " + getTableName() + " with ID " + id, x);
			throw new RuntimeException("Couldn't read version of " + getTableName() + " with ID " + id);
		}
	}
	
	
	// Statements

	@Override
	protected String selectByIdQuery() {
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM "); query.append(getTableName()); 
		query.append(" WHERE id = ? AND version = 0");
		return query.toString();
	}
	
	@Override
	protected String selectAllQuery() {
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM "); query.append(getTableName()); 
		query.append(" WHERE version = 0");
		return query.toString();
	}
	
	protected String selectByIdAndTimeQuery() {
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM "); query.append(getTableName()); 
		query.append(" WHERE id = ? AND version = ?");
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
		s.append("version) VALUES (");
		for (int i = 0; i<getColumns().size(); i++) {
			s.append("?, ");
		}
		s.append("0)");

		return s.toString();
	}
	
	protected String updateQuery() {
		StringBuilder s = new StringBuilder();
		
		s.append("INSERT INTO "); s.append(getTableName()); s.append(" (");
		for (String name : getColumns().keySet()) {
			s.append(name);
			s.append(", ");
		}
		s.append("id, version) VALUES (");
		for (int i = 0; i<getColumns().size(); i++) {
			s.append("?, ");
		}
		s.append("?, 0)");

		return s.toString();
	}

	private String selectMaxVersionQuery() {
		StringBuilder s = new StringBuilder();
		
		s.append("SELECT MAX(version) FROM "); s.append(getTableName()); 
		s.append(" WHERE id = ?");

		return s.toString();
	}
	
	private String endQuery() {
		StringBuilder s = new StringBuilder();
		s.append("UPDATE "); s.append(getTableName()); s.append(" SET version = ? WHERE id = ? AND version = 0");
		return s.toString();
	}
	
	private String readVersionsQuery() {
		StringBuilder s = new StringBuilder();
		
		s.append("SELECT version FROM "); s.append(getTableName()); 
		s.append(" WHERE id = ?");

		return s.toString();
	}

}
