package ch.openech.mj.server;

import java.util.List;

import ch.openech.mj.criteria.Criteria;
import ch.openech.mj.db.Transaction;

/**
 * Access is defined as annotations on the model classes
 * 
 * @author bruno
 */
public interface DbService {

	public <T> T read(Class<T> clazz, long id);

	public <T> List<T> search(Class<T> clazz, String query, int maxResults);

	public <T> List<T> search(Class<T> clazz, Object[] keys, String query, int maxResults);
	
	public <T> List<T> read(Class<T> clazz, Criteria critera);

	public <T> long insert(T object);

	public <T> void update(T object);

	public <T> void delete(T object);
	
	// @Role(Test)
	public <T> void deleteAll(Class<T> clazz);

	// TODO this is very specific and should be done differently
	public <T> long getMaxId(Class<T> clazz);
	
	// Only for historized tables
	
	public <T> List<T> loadHistory(T object);
	
	public <T> T loadHistory(T object, int time);
	
	//
	
	@Deprecated // TODO replace with annotations on service methods
	public <V> V transaction(Transaction<V> transaction, String description);
}
