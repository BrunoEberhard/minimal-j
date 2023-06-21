package org.minimalj.frontend.impl.web;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import org.minimalj.application.Application;
import org.minimalj.application.Configuration;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.impl.json.JsonFrontend;
import org.minimalj.model.test.ModelTest;
import org.minimalj.util.LocaleContext;
import org.minimalj.util.LocaleContext.AcceptedLanguageLocaleSupplier;
import org.minimalj.util.StringUtils;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsServer;

public class WebServer {
	private static final Logger LOG = Logger.getLogger(WebServer.class.getName());

	public static final boolean SECURE = true;

	public static final String X_FORWARDED_PROTO = "X-Forwarded-Proto";

	public static boolean useWebSocket = Boolean.valueOf(Configuration.get("MjUseWebSocket", "false"));

	private static HttpServer server;

	public static class WebServerHttpExchange extends MjHttpExchange {
		private final HttpExchange exchange;

		protected WebServerHttpExchange(HttpExchange exchange) {
			this.exchange = exchange;
		}

		@Override
		public String getPath() {
			return exchange.getRequestURI().getPath();
		}

		@Override
		public String getMethod() {
			return exchange.getRequestMethod();
		}

		@Override
		public InputStream getRequest() {
			return exchange.getRequestBody();
		}

		@Override
		public Map<String, Collection<String>> getParameters() {
			if (exchange.getRequestMethod().equals("GET")) {
				return decodeParameters(exchange.getRequestURI().getQuery());
			} else {
				String requestBody = convertStreamToString(exchange.getRequestBody());
				return decodeParameters(requestBody);
			}
		}

		@Override
		public String getHeader(String name) {
			Collection<String> values = exchange.getRequestHeaders().get(name);
			return values != null ? values.iterator().next() : null;
		}

		@Override
		public void addHeader(String key, String value) {
			exchange.getResponseHeaders().add(key, value);
		}

		@Override
		public void sendResponse(int statusCode, byte[] bytes, String contentType) {
			try (OutputStream os = exchange.getResponseBody()) {
				exchange.getResponseHeaders().add("Content-Type", contentType);
				exchange.sendResponseHeaders(statusCode, bytes.length);
				os.write(bytes);
			} catch (IOException x) {
				// this happens when the browser doesn't accept the response
				// and this can be quite often. Only log with level finer
				LOG.log(Level.INFO, x.getMessage(), x);
			}
		}

		@Override
		public void sendResponse(int statusCode, String response, String contentType) {
			sendResponse(statusCode, response.getBytes(Charset.forName("utf-8")), contentType);
		}

		@Override
		public boolean isResponseSent() {
			return exchange.getResponseCode() >= 0;
		}
	}

	private static void handle(HttpExchange exchange) {
		try {
			LocaleContext.setLocale(new AcceptedLanguageLocaleSupplier(exchange.getRequestHeaders().getFirst(AcceptedLanguageLocaleSupplier.ACCEPTED_LANGUAGE_HEADER)));
			MjHttpExchange mjHttpExchange = new WebServerHttpExchange(exchange);
			WebApplication.handle(mjHttpExchange);
		} finally {
			LocaleContext.resetLocale();
		}
	}

	private static class HttpsRedirectFilter extends com.sun.net.httpserver.Filter {

		@Override
		public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
			if (!isHttps(exchange.getProtocol())) {
				redirect(exchange);
			} else {
				chain.doFilter(exchange);
			}
		}

		protected void redirect(HttpExchange exchange) throws IOException {
			String host = exchange.getRequestHeaders().getFirst("host");
			if (!StringUtils.isEmpty(host)) {
				int index = host.indexOf(":");
				if (index > 0) {
					host = host.substring(0, index);
				}
				exchange.getResponseHeaders().add("Location", "https://" + host + getPort() + exchange.getRequestURI().getPath());
				exchange.sendResponseHeaders(301, -1);
			} else {
				exchange.sendResponseHeaders(400, -1);
			}
		}

		protected String getPort() {
			int port = WebServer.getPort(SECURE);
			if (port != 443) {
				return ":" + port;
			} else {
				return "";
			}
		}

		protected boolean isHttps(String s) {
			return s.toUpperCase().contains("HTTPS");
		}

		@Override
		public String description() {
			return "Redirects non https requests";
		}
	}

	private static class FowardedHttpsRedirectFilter extends HttpsRedirectFilter {

		@Override
		public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
			String forwardedProtocol = exchange.getRequestHeaders().getFirst(X_FORWARDED_PROTO);
			if (!StringUtils.isEmpty(forwardedProtocol) && !isHttps(forwardedProtocol)) {
				redirect(exchange);
			} else {
				chain.doFilter(exchange);
			}
		}

		protected String getPort() {
			return "";
		}

		@Override
		public String description() {
			return "Redirects a forwarded request that was not a https request";
		}
	}

	private static void start(boolean secure) {
		int port = getPort(secure);
		if (port > 0) {
			LOG.info("Start " + Application.getInstance().getClass().getSimpleName() + " web frontend on port " + port + (secure ? " (Secure)" : ""));
			try {
				InetSocketAddress addr = new InetSocketAddress(port);
				server = secure ? HttpsServer.create(addr, 0) : HttpServer.create(addr, 0);
				HttpContext context = server.createContext("/");
				if (!secure) {
					boolean secureAvailable = getPort(SECURE) > 0;
					if (secureAvailable && Boolean.valueOf(Configuration.get("MjForceSsl", "true"))) {
						context.getFilters().add(new HttpsRedirectFilter());
					}
					if (!secureAvailable && !Boolean.valueOf(Configuration.get("MjForceSsl", "false")) && !Configuration.isDevModeActive()) {
						context.getFilters().add(new FowardedHttpsRedirectFilter());
					}
				} else {
					SSLContext sslContext = createSslContext();
					((HttpsServer) server).setHttpsConfigurator(new com.sun.net.httpserver.HttpsConfigurator(sslContext));
				}
				context.setHandler(WebServer::handle);
				server.start();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static SSLContext createSslContext()
			throws NoSuchAlgorithmException, KeyStoreException, FileNotFoundException, IOException, CertificateException, UnrecoverableKeyException, KeyManagementException {
		if (!Configuration.available("MjKeyStorePassword") || !Configuration.available("MjKeyStore")) {
			throw new RuntimeException("MjKeyStore / MjKeyStorePassword not set");
		}
		SSLContext sslContext = SSLContext.getInstance("TLS");

		char[] password = Configuration.get("MjKeyStorePassword").toCharArray();
		KeyStore ks = KeyStore.getInstance("JKS");
		FileInputStream fis = new FileInputStream(Configuration.get("MjKeyStore"));
		ks.load(fis, password);

		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
		keyManagerFactory.init(ks, password);

		sslContext.init(keyManagerFactory.getKeyManagers(), null, null);
		return sslContext;
	}

	public static String convertStreamToString(InputStream is) {
		try (InputStreamReader ir = new InputStreamReader(is, StandardCharsets.UTF_8)) {
			// Replace with readAllBytes of Java in higher versions
			return new String(readAllBytes(is), StandardCharsets.UTF_8);
		} catch (IOException x) {
			throw new RuntimeException(x);
		}
	}

	public static byte[] readAllBytes(InputStream inputStream) throws IOException {
		int bufLen = 1024;
		byte[] buf = new byte[bufLen];
		int readLen;

		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			while ((readLen = inputStream.read(buf, 0, bufLen)) != -1) {
				outputStream.write(buf, 0, readLen);
			}
			return outputStream.toByteArray();
		}
	}

	public static int getPort(boolean secure) {
		String portString = Configuration.get("MjFrontendPort" + (secure ? "Ssl" : ""), secure ? "-1" : "8080");
		return !StringUtils.isEmpty(portString) ? Integer.valueOf(portString) : -1;
	}

	public static void start() {
		if (useWebSocket) {
			System.err.println("WebSockets are not supported in JDK Server. Please use MinimalTow or NanoHttp ext - projects for WebSockets.");
			System.exit(-1);
		}

		ModelTest.exitIfProblems();
		Frontend.setInstance(new JsonFrontend());

		start(!SECURE);
		start(SECURE);
	}

	// only for tests
	public static void stop() {
		if (server != null) {
			server.stop(0);
		}
	}

	public static void start(Application application) {
		Application.setInstance(application);
		start();
	}

	public static void main(String... args) {
		Application.initApplication(args);
		start();
	}

}