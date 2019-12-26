package org.minimalj.frontend.impl.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.minimalj.application.Application;
import org.minimalj.application.Configuration;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.impl.json.JsonFrontend;
import org.minimalj.model.test.ModelTest;
import org.minimalj.util.LocaleContext;
import org.minimalj.util.StringUtils;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsServer;

//Beginning with JDK 9 this restriction warnings were removed
@SuppressWarnings("restriction")
public class WebServer {
	private static final Logger LOG = Logger.getLogger(WebServer.class.getName());

	private static final boolean SECURE = true;

	public static boolean useWebSocket = Boolean.valueOf(Configuration.get("MjUseWebSocket", "false"));

	private static class WebServerHttpExchange extends MjHttpExchange {
		private final HttpExchange exchange;

		private WebServerHttpExchange(HttpExchange exchange) {
			this.exchange = exchange;
		}

		@Override
		public String getPath() {
			return exchange.getRequestURI().getPath();
		}

		@Override
		public InputStream getRequest() {
			return exchange.getRequestBody();
		}

		@Override
		public Map<String, List<String>> getParameters() {
			if (exchange.getRequestMethod().equals("GET")) {
				return decodeParameters(exchange.getRequestURI().getQuery());
			} else {
				String requestBody = convertStreamToString(exchange.getRequestBody());
				return decodeParameters(requestBody);
			}
		}

		@Override
		public void sendResponse(int statusCode, byte[] bytes, String contentType) {
			try (OutputStream os = exchange.getResponseBody()) {
				exchange.getResponseHeaders().add("Content-Type", contentType);
				exchange.sendResponseHeaders(statusCode, bytes.length);
				os.write(bytes);
			} catch (IOException x) {
				throw new RuntimeException(x);
			}
		}

		@Override
		public void sendResponse(int statusCode, String response, String contentType) {
			sendResponse(statusCode, response.getBytes(Charset.forName("utf-8")), contentType + "; charset=utf-8");
		}

		@Override
		public boolean isResponseSent() {
			return exchange.getResponseCode() >= 0;
		}
	}

	private static void handle(HttpExchange exchange) {
		try {
			LocaleContext.setCurrent(MjHttpExchange.getLocale(exchange.getRequestHeaders().getFirst("accept-language")));
			MjHttpExchange mjHttpExchange = new WebServerHttpExchange(exchange);
			WebApplication.handle(mjHttpExchange);
		} finally {
			LocaleContext.setCurrent(null);
		}
	}

	private static void start(boolean secure) {
		int port = getPort(secure);
		if (port > 0) {
			LOG.info("Start " + Application.getInstance().getClass().getSimpleName() + " web frontend on port " + port + (secure ? " (Secure)" : ""));
			try {
				InetSocketAddress addr = new InetSocketAddress(port);
				HttpServer server = secure ? HttpsServer.create(addr, 0) : HttpServer.create(addr, 0);
				HttpContext context = server.createContext("/");
				context.setHandler(WebServer::handle);
				server.start();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	// https://stackoverflow.com/questions/309424/how-do-i-read-convert-an-inputstream-into-a-string-in-java
	// Java 9: remove
	public static String convertStreamToString(InputStream is) {
		StringBuilder sb = new StringBuilder(1024);
		char[] read = new char[128];
		try (InputStreamReader ir = new InputStreamReader(is, StandardCharsets.UTF_8)) {
			for (int i; -1 != (i = ir.read(read)); sb.append(read, 0, i))
				;
		} catch (IOException x) {
			throw new RuntimeException(x);
		}
		return sb.toString();
	}

	private static int getPort(boolean secure) {
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

	public static void start(Application application) {
		Application.setInstance(application);
		start();
	}

	public static void main(String... args) {
		Application.initApplication(args);
		start();
	}

}