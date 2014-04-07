package ch.openech.mj.server;

import java.util.List;
import java.util.Map;

import ch.openech.mj.criteria.Criteria;
import ch.openech.mj.db.Transaction;
import ch.openech.mj.model.Search;

/**
 * Access is defined as annotations on the model classes
 * 
 * @author bruno
 */
public interface DbService {

	public <T> List<T> search(Search<T> index, String query);
	
	public <T> T read(Class<T> clazz, long id);

	public <T> List<T> read(Class<T> clazz, Criteria critera);

	public <T> List<T> read(Class<T> clazz, String whereClause);

	public List<Object[]> read(String query);

	public <T> long insert(T object);

	public <T> void update(T object);

	public <T> void delete(T object);
	
	// @Role(Test)
	public <T> void deleteAll(Class<T> clazz);

	// TODO this is very specific and should be done differently
	public <T> long getMaxId(Class<T> clazz);
	
	// Only for historized tables
	
	public <T> Map<Integer, T> loadHistory(T object);
	
	public <T> T loadHistory(T object, int time);
	
	//
	
	public <V> V transaction(Transaction<V> transaction, String description);
}
