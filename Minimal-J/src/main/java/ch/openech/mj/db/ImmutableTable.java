package ch.openech.mj.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import ch.openech.mj.db.model.ColumnProperties;
import ch.openech.mj.db.model.PropertyInterface;

/*
 * Idee: Immutables können zwar keine SubTables haben, aber immerhin
 * Referenzen, egal ob wieder Immutables oder auf normale Tabellen
 * 
 * Idee2: Die leeren (frisch instanzierten) Objekte einer Klasse haben immer
 * die Id 0! Diese Objekte werden in der Klasse EmptyObjects verwaltet.
 * 
 */
public class ImmutableTable<T> extends AbstractTable<T> {
	
	protected PreparedStatement selectIdStatement;

	public ImmutableTable(DbPersistence dbPersistence, Class<T> clazz) {
		super(dbPersistence, null, clazz);
		if (clazz.equals(List.class)) {
			throw new IllegalArgumentException();
		}
	}

	@Override
	protected void prepareStatements() throws SQLException {
		super.prepareStatements();
		selectIdStatement = prepareSelectId();
	}
	
	@Override
	public void closeStatements() throws SQLException {
		super.closeStatements();
		selectIdStatement.close();
	}
	
	public int getOrCreateId(T object) throws SQLException {
		if (EmptyObjects.isEmpty(object)) return 0;
		
		Integer id = getId(object);
		if (id == null) {
			id = executeInsertWithAutoIncrement(insertStatement, object);
		}
		return id;
	}
	
	public Integer getId(T object) throws SQLException {
		setParameters(selectIdStatement, object, true, false);
	
		ResultSet resultSet = selectIdStatement.executeQuery();
		Integer result = resultSet.next() ? resultSet.getInt(1) : null;
		resultSet.close();
		
		return result;
	}

	public T selectById(int id) throws SQLException {
		if (id == 0) return EmptyObjects.getEmptyObject(getClazz());
		
		selectByIdStatement.setInt(1, id);
		return executeSelect(selectByIdStatement);
	}

	public void insert(T object) throws SQLException {
		executeInsert(insertStatement, object);
	}
	
	// Statements

	@Override
	protected PreparedStatement prepareSelectById() throws SQLException {
		StringBuilder query = new StringBuilder();
		query.append("SELECT * FROM "); query.append(getTableName()); 
		query.append(" WHERE id = ?");
		return getConnection().prepareStatement(query.toString());
	}
	
	protected String selectId() throws SQLException {
		StringBuilder where = new StringBuilder();
	
		boolean first = true;	
		for (String key : ColumnProperties.getKeys(getClazz())) {
			if (!first) where.append(" AND "); else first = false;
			
			// where.append(column.getName()); where.append(" = ?");
			// doesnt work for null so pattern is:
			// ((? IS NULL AND col1 IS NULL) OR col1 = ?)
			where.append("((? IS NULL AND "); where.append(key); where.append(" IS NULL) OR ");
			where.append(key); where.append(" = ?)");
		}
		
		StringBuilder query = new StringBuilder();
		query.append("SELECT id FROM "); query.append(getTableName()); query.append(" WHERE ");
		query.append(where);
		
		return query.toString();
	}
	
	@Override
	protected PreparedStatement prepareInsert() throws SQLException {
		StringBuilder s = new StringBuilder();
		
		s.append("INSERT INTO "); s.append(getTableName()); s.append(" (");
		Map<String, PropertyInterface> properties = ColumnProperties.getProperties(clazz);
		int size = properties.entrySet().size();
		int i = 0;
		for (String name : properties.keySet()) {
			s.append(name);
			if (i++ < size - 1) s.append(", ");
		}
		s.append(") VALUES (");
		for (int j = 0; j<size; j++) {
			s.append("?");
			if (j < size - 1) s.append(", ");
		}
		s.append(")");
		System.out.println(s.toString());
		
		return getConnection().prepareStatement(s.toString(), Statement.RETURN_GENERATED_KEYS);
	}

}
