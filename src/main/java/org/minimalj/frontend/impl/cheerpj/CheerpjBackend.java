package org.minimalj.frontend.impl.cheerpj;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.logging.Logger;

import org.minimalj.backend.Backend;
import org.minimalj.backend.SocketBackend;
import org.minimalj.security.Authentication;
import org.minimalj.security.Subject;
import org.minimalj.transaction.InputStreamTransaction;
import org.minimalj.transaction.OutputStreamTransaction;
import org.minimalj.transaction.Transaction;
import org.minimalj.util.LoggingRuntimeException;
import org.minimalj.util.SerializationContainer;

public class CheerpjBackend extends Backend {
	private static final Logger LOG = Logger.getLogger(CheerpjBackend.class.getName());

	private final String url;
	private final int port;
	
	public CheerpjBackend() {
		this("localhost", 8080);
	}
	
	public CheerpjBackend(String url, int port) {
		LOG.fine("Create Http Backend to " + url + ":" + port);
		this.url = url;
		this.port = port;
		setRepository(new SocketBackend.BackendRepository());
	}
	
	@Override
	public Authentication createAuthentication() {
		return execute(new SocketBackend.GetAuthentication());
	}
	
	public <T> T doExecute(Transaction<T> transaction) {
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
					SocketBackend.sendStream(oos, ((InputStreamTransaction<?>) transaction).getStream());
				}
				
				outputStream.flush();
				 
				try (ObjectInputStream ois = new ObjectInputStream(connection.getInputStream())) {
					if (transaction instanceof OutputStreamTransaction) {
						SocketBackend.receiveStream(ois, ((OutputStreamTransaction<?>) transaction).getStream());
					}

					return (T) SerializationContainer.unwrap(SocketBackend.readResult(ois));
				} catch (ClassNotFoundException e) {
					throw new LoggingRuntimeException(e, LOG, "Could not read result from transaction");
				} catch (IOException e) {
					if (connection.getResponseCode() == 500) {
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
	
}