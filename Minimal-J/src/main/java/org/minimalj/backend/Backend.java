package org.minimalj.backend;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;

import org.minimalj.backend.db.DbBackend;
import org.minimalj.transaction.StreamConsumer;
import org.minimalj.transaction.StreamProducer;
import org.minimalj.transaction.Transaction;
import org.minimalj.transaction.criteria.Criteria;

/**
 * The backend can be in same VM as the frontend or it can
 * be on a remote server.<p>
 * 
 * @author bruno
 *
 */
public abstract class Backend {

	static Backend instance;
	
	public static void setSocketBackend(String backendAddress, int port) {
		setInstance(new SocketBackend(backendAddress, port));
	}

	public static void setEmbeddedDbBackend() {
		setInstance(new DbBackend());
	}
	
	public static void setDbBackend(String database, String user, String password) {
		setInstance(new DbBackend(database, user, password));
	}
	
	public static void setInstance(Backend backend) {
		if (Backend.instance != null) {
			throw new IllegalStateException("Backend cannot be changed");
		}		
		if (backend == null) {
			throw new IllegalArgumentException("Backend cannot be null");
		}
		instance = backend;
	}
	
	public static Backend getInstance() {
		return instance;
	}

	public abstract <T extends Serializable> T execute(Transaction<T> transaction);
	public abstract <T extends Serializable> T execute(StreamConsumer<T> streamConsumer, InputStream inputStream);
	public abstract <T extends Serializable> T execute(StreamProducer<T> streamProducer, OutputStream outputStream);

	// persistence
	
	public abstract <T> T read(Class<T> clazz, long id);
	public abstract <T> List<T> read(Class<T> clazz, Criteria criteria, int maxResults);

	public abstract <T> long insert(T object);
	public abstract <T> void update(T object);
	public abstract <T> void delete(T object);
	public abstract <T> void deleteAll(Class<T> clazz);

	public abstract <T> T executeStatement(Class<T> clazz, String queryName, Serializable... parameter);
	public abstract <T> List<T> executeStatement(Class<T> clazz, String queryName, int maxResults, Serializable... parameter);

	// Only for historized entities
	public abstract <T> List<T> loadHistory(T object);
	public abstract <T> T read(Class<T> clazz, long id, Integer time);
	
}
