package ch.openech.mj.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import ch.openech.mj.db.model.ColumnAccess;

/**
 * Idee: Eine historisierte Tabelle besitzt ein Feld namens "endVersion". Beim aktuellen
 * Objekt ist dieses Feld null. Bei historisierten Objekte wird dieses Feld hochgezählt,
 * d.h. "1" deutet auf die älteste Version, "2" auf die zweitälteste usw.<p>
 * 
 * Unhistorisierte Tabellen gibt es eigentlich nicht. Alle Tabelle könnten historisiert
 * sein, bei unhistorisierten wird einfach immer nur "correct" aufgerufen und damit
 * bleibt die Versionsspalte immer auf null.<p>
 * 
 */
@SuppressWarnings("rawtypes")
public class Table<T> extends AbstractTable<T> {

	private PreparedStatement selectByIdAndTimeStatement;
	private PreparedStatement updateStatement;
	private PreparedStatement endStatement;
	private PreparedStatement selectMaxVersionStatement;
	private PreparedStatement readVersionsStatement;
	
	private final WeakHashMap<Object, Integer> objectIds = new WeakHashMap<Object, Integer>(2048);

	public Table(DbPersistence dbPersistence, Class<T> clazz) {
		super(dbPersistence, null, clazz);
	}
	
	@Override
	public void initialize() throws SQLException {
		super.initialize();
		selectByIdAndTimeStatement = prepareSelectByIdAndTime();
		endStatement = prepareEnd();
		updateStatement = prepareUpdate();
		selectMaxVersionStatement = prepareSelectMaxVersion();
		readVersionsStatement = prepareReadVersions();
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
	
	public void registerObjectId(Object object, int id) {
		objectIds.put(object, Integer.valueOf(id));
	}

	public Integer getId(Object object) {
		return objectIds.get(object);
	}

//	public Integer getId(T object, int version) throws SQLException {
//		setParameters(selectIdStatement, object, version, true, version, version);
//
//		ResultSet resultSet = selectIdStatement.executeQuery();
//		Integer result = resultSet.next() ? resultSet.getInt(1) : null;
//		resultSet.close();
//		
//		return result;
//	}
	
	public int insert(T object) throws SQLException {
		int id = executeInsertWithAutoIncrement(insertStatement, object);
		for (Entry<String, AbstractTable<?>> subTable : subTables.entrySet()) {
			SubTable historizedSubTable = (SubTable) subTable.getValue();
			List list;
			try {
				list = (List)ColumnAccess.getValue(object, subTable.getKey());
				if (list != null && !list.isEmpty()) {
					historizedSubTable.insert(id, list, Integer.valueOf(0));
				}
			} catch (IllegalArgumentException e) {
				throw new RuntimeException(e);
//			} catch (IllegalAccessException e) {
//				throw new RuntimeException(e);
			}
		}
		registerObjectId(object, id);
		return id;
	}

	public void update(T object) throws SQLException {
		Integer id = getId(object);
		if (id == null) throw new IllegalArgumentException("Not a read object: " + object);
		update(id.intValue(), object);
	}
	
	private void update(int id, T object) throws SQLException {
		// TODO Update sollte erst mal prüfen, ob update nötig ist.
		// T oldObject = read(id);
		// na, ob dann das mit allen subTables noch stimmt??
		// if (ColumnAccess.equals(oldObject, object)) return;
		
		int version = findMaxVersion(id) + 1;
		
		logger.info("EndStatement: version=" + version + " / id=" + id);
		endStatement.setInt(1, version);
		endStatement.setInt(2, id);
		endStatement.execute();	
		
		int parameterPos = setParameters(updateStatement, object, false, true);
		setParameter(updateStatement, parameterPos++, id);
		logger.info("UpdateStatement: id=" + id);
		updateStatement.execute();
		
		for (Entry<String, AbstractTable<?>> subTable : subTables.entrySet()) {
			SubTable historizedSubTable = (SubTable) subTable.getValue();
			List list;
			try {
				list = (List) ColumnAccess.getValue(object, subTable.getKey());
			} catch (IllegalArgumentException e) {
				throw new RuntimeException(e);
			}
			boolean loaded = true;
			if (list instanceof DbList) {
				loaded = ((DbList) list).isLoaded();
			}
			if (loaded) {
				historizedSubTable.update(id, list, version);
			}
		}
	}
	
	private int findMaxVersion(int id) throws SQLException {
		int result = 0;
		selectMaxVersionStatement.setInt(1, id);
		ResultSet resultSet = selectMaxVersionStatement.executeQuery();
		if (resultSet.next()) {
			result = resultSet.getInt(1);
		} 
		resultSet.close();
		return result;
	}

	public T read(int id) throws SQLException {
		selectByIdStatement.setInt(1, id);
		T object = executeSelect(selectByIdStatement);
		if (object != null) {
			loadRelations(object, id, null);
			registerObjectId(object, id);
		}
		return object;
	}

	public T read(int id, Integer time) throws SQLException {
		if (time != null) {
			selectByIdAndTimeStatement.setInt(1, id);
			selectByIdAndTimeStatement.setInt(2, time);
			T object = executeSelect(selectByIdAndTimeStatement);
			loadRelations(object, id, time);
			// note: object is not registered, because its an old version
			// and cannot be updated
			return object;
		} else {
			return read(id);
		}
	}

	@SuppressWarnings("unchecked")
	private void loadRelations(T object, int id, Integer time) throws SQLException {
		for (Entry<String, AbstractTable<?>> subTable : subTables.entrySet()) {
			SubTable historizedSubTable = (SubTable) subTable.getValue();
			// DbList list = new DbList(historizedSubTable, id, time);
			try {
				List list = (List)ColumnAccess.getValue(object, subTable.getKey());
				list.addAll(historizedSubTable.read(id, time));
//				subTable.getKey().set(object, list);
			} catch (IllegalArgumentException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public List<Integer> readVersions(int id) throws SQLException {
		List<Integer> result = new ArrayList<Integer>();

		readVersionsStatement.setInt(1, id);
		ResultSet resultSet = readVersionsStatement.executeQuery();
		while (resultSet.next()) {
			int version = resultSet.getInt(1);
			if (!result.contains(version)) result.add(version);
		}
		resultSet.close();
		
		for (Entry<String, AbstractTable<?>> subTable : subTables.entrySet()) {
			SubTable historizedSubTable = (SubTable) subTable.getValue();
			historizedSubTable.readVersions(id, result);
		}
		
		result.remove(Integer.valueOf(0));
		Collections.sort(result);
		return result;
	}
	
	
	// Statements

	@Override
	protected PreparedStatement prepareSelectById() throws SQLException {
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM "); query.append(getTableName()); 
		query.append(" WHERE id = ? AND version IS NULL");
		return getConnection().prepareStatement(query.toString());
	}
	
	protected PreparedStatement prepareSelectByIdAndTime() throws SQLException {
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM "); query.append(getTableName()); 
		query.append(" WHERE id = ? AND version = ?");
		return getConnection().prepareStatement(query.toString());
	}
	
	@Override
	protected PreparedStatement prepareInsert() throws SQLException {
		StringBuilder s = new StringBuilder();
		
		s.append("INSERT INTO "); s.append(getTableName()); s.append(" (");
		for (String columnName : columnNames) {
			s.append(columnName);
			s.append(", ");
		}
		s.append("version) VALUES (");
		for (int i = 0; i<columnNames.size(); i++) {
			s.append("?, ");
		}
		s.append("null)");

		return getConnection().prepareStatement(s.toString(), Statement.RETURN_GENERATED_KEYS);
	}
	
	protected PreparedStatement prepareUpdate() throws SQLException {
		StringBuilder s = new StringBuilder();
		
		s.append("INSERT INTO "); s.append(getTableName()); s.append(" (");
		for (String name : columnNames) {
			s.append(name);
			s.append(", ");
		}
		s.append("id, version) VALUES (");
		for (int i = 0; i<columnNames.size(); i++) {
			s.append("?, ");
		}
		s.append("?, null)");

		return getConnection().prepareStatement(s.toString());
	}

	protected PreparedStatement prepareSelectMaxVersion() throws SQLException {
		StringBuilder s = new StringBuilder();
		
		s.append("SELECT MAX(version) FROM "); s.append(getTableName()); 
		s.append(" WHERE id = ?");

		return getConnection().prepareStatement(s.toString());
	}
	
	protected PreparedStatement prepareEnd() throws SQLException {
		StringBuilder s = new StringBuilder();
		s.append("UPDATE "); s.append(getTableName()); s.append(" SET version = ? WHERE id = ? AND version IS NULL");
		return getConnection().prepareStatement(s.toString());
	}
	
	protected PreparedStatement prepareReadVersions() throws SQLException {
		StringBuilder s = new StringBuilder();
		
		s.append("SELECT version FROM "); s.append(getTableName()); 
		s.append(" WHERE version IS NOT NULL AND id = ?");

		return getConnection().prepareStatement(s.toString());
	}

}
