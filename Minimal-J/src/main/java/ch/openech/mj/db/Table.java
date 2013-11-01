package ch.openech.mj.db;

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

	private PreparedStatement selectById;
	private PreparedStatement selectAllStatement;
	private PreparedStatement updateStatement;
	protected final Map<String, AbstractTable<?>> subTables;
	
	private final WeakHashMap<Object, Integer> objectIds = new WeakHashMap<Object, Integer>(2048);

	public Table(DbPersistence dbPersistence, Class<T> clazz) {
		super(dbPersistence, null, clazz);
		this.subTables = findSubTables();
	}
	
	@Override
	public void initialize() throws SQLException {
		super.initialize();
		selectById = prepare(selectByIdQuery());
		updateStatement = prepare(updateQuery());
		selectAllStatement = prepare(selectAllQuery());
		initializeSubTables();
		initializeIndexes();
	}

	private void initializeSubTables() throws SQLException {
		findSubTables();
		for (AbstractTable<?> table : subTables.values()) {
			table.initialize();
		}
	}

	private void initializeIndexes() throws SQLException {
		// something to do?
	}

	@Override
	public void closeStatements() throws SQLException {
		super.closeStatements();
		selectById.close();
		selectAllStatement.close();
		updateStatement.close();
		
		for (AbstractTable<?> table : subTables.values()) {
			table.closeStatements();
		}
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
				SubTable subTable = (SubTable) subTableEntry.getValue();
				List list;
				try {
					list = (List)getLists().get(subTableEntry.getKey()).getValue(object);
					if (list != null && !list.isEmpty()) {
						subTable.insert(id, list);
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
	
	public void clear() {
		for (AbstractTable<?> table : subTables.values()) {
			table.clear();
		}
		super.clear();
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
	
	private void update(int id, T object) throws SQLException {
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
			subTable.update(id, list);
		}
	}

	public T read(int id) {
		if (id < 1) throw new IllegalArgumentException(String.valueOf(id));

		try {
			selectByIdStatement.setInt(1, id);
			T object = executeSelect(selectByIdStatement);
			if (object != null) {
				loadRelations(object, id);
			}
			return object;
		} catch (SQLException x) {
			sqlLogger.log(Level.SEVERE, "Couldn't read " + getTableName() + " with ID " + id, x);
			throw new RuntimeException("Couldn't read " + getTableName() + " with ID " + id);
		}
	}

	@SuppressWarnings("unchecked")
	private void loadRelations(T object, int id) throws SQLException {
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

}
