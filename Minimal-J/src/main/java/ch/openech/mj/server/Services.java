package ch.openech.mj.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.ConnectException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import ch.openech.mj.util.LoggingRuntimeException;
import ch.openech.mj.util.SerializationContainer;

public class Services {
	private static final Logger logger = Logger.getLogger(Services.class.getName());

	private static InvocationHandler invocationHandler;

	public static void configureLocal() {
		setInvocationHandler(new LocalInvocationHandler());
	}
	
	public static void configureRemoteSocket(String url, int port) {
		setInvocationHandler(new SocketInvocationHandler(url, port));
	}
	
	public static synchronized void setInvocationHandler(InvocationHandler invocationHandler) {
		if (Services.invocationHandler != null) {
			throw new IllegalStateException("InvocationHandler cannot be changed");
		}		
		if (invocationHandler == null) {
			throw new IllegalArgumentException("InvocationHandler cannot be null");
		}
		Services.invocationHandler = invocationHandler;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T get(Class<T> serviceClass) {
		return (T) Proxy.newProxyInstance(Services.class.getClassLoader(), new Class<?>[] {serviceClass}, invocationHandler);
	}
	
	private static class LocalInvocationHandler implements InvocationHandler {
		private final ServiceNamingConvention serviceNamingConvention;
		private final Map<Class<?>, Object> implementations = new HashMap<>();
		
		public LocalInvocationHandler() {
			this(new DefaultServiceNamingConvention());
		}
		
		public LocalInvocationHandler(ServiceNamingConvention serviceNamingConvention) {
			this.serviceNamingConvention = serviceNamingConvention;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			Object implementation = getImplementation(method.getDeclaringClass());
			Object result = method.invoke(implementation, args);
			return result;
		}

		public Object getImplementation(Class<?> interfaceClass) {
			if (!implementations.containsKey(interfaceClass)) {
				String implementationClassName = serviceNamingConvention.getImplementationClassName(interfaceClass.getName());
				try {
					Class<?> implementationClass = Class.forName(implementationClassName);
					Object implementation = implementationClass.newInstance();
					implementations.put(interfaceClass, implementation);
				} catch (ClassNotFoundException cnfe) {
					String msg = "Could not find class for interface " + interfaceClass.getName() + ". Looked for " + implementationClassName;
					throw new LoggingRuntimeException(cnfe, logger, msg);
				} catch (InstantiationException | IllegalAccessException e) {
					String msg = "Could not instantiate implementation class " + implementationClassName +" because of " + e.getClass().getName();
					throw new LoggingRuntimeException(e, logger, msg);
				}
			}
			return implementations.get(interfaceClass);
		}

	}
	
	public static interface ServiceNamingConvention {
		public String getImplementationClassName(String interfaceName);
	}
	
	public static class DefaultServiceNamingConvention implements ServiceNamingConvention {
		public String getImplementationClassName(String interfaceName) {
			return interfaceName + "Impl";
		}
	}

	private static class SocketInvocationHandler implements InvocationHandler {
		private final String url;
		private final int port;
		
		public SocketInvocationHandler(String url, int port) {
			this.url = url;
			this.port = port;
		}
		
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			try (Socket socket = new Socket(url, port)) {
				try (ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())) {
					oos.writeObject(method.getDeclaringClass().getName());
					oos.writeObject(method.getName());
					oos.writeObject(method.getParameterTypes());
					oos.writeObject(SerializationContainer.wrap(args));
					sendStream(oos, args);
					try (ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {
						receiveStream(ois, args);
						Object result = SerializationContainer.unwrap(ois.readObject());
						return result;
					}
				}
			} catch (ConnectException c) {
				throw new RuntimeException("Couldn't connect to " + url + ":" + port);
			}
		}

		// send data from client to server (import of data)
		private void sendStream(ObjectOutputStream oos, Object[] args) throws IOException {
			for (Object arg : args) {
				if (arg instanceof InputStream) {
					InputStream inputStream = (InputStream) arg;
					int b;
					while ((b = inputStream.read()) >= 0) {
						oos.write(b);
					}
					oos.flush();
					return;
				}
			}
		}
		
		// send data from server to client (export data)
		private void receiveStream(ObjectInputStream ois, Object[] args) throws IOException {
			for (Object arg : args) {
				if (arg instanceof OutputStream) {
					OutputStream outputStream = (OutputStream) arg;
					int b;
					while ((b = ois.read()) >= 0) {
						outputStream.write(b);
					}
					return;
				}
			}
		}

	}
}
