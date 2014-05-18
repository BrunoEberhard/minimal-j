package ch.openech.mj.backend;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import ch.openech.mj.criteria.Criteria;
import ch.openech.mj.transaction.DeleteAllTransaction;
import ch.openech.mj.transaction.DeleteTransaction;
import ch.openech.mj.transaction.InsertTransaction;
import ch.openech.mj.transaction.ReadCriteriaTransaction;
import ch.openech.mj.transaction.ReadTransaction;
import ch.openech.mj.transaction.SearchTransaction;
import ch.openech.mj.transaction.StatementTransaction;
import ch.openech.mj.transaction.StreamConsumer;
import ch.openech.mj.transaction.StreamProducer;
import ch.openech.mj.transaction.Transaction;
import ch.openech.mj.transaction.UpdateTransaction;
import ch.openech.mj.util.SerializationContainer;

public class SocketBackend extends Backend {
	private final String url;
	private final int port;
	
	public SocketBackend(String url, int port) {
		this.url = url;
		this.port = port;
	}

	public <T extends Serializable> T execute(Transaction<T> transaction) {
		try (Socket socket = new Socket(url, port)) {
			try (ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())) {
				oos.writeObject(transaction);
				try (ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {
					@SuppressWarnings("unchecked")
					T result = (T) ois.readObject();
					return result;
				}
			}
		} catch (Exception c) {
			throw new RuntimeException("Couldn't connect to " + url + ":" + port);
		}
	}
	
	@Override
	public <T extends Serializable> T execute(StreamConsumer<T> streamConsumer, InputStream inputStream) {
		try (Socket socket = new Socket(url, port)) {
			try (ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())) {
				oos.writeObject(streamConsumer);
				sendStream(oos, inputStream);
				try (ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {
					@SuppressWarnings("unchecked")
					T result = (T) ois.readObject();
					return result;
				}
			}
		} catch (Exception c) {
			throw new RuntimeException("Couldn't connect to " + url + ":" + port);
		}
	}
	
	// send data from frontend to backend (import of data)
	private void sendStream(ObjectOutputStream oos, InputStream inputStream) throws IOException {
		int b;
		while ((b = inputStream.read()) >= 0) {
			oos.write(b);
		}
		oos.flush();
	}

	@Override
	public <T extends Serializable> T execute(StreamProducer<T> streamProducer, OutputStream outputStream) {
		try (Socket socket = new Socket(url, port)) {
			try (ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())) {
				oos.writeObject(streamProducer);
				try (ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {
					receiveStream(ois, outputStream);
					@SuppressWarnings("unchecked")
					T result = (T) ois.readObject();
					return result;
				}
			}
		} catch (Exception c) {
			throw new RuntimeException("Couldn't connect to " + url + ":" + port);
		}
	}

	// send data from backend to frontend (export data)
	private void receiveStream(ObjectInputStream ois, OutputStream outputStream) throws IOException {
		int b;
		while ((b = ois.read()) >= 0) {
			outputStream.write(b);
		}
		return;
	}
	
	public <T> T read(Class<T> clazz, long id) {
		return read(clazz, id, null);
	}
	
	public <T> List<T> search(Class<T> clazz, String query, int maxResults) {
		@SuppressWarnings("unchecked")
		List<T> result = (List<T>) SerializationContainer.unwrap(instance.execute(new SearchTransaction<T>(clazz, null, query, maxResults)));
		return result;
	}
	
	public <T> List<T> search(Class<T> clazz, Object[] keys, String query, int maxResults) {
		@SuppressWarnings("unchecked")
		List<T> result = (List<T>) SerializationContainer.unwrap(instance.execute(new SearchTransaction<T>(clazz, keys, query, maxResults)));
		return result;
	}
	
	public <T> List<T> read(Class<T> clazz, Criteria criteria) {
		@SuppressWarnings("unchecked")
		List<T> result = (List<T>) SerializationContainer.unwrap(instance.execute(new ReadCriteriaTransaction<T>(clazz, criteria)));
		return result;
	}
	
	public long insert(Object object) {
		return instance.execute(new InsertTransaction(object));
	}

	public void update(Object object) {
		instance.execute(new UpdateTransaction(object));
	}

	public <T> void delete(T object) {
		instance.execute(new DeleteTransaction(object));
	}
	
	public <T> void deleteAll(Class<T> clazz) {
		instance.execute(new DeleteAllTransaction(clazz));
	}
	
	public <T> List<T> loadHistory(T object) {
		// TODO
		return new ArrayList<>();
	}
	
	@SuppressWarnings("unchecked")
	public <T> T read(Class<T> clazz, long id, Integer time) {
		Serializable result = instance.execute(new ReadTransaction<T>(clazz, id, time));
		return (T) SerializationContainer.unwrap(result);
	}
	
	public Serializable executeStatement(String queryName, Serializable... parameter) {
		StatementTransaction statementTransaction = new StatementTransaction(queryName, parameter);
		return instance.execute(statementTransaction);
	}

}