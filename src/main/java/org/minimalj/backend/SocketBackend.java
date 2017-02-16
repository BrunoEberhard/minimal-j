package org.minimalj.backend;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.logging.Logger;

import org.minimalj.backend.repository.DeleteEntityTransaction;
import org.minimalj.backend.repository.InsertTransaction;
import org.minimalj.backend.repository.ListTransaction.ReadAllElementsTransaction;
import org.minimalj.backend.repository.ReadCriteriaTransaction;
import org.minimalj.backend.repository.ReadEntityTransaction;
import org.minimalj.backend.repository.SaveTransaction;
import org.minimalj.backend.repository.UpdateTransaction;
import org.minimalj.repository.Repository;
import org.minimalj.repository.query.Query;
import org.minimalj.repository.sql.LazyList;
import org.minimalj.security.Authentication;
import org.minimalj.security.Subject;
import org.minimalj.transaction.InputStreamTransaction;
import org.minimalj.transaction.OutputStreamTransaction;
import org.minimalj.transaction.Transaction;
import org.minimalj.util.LoggingRuntimeException;
import org.minimalj.util.SerializationContainer;

public class SocketBackend extends Backend {
	private static final Logger LOG = Logger.getLogger(SocketBackend.class.getName());

	private final String url;
	private final int port;
	
	public SocketBackend(String url, int port) {
		this.url = url;
		this.port = port;
		setRepository(new SocketBackendRepository());
	}
	
	public Authentication createAuthentication() {
		return execute(new GetAuthentication());
	}
	
	public static class GetAuthentication implements Transaction<Authentication> {
		private static final long serialVersionUID = 1L;

		@Override
		public Authentication execute() {
			return Backend.getInstance().getAuthentication();
		}
	}
	
	@Override
	public <T> T doExecute(Transaction<T> transaction) {
		try (Socket socket = new Socket(url, port)) {
			try (ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())) {
				Subject subject = Subject.getCurrent();
				oos.writeObject(subject != null ? subject.getToken() : null);
				
				oos.writeObject(transaction);
				if (transaction instanceof InputStreamTransaction) {
					sendStream(oos, ((InputStreamTransaction<?>) transaction).getStream());
				}
				
				try (ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {
					if (transaction instanceof OutputStreamTransaction) {
						receiveStream(ois, ((OutputStreamTransaction<?>) transaction).getStream());
					}
					
					String errorMessage = (String) ois.readObject();
					if (errorMessage != null) throw new RuntimeException(errorMessage);
					
					return readResult(ois);
				} catch (ClassNotFoundException e) {
					throw new LoggingRuntimeException(e, LOG, "Could not read result from transaction");
				}
			}
		} catch (IOException x) {
			throw new LoggingRuntimeException(x, LOG, "Couldn't execute on " + url + ":" + port);
		}
	}
	
	protected <T> T readResult(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		Object wrappedResult = ois.readObject();
		@SuppressWarnings("unchecked")
		T result =  (T) SerializationContainer.unwrap(wrappedResult);
		return result;
	}
	
	// send data from frontend to backend (import of data)
	private void sendStream(ObjectOutputStream oos, InputStream inputStream) throws IOException {
		int b;
		while ((b = inputStream.read()) >= 0) {
			oos.write(b);
		}
		oos.flush();
	}

	// send data from backend to frontend (export data)
	private void receiveStream(ObjectInputStream ois, OutputStream outputStream) throws IOException {
		int b;
		while ((b = ois.read()) >= 0) {
			outputStream.write(b);
		}
		return;
	}
	
	public static class SocketBackendRepository implements Repository {

		public <T> T read(Class<T> clazz, Object id) {
			return execute(new ReadEntityTransaction<T>(clazz, id, null));
		}

		public <T> List<T> find(Class<T> clazz, Query query) {
			return execute(new ReadCriteriaTransaction<T>(clazz, query));
		}

		public <T> Object insert(T object) {
			return execute(new InsertTransaction<T>(object));
		}

		public <T> void update(T object) {
			execute(new UpdateTransaction<T>(object));
		}

		public <T> T save(T object) {
			return execute(new SaveTransaction<T>(object));
		}

		public <T> void delete(Class<T> clazz, Object id) {
			execute(new DeleteEntityTransaction<T>(clazz, id));
		}

		@Override
		public <ELEMENT, PARENT> List<ELEMENT> getList(LazyList<PARENT, ELEMENT> list) {
			return execute(new ReadAllElementsTransaction<>(list));
		}
	}

}