package org.minimalj.backend;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.minimalj.backend.repository.CountTransaction;
import org.minimalj.backend.repository.DeleteEntityTransaction;
import org.minimalj.backend.repository.InsertTransaction;
import org.minimalj.backend.repository.ReadCriteriaTransaction;
import org.minimalj.backend.repository.ReadEntityTransaction;
import org.minimalj.backend.repository.UpdateTransaction;
import org.minimalj.repository.Repository;
import org.minimalj.repository.query.Query;
import org.minimalj.rest.EntityJsonReader;
import org.minimalj.security.Authentication;
import org.minimalj.security.Subject;
import org.minimalj.transaction.InputStreamTransaction;
import org.minimalj.transaction.OutputStreamTransaction;
import org.minimalj.transaction.Transaction;
import org.minimalj.util.LoggingRuntimeException;
import org.minimalj.util.SerializationContainer;

import fi.iki.elonen.NanoHTTPD.Response.Status;

/**
 * The RestBackend should rely on the services from a RestServer. CRUD
 * Transaction call special methods, all other Transactions call a generic
 * java-transaction method which is kind of a tunneling of the Transaction. The
 * input is serialized and encoded and the output the other way around.
 * <p>
 * 
 * @author bruno
 */
public class RestBackend extends Backend {
	private static final Logger LOG = Logger.getLogger(RestBackend.class.getName());

	private final String url;
	private final int port;
	
	public RestBackend() {
		this("localhost", 8090);
	}
	
	public RestBackend(String url, int port) {
		this.url = url;
		this.port = port;
		setRepository(new RestBackendRepository());
	}
	
	@Override
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
		if (transaction instanceof ReadEntityTransaction) {
			return super.doExecute(transaction);
		}
		HttpURLConnection connection = null;
		try {
			URL u = new URL("http", url, port, "/java-transaction/" + UUID.randomUUID());
			connection = (HttpURLConnection) u.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			// maybe POST would fit better, but sending binary data is much easier with put.
			connection.setRequestMethod("PUT");
			connection.addRequestProperty("Content-Type", "application/octet-stream");

			Subject subject = Subject.getCurrent();
			if (subject != null) {
				connection.setRequestProperty("token", subject.getToken().toString());
			}
			OutputStream outputStream = connection.getOutputStream();
			
			try (ObjectOutputStream oos = new ObjectOutputStream(outputStream)) {
				oos.writeObject(transaction);
				oos.flush();
				if (transaction instanceof InputStreamTransaction) {
					sendStream(oos, ((InputStreamTransaction<?>) transaction).getStream());
				}
				
				outputStream.flush();
				 
				try (ObjectInputStream ois = new ObjectInputStream(connection.getInputStream())) {
					if (transaction instanceof OutputStreamTransaction) {
						receiveStream(ois, ((OutputStreamTransaction<?>) transaction).getStream());
					}

					return (T) SerializationContainer.unwrap(readResult(ois));
				} catch (ClassNotFoundException e) {
					throw new LoggingRuntimeException(e, LOG, "Could not read result from transaction");
				} catch (IOException e) {
					if (connection.getResponseCode() == Status.INTERNAL_ERROR.getRequestStatus()) {
						throw new RuntimeException(connection.getResponseMessage());
					} else {
						throw new RuntimeException("Could not execute " + transaction, e);
					}
				}
			}
		} catch (IOException x) {
			throw new LoggingRuntimeException(x, LOG, "Couldn't execute on " + url);
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
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
	
	public class RestBackendRepository implements Repository {

		@Override
		public <T> T read(Class<T> clazz, Object id) {
			// return execute(new ReadEntityTransaction<T>(clazz, id, null));
			HttpURLConnection connection = null;
			try {
				URL u = new URL("http", url, port, "/" + clazz.getSimpleName() + "/" + id);
				connection = (HttpURLConnection) u.openConnection();
				connection.setRequestMethod("GET");
				connection.setDoOutput(false);
				connection.setDoInput(true);
				Subject subject = Subject.getCurrent();
				if (subject != null) {
					connection.setRequestProperty("token", subject.getToken().toString());
				}
				connection.connect();
				if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
					try (InputStream inputStream = connection.getInputStream()) {
						EntityJsonReader read = new EntityJsonReader();
						return (T) read.read(clazz, inputStream);
					}
				} else {
					// throw new RuntimeException(errorMessage);
				}
			} catch (IOException x) {
				throw new LoggingRuntimeException(x, LOG, "Couldn't execute on " + url);
			} finally {
				if (connection != null) {
					connection.disconnect();
				}
			}
			return null;
		}

		@Override
		public <T> List<T> find(Class<T> clazz, Query query) {
			return execute(new ReadCriteriaTransaction<T>(clazz, query));
		}

		@Override
		public <T> long count(Class<T> clazz, Query query) {
			return execute(new CountTransaction<T>(clazz, query));
		}
		
		@Override
		public <T> Object insert(T object) {
			// TODO use POST
			return execute(new InsertTransaction<T>(object));
		}

		@Override
		public <T> void update(T object) {
			// TODO use PUT
			execute(new UpdateTransaction<T>(object));
		}

		@Override
		public <T> void delete(Class<T> clazz, Object id) {
			// TODO use DELETE
			execute(new DeleteEntityTransaction<T>(clazz, id));
		}
	}

}