package org.minimalj.backend;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.List;

import org.minimalj.transaction.StreamConsumer;
import org.minimalj.transaction.StreamProducer;
import org.minimalj.transaction.Transaction;
import org.minimalj.transaction.criteria.Criteria;
import org.minimalj.transaction.persistence.DeleteAllTransaction;
import org.minimalj.transaction.persistence.DeleteTransaction;
import org.minimalj.transaction.persistence.InsertTransaction;
import org.minimalj.transaction.persistence.ReadCriteriaTransaction;
import org.minimalj.transaction.persistence.ReadTransaction;
import org.minimalj.transaction.persistence.StatementTransaction;
import org.minimalj.transaction.persistence.UpdateTransaction;
import org.minimalj.util.SerializationContainer;

public class SocketBackend extends Backend {
	private final String url;
	private final int port;
	
	public SocketBackend(String url, int port) {
		this.url = url;
		this.port = port;
	}

	@Override
	public <T> T execute(Transaction<T> transaction) {
		try (Socket socket = new Socket(url, port)) {
			try (ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())) {
				oos.writeObject(transaction);
				try (ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {
					return readResult(ois);
				}
			}
		} catch (Exception c) {
			throw new RuntimeException("Couldn't connect to " + url + ":" + port);
		}
	}

	protected <T> T readResult(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		Object wrappedResult = ois.readObject();
		@SuppressWarnings("unchecked")
		T result =  (T) SerializationContainer.unwrap(wrappedResult);
		return result;
	}
	
	@Override
	public <T extends Serializable> T execute(StreamConsumer<T> streamConsumer, InputStream inputStream) {
		try (Socket socket = new Socket(url, port)) {
			try (ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())) {
				oos.writeObject(streamConsumer);
				sendStream(oos, inputStream);
				try (ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {
					return readResult(ois);
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
					return readResult(ois);
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
	
	public <T> T read(Class<T> clazz, Object id) {
		return read(clazz, id, null);
	}
	
	public <T> List<T> read(Class<T> clazz, Criteria criteria, int maxResults) {
		List<T> result = getInstance().execute(new ReadCriteriaTransaction<T>(clazz, criteria, maxResults));
		return result;
	}
	
	public <T> Object insert(T object) {
		return getInstance().execute(new InsertTransaction(object));
	}

	public void update(Object object) {
		getInstance().execute(new UpdateTransaction(object));
	}

	public <T> void delete(Class<T> clazz, Object id) {
		getInstance().execute(new DeleteTransaction(clazz, id));
	}
	
	public <T> void deleteAll(Class<T> clazz) {
		getInstance().execute(new DeleteAllTransaction(clazz));
	}
	
	public <T> T read(Class<T> clazz, Object id, Integer time) {
		T result = getInstance().execute(new ReadTransaction<T>(clazz, id, time));
		return result;
	}
	
	public <T> T executeStatement(Class<T> clazz, String queryName, Serializable... parameter) {
		StatementTransaction statementTransaction = new StatementTransaction(clazz, queryName, parameter);
		return (T) getInstance().execute(statementTransaction);
	}
	
	public <T> List<T> executeStatement(Class<T> clazz, String queryName, int maxResults, Serializable... parameter) {
		StatementTransaction statementTransaction = new StatementTransaction(clazz, queryName, maxResults, parameter);
		return (List<T>) getInstance().execute(statementTransaction);
	}

}