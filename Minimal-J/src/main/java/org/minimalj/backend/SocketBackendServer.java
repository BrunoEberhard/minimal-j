package org.minimalj.backend;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.minimalj.application.Launcher;
import org.minimalj.transaction.StreamConsumer;
import org.minimalj.transaction.StreamProducer;
import org.minimalj.transaction.Transaction;
import org.minimalj.util.LoggingRuntimeException;
import org.minimalj.util.UnclosingOoutputStream;

// TODO @Deploy(Server)
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
	
	private static class SocketBackendRunnable implements Runnable {
		private final Socket socket;

		public SocketBackendRunnable(Socket socket) {
			this.socket = socket;
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public void run() {
			try (ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {
				Object input = ois.readObject();

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
						result = Backend.getInstance().execute(streamProducer, new UnclosingOoutputStream(oos));
					}
					oos.writeObject(result);
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
		Launcher.initApplication(args);
		
		new SocketBackendServer(8020).run();
	}

}
