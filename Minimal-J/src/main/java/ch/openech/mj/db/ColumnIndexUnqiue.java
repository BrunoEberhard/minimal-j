package ch.openech.mj.db;//

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import ch.openech.mj.model.PropertyInterface;


public class ColumnIndexUnqiue<T> extends AbstractIndex<T> {

	protected ColumnIndexUnqiue<?> innerIndex;
	
	ColumnIndexUnqiue(DbPersistence dbPersistence, AbstractTable<T> table, PropertyInterface property, String column, ColumnIndexUnqiue<?> innerIndex) {
		super(dbPersistence, table, property, column);
		this.innerIndex = innerIndex;
	}
	
	public Integer findId(Object query) {		
		try {
			if (innerIndex != null) {
				query = innerIndex.findId(query);
			}
			helper.setParameter(selectByColumnStatement, 1, query, property);
			return executeSelectId(selectByColumnStatement);
		} catch (SQLException x) {
			String message = "Couldn't use index of column + " + column + " of table " + table.getTableName() + " with query " + query;
			sqlLogger.log(Level.SEVERE, message, x);
			throw new RuntimeException(message);
		}
	}
	

	@Override
	public List<Integer> findIds(Object query) {
		return Collections.singletonList(findId(query));
	}
	
	public T find(Object query) {
		Integer id = findId(query);
		return lookup(id);
	}

	
	@Override
	protected String selectQuery() {
		StringBuilder query = new StringBuilder();
		query.append("SELECT id FROM "); query.append(table.getTableName()); 
		query.append(" WHERE "); query.append(column); query.append(" = ?");
		if (table instanceof HistorizedTable) {
			query.append(" AND version = 0");
		}
		return query.toString();
	}

}
