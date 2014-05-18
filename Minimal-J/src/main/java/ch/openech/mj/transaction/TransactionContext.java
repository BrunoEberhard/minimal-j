package ch.openech.mj.transaction;

import java.util.List;

import ch.openech.mj.criteria.Criteria;

public interface TransactionContext {

	public <T> T read(Class<T> clazz, long id);
	public <T> List<T> search(Class<T> clazz, String query, int maxResults);
	public <T> List<T> search(Class<T> clazz, Object[] keys, String query, int maxResults);
	public <T> List<T> read(Class<T> clazz, Criteria criteria);

	public <T> long insert(T object);
	public <T> void update(T object);
	public <T> void delete(T object);
	public <T> void deleteAll(Class<T> clazz);

	// Only for historized tables
	public <T> List<T> loadHistory(T object);
	public <T> T read(Class<T> clazz, long id, Integer time);
	
	public Object executeStatement(String query);
}
