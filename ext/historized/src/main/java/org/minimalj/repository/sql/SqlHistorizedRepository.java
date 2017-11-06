package org.minimalj.repository.sql;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.minimalj.model.Model;
import org.minimalj.model.View;
import org.minimalj.model.ViewUtil;
import org.minimalj.util.CloneHelper;
import org.minimalj.util.FieldUtils;

public class SqlHistorizedRepository extends SqlRepository {

	public SqlHistorizedRepository(DataSource dataSource, boolean createTablesOnInitialize, Class<?>... classes) {
		super(dataSource, createTablesOnInitialize, classes);
	}

	public SqlHistorizedRepository(DataSource dataSource, boolean createTablesOnInitialize, Model model) {
		super(dataSource, createTablesOnInitialize, model);
	}

	public SqlHistorizedRepository(DataSource dataSource, Class<?>... classes) {
		super(dataSource, classes);
	}

	public SqlHistorizedRepository(DataSource dataSource, Model model) {
		super(dataSource, model);
	}

	@Override
	<U> Table<U> createTable(Class<U> clazz) {
		boolean historized = FieldUtils.hasValidHistorizedField(clazz);
		if (historized) {
			return new HistorizedTable<>(this, clazz);
		} else {
			return super.createTable(clazz);
		}
	}
	
	public <T> List<T> loadHistory(Class<?> clazz, Object id, int maxResult) {
		@SuppressWarnings("unchecked")
		Table<T> table = (Table<T>) getTable(clazz);
		if (table instanceof HistorizedTable) {
			HistorizedTable<T> historizedTable = (HistorizedTable<T>) table;
			int maxVersion = historizedTable.getMaxVersion(id);
			int maxResults = Math.min(maxVersion + 1, maxResult);
			List<T> result = new ArrayList<>(maxResults);
			for (int i = 0; i<maxResults; i++) {
				result.add(historizedTable.read(id, maxVersion - i));
			}
			return result;
		} else {
			throw new IllegalArgumentException(clazz.getSimpleName() + " is not historized");
		}
	}

	public <T> T readVersion(Class<T> clazz, Object id, int time) {
		HistorizedTable<T> table = (HistorizedTable<T>) getTable(ViewUtil.resolve(clazz));
		T result = table.read(id, time);
		if (View.class.isAssignableFrom(clazz)) {
			// TODO Historized views are not optimized for read by id and time.
			// The complete object is read and reduced to view.
			// Should not cost too much performance as it's only one entity.
			return ViewUtil.view(result, CloneHelper.newInstance(clazz));
		} else {
			return result;
		}
	}
}
