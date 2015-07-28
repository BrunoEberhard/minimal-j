package org.minimalj.backend;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.minimalj.application.Application;
import org.minimalj.application.Authentication;
import org.minimalj.application.Subject;
import org.minimalj.transaction.Role;
import org.minimalj.transaction.StreamConsumer;
import org.minimalj.transaction.StreamProducer;
import org.minimalj.transaction.Transaction;
import org.minimalj.util.LoggingRuntimeException;
import org.minimalj.util.SerializationContainer;
import org.minimalj.util.UnclosingOutputStream;

public class SocketBackendServer {
	private static final Logger logger = Logger.getLogger(SocketBackendServer.class.getName());
	
	private final int port;
	private final ThreadPoolExecutor executor;
	
	public SocketBackendServer(int port) {
		this.port = port;
		this.executor = new ThreadPoolExecutor(10, 30, 10L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
	}
	
	public void run() {
		Thread.currentThread().setName("MjServer");
		try (ServerSocket serverSocket = new ServerSocket(port)) {
			accecptInvocations(serverSocket);
		} catch (IOException iox) {
			throw new LoggingRuntimeException(iox, logger, "Could not create server socket");
		}
	}

	private void accecptInvocations(ServerSocket serverSocket) {
		while (true) {
			Socket socket;
			try {
				socket = serverSocket.accept();
				SocketBackendRunnable runnable = new SocketBackendRunnable(socket);
				executor.execute(runnable);
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Server socket couldn't accept connection", e);
			}
		}
	}
	
	private class SocketBackendRunnable implements Runnable {
		private final Socket socket;

		public SocketBackendRunnable(Socket socket) {
			this.socket = socket;
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public void run() {
			try (ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {

				String user = (String) ois.readObject();
				Serializable authentication = null;
				if (user != null) {
					authentication = (Serializable) ois.readObject();
					Subject.set(Authentication.getInstance().login(user, authentication));
				} else {
					Subject.set(null);
				}

				Object input = ois.readObject();

				Role role = input.getClass().getAnnotation(Role.class);
				if (role != null) {
					Authentication.checkPermission(role.value());
				}
				
				Object result = null;
				if (input instanceof Transaction) {
					Transaction transaction = (Transaction) input;
					result = Backend.getInstance().execute(transaction);
				} else if (input instanceof StreamConsumer) {
					StreamConsumer streamConsumer = (StreamConsumer) input;
					result = Backend.getInstance().execute(streamConsumer, ois);
				} 
				
				try (ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())) {
					if (input instanceof StreamProducer) {
						StreamProducer streamProducer = (StreamProducer) input;
						result = Backend.getInstance().execute(streamProducer, new UnclosingOutputStream(oos));
					}
					Object wrappedResult = SerializationContainer.wrap(result);
					oos.writeObject(wrappedResult);
				}
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Could not create ObjectInputStream from socket", e);
				e.printStackTrace();
			} catch (Exception e) {
				logger.log(Level.SEVERE, "SocketRunnable failed", e);
				e.printStackTrace();
			}
		}
	}
	
	public static void main(final String[] args) throws Exception {
		Application.initApplication(args);
		
		new SocketBackendServer(8020).run();
	}

}
