package ch.openech.mj.server;

import java.util.ArrayList;
import java.util.List;

import ch.openech.mj.application.MjApplication;
import ch.openech.mj.criteria.Criteria;
import ch.openech.mj.db.AbstractTable;
import ch.openech.mj.db.DbPersistence;
import ch.openech.mj.db.DbPersistenceHelper;
import ch.openech.mj.db.HistorizedTable;
import ch.openech.mj.db.Table;
import ch.openech.mj.db.Transaction;
import ch.openech.mj.model.annotation.ViewOf;
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
	public <T> List<T> search(Class<T> resultClass, String query, int maxResults) {
		if (ViewOf.class.isAssignableFrom(resultClass)) {
			Class<?> viewedClass = DbPersistenceHelper.getViewedClass(resultClass);
			return ((Table) persistence.getTable(viewedClass)).search(resultClass, query, maxResults);
		} else {
			return ((Table) persistence.getTable(resultClass)).search(query, maxResults);
		}
	}

	@Override
	public <T> List<T> search(Class<T> resultClass, Object[] fields, String query, int maxResults) {
		if (ViewOf.class.isAssignableFrom(resultClass)) {
			Class<?> viewedClass = DbPersistenceHelper.getViewedClass(resultClass);
			return ((Table) persistence.getTable(viewedClass)).search(resultClass, fields, query, maxResults);
		} else {
			return ((Table) persistence.getTable(resultClass)).search(fields, query, maxResults);
		}
	}

	@Override
	public <T> T read(Class<T> clazz, long id) {
		return ((Table<T>) persistence.getTable(clazz)).read(id);
	}
	
	@Override
	public <T> List<T> read(Class<T> clazz, Criteria criteria) {
		return ((Table<T>) persistence.getTable(clazz)).read(criteria);
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
	public <T> List<T> loadHistory(T object) {
		@SuppressWarnings("unchecked")
		AbstractTable<T> abstractTable = (AbstractTable<T>) persistence.getTable(object.getClass());
		if (abstractTable instanceof HistorizedTable) {
			long id = IdUtils.getId(object);
			List<Integer> times = ((HistorizedTable<T>) abstractTable).readVersions(id);
			List<T> result = new ArrayList<>();
			for (int time : times) {	
				result.add(((HistorizedTable<T>) abstractTable).read(id, time));
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
