package ch.openech.mj.backend;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;

import ch.openech.mj.criteria.Criteria;
import ch.openech.mj.db.DbBackend;
import ch.openech.mj.transaction.StreamConsumer;
import ch.openech.mj.transaction.StreamProducer;
import ch.openech.mj.transaction.Transaction;

/**
 * The backend can be in same VM as the frontend or it can
 * be on a remote server.<p>
 * 
 * @author bruno
 *
 */
public abstract class Backend {

	static Backend instance;
	
	public static void configureSocketBackend(String backendAddress, int port) {
		instance = new SocketBackend(backendAddress, port);
	}

	public static void configureLocal() {
		// TODO make this configurable
		instance = new DbBackend();
	}
	
	public static Backend getInstance() {
		return instance;
	}

	public abstract <T extends Serializable> T execute(Transaction<T> transaction);
	public abstract <T extends Serializable> T execute(StreamConsumer<T> streamConsumer, InputStream inputStream);
	public abstract <T extends Serializable> T execute(StreamProducer<T> streamProducer, OutputStream outputStream);

	// 
	
	public abstract <T> T read(Class<T> clazz, long id);
	public abstract <T> List<T> search(Class<T> clazz, String query, int maxResults);
	public abstract <T> List<T> search(Class<T> clazz, Object[] keys, String query, int maxResults);
	public abstract <T> List<T> read(Class<T> clazz, Criteria criteria);

	public abstract <T> long insert(T object);
	public abstract <T> void update(T object);
	public abstract <T> void delete(T object);
	public abstract <T> void deleteAll(Class<T> clazz);

	// Only for historized tables
	public abstract <T> List<T> loadHistory(T object);
	public abstract <T> T read(Class<T> clazz, long id, Integer time);
	
	public abstract Serializable executeStatement(String queryName, Serializable... parameter);

}
