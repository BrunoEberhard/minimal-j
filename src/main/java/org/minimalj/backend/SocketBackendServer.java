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
import org.minimalj.security.Authorization;
import org.minimalj.security.Subject;
import org.minimalj.transaction.InputStreamTransaction;
import org.minimalj.transaction.OutputStreamTransaction;
import org.minimalj.transaction.Transaction;
import org.minimalj.util.LoggingRuntimeException;
import org.minimalj.util.SerializationContainer;
import org.minimalj.util.UnclosingOutputStream;

public class SocketBackendServer {
	private static final Logger LOG = Logger.getLogger(SocketBackendServer.class.getName());
	
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
			throw new LoggingRuntimeException(iox, LOG, "Could not create server socket");
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
				LOG.log(Level.SEVERE, "Server socket couldn't accept connection", e);
			}
		}
	}
	
	private static class SocketBackendRunnable implements Runnable {
		private final Socket socket;

		public SocketBackendRunnable(Socket socket) {
			this.socket = socket;
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public void run() {
			try (ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {
				Serializable securityToken = (Serializable) ois.readObject();
				if (Authorization.isActive()) {
					Subject subject = Authorization.getInstance().getUserByToken(securityToken);
					Subject.setCurrent(subject);
				}
				
				Transaction transaction = (Transaction) ois.readObject();
				
				Object result, wrappedResult = null;
				if (transaction instanceof InputStreamTransaction) {
					InputStreamTransaction inputStreamTransaction = (InputStreamTransaction) transaction;
					inputStreamTransaction.setStream(ois);
				} 
				
				try (ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())) {			
					if (transaction instanceof OutputStreamTransaction) {
						OutputStreamTransaction outputStreamTransaction = (OutputStreamTransaction) transaction;
						outputStreamTransaction.setStream(new UnclosingOutputStream(oos));
					}
					try {
						result = Backend.execute(transaction);
						wrappedResult = SerializationContainer.wrap(result);
					} catch (Exception exception) {
						LOG.log(Level.SEVERE, "Exception in Transaction", exception);
						oos.writeObject(exception.getMessage());
						return;
					}
					oos.writeObject(null); // no exception
					oos.writeObject(wrappedResult);
				}
			} catch (IOException e) {
				LOG.log(Level.SEVERE, "Could not create ObjectInputStream from socket", e);
			} catch (ClassNotFoundException e) {
				LOG.log(Level.SEVERE, "Could not execute transaction", e);
			} finally {
				Subject.setCurrent(null);
			}
		}
	}
	
	public static void main(final String[] args) throws Exception {
		Application.initApplication(args);
		
		new SocketBackendServer(8020).run();
	}

}
