package ch.openech.mj.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import ch.openech.mj.application.MjApplication;
import ch.openech.mj.server.Services.ServiceNamingConvention;
import ch.openech.mj.util.LoggingRuntimeException;
import ch.openech.mj.util.SerializationInputStream;
import ch.openech.mj.util.SerializationOutputStream;
import ch.openech.mj.util.StringUtils;


public class SocketServer {
	private static final Logger logger = Logger.getLogger(SocketServer.class.getName());
	
	private final int port;
	private final ThreadPoolExecutor executor;
	
	public SocketServer(int port) {
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
//				SocketServerRunnable runnable = new SocketServerRunnable(socket);
				SocketServerRunnable_withObjectInputStream runnable = new SocketServerRunnable_withObjectInputStream(socket);
				executor.execute(runnable);
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Server socket couldn't accept connection", e);
			}
		}
	}
	
	private static class SocketServerRunnable implements Runnable {
		private final Socket socket;
		private final ServiceNamingConvention serviceNamingConvention = new Services.DefaultServiceNamingConvention();

		public SocketServerRunnable(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			try (SerializationInputStream is = new SerializationInputStream(socket.getInputStream())) {
				String serviceName = is.readString();
				String methodName = is.readString();
				// Class<?>[] parameterTypes = (Class<?>[]) ois.readObject();
				Class<?>[] parameterTypes = is.readParameterTypes();
				Object[] args = is.readArguments();

				String implementationClassName = serviceNamingConvention.getImplementationClassName(serviceName);
				Class<?> implementationClass = Class.forName(implementationClassName);
				Method method = implementationClass.getMethod(methodName, parameterTypes);
				Object implementation = implementationClass.newInstance();
				
				Object result = method.invoke(implementation, args);
				
				try (SerializationOutputStream os = new SerializationOutputStream(socket.getOutputStream())) {
					os.writeArgument(result);
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
	
	@SuppressWarnings("unused")
	private static class SocketServerRunnable_withObjectInputStream implements Runnable {
		private final Socket socket;
		private final ServiceNamingConvention serviceNamingConvention = new Services.DefaultServiceNamingConvention();

		public SocketServerRunnable_withObjectInputStream(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			try (ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {
				String serviceName = (String) ois.readObject();
				String methodName = (String) ois.readObject();
				Class<?>[] parameterTypes = (Class<?>[]) ois.readObject();
				Object[] args = (Object[]) ois.readObject();

				String implementationClassName = serviceNamingConvention.getImplementationClassName(serviceName);
				Class<?> implementationClass = Class.forName(implementationClassName);
				Method method = implementationClass.getMethod(methodName, parameterTypes);
				Object implementation = implementationClass.newInstance();
				
				Object result = method.invoke(implementation, args);
				
				try (ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())) {
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
		String applicationName = System.getProperty("MjApplication");
		if (StringUtils.isBlank(applicationName)) {
			System.err.println("Missing MjApplication parameter");
			System.exit(-1);
		}

		@SuppressWarnings("unchecked")
		Class<? extends MjApplication> applicationClass = (Class<? extends MjApplication>) Class.forName(applicationName);
		MjApplication application = applicationClass.newInstance();
		application.init();

		new SocketServer(8020).run();
	}

}
