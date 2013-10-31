package ch.openech.mj.db;//

import ch.openech.mj.model.PropertyInterface;

public class FulltextIndex<T> extends ColumnIndex<T> {

	protected FulltextIndex(DbPersistence dbPersistence, AbstractTable<T> table, PropertyInterface property, String column) {
		super(dbPersistence, table, property, column, null);
	}

	@Override
	protected String selectQuery() {
		StringBuilder query = new StringBuilder();
		query.append("SELECT id FROM "); query.append(table.getTableName()); 
		query.append(" WHERE "); query.append(column); query.append(" LIKE ?");
		if (table instanceof HistorizedTable) {
			query.append(" AND version = 0");
		}
		return query.toString();
	}

}
