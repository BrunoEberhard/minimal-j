package ch.openech.mj.server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.openech.mj.application.MjApplication;
import ch.openech.mj.criteria.Criteria;
import ch.openech.mj.db.AbstractTable;
import ch.openech.mj.db.DbPersistence;
import ch.openech.mj.db.HistorizedTable;
import ch.openech.mj.db.Table;
import ch.openech.mj.db.Transaction;
import ch.openech.mj.model.Search;
import ch.openech.mj.util.IdUtils;

/**
 * Access is defined as annotations on the model classes
 * 
 * @author bruno
 */
public class DbServiceImpl implements DbService {

	private static DbPersistence persistence;
	
	public DbServiceImpl() {
		if (persistence == null) {
			persistence = new DbPersistence(DbPersistence.embeddedDataSource(), MjApplication.getApplication().getEntityClasses());
		}
	}
	
	@Override
	public <T> long insert(T object) {
		return persistence.insert(object);
	}

	@Override
	public <T> void update(T object) {
		persistence.update(object);
	}

	@Override
	public <T> void delete(T object) {
		persistence.delete(object);
	}

	@Override
	public <T> void deleteAll(Class<T> clazz) {
		persistence.getTable(clazz).clear();
	}

	@Override
	public <T> List<T> search(Search<T> search, String query) {
		return persistence.getTable(search.getClazz()).search(search, query);
	}

	@Override
	public <T> T read(Class<T> clazz, long id) {
		return ((Table<T>) persistence.getTable(clazz)).read(id);
	}
	
	@Override
	public <T> List<T> read(Class<T> clazz, Criteria critera) {
		return null;
	}

	@Override
	public <T> List<T> read(Class<T> clazz, String whereClause) {
		return null;
	}

	@Override
	public List<Object[]> read(String query) {
		return null;
	}

	@Override
	public <T> T loadHistory(T object, int time) {
		@SuppressWarnings("unchecked")
		AbstractTable<T> abstractTable = (AbstractTable<T>) persistence.getTable(object.getClass());
		if (abstractTable instanceof HistorizedTable) {
			long id = IdUtils.getId(object);
			return ((HistorizedTable<T>) abstractTable).read(id, time);
		} else {
			throw new IllegalArgumentException(object.getClass() + " is not historized");
		}
	}

	@Override
	public <T> Map<Integer, T> loadHistory(T object) {
		@SuppressWarnings("unchecked")
		AbstractTable<T> abstractTable = (AbstractTable<T>) persistence.getTable(object.getClass());
		if (abstractTable instanceof HistorizedTable) {
			long id = IdUtils.getId(object);
			List<Integer> times = ((HistorizedTable<T>) abstractTable).readVersions(id);
			Map<Integer, T> result = new HashMap<>();
			for (int time : times) {	
				result.put(time, ((HistorizedTable<T>) abstractTable).read(id, time));
			}
			return result;
		} else {
			throw new IllegalArgumentException(object.getClass() + " is not historized");
		}
	}

	@Override
	public <V> V transaction(Transaction<V> transaction, String description) {
		return persistence.transaction(transaction, description);
	}

	@Override
	public <T> long getMaxId(Class<T> clazz) {
		return ((Table<T>) persistence.getTable(clazz)).getMaxId();
	}

}
