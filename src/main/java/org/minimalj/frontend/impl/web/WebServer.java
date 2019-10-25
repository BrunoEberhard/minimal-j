package org.minimalj.frontend.impl.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.minimalj.application.Application;
import org.minimalj.application.Configuration;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.impl.json.JsonFrontend;
import org.minimalj.model.test.ModelTest;
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

	public static MjHttpExchange adapt(HttpExchange exchange) {
		return new MjHttpExchange() {
			@Override
			public String getPath() {
				return exchange.getRequestURI().getPath();
			}

			@Override
			public InputStream getRequest() throws IOException {
				return exchange.getRequestBody();
			}

			public Locale getLocale() {
				return MjHttpExchange.getLocale(exchange.getRequestHeaders().getFirst("accept-language"));
			}

			public void sendResponse(byte[] bytes) throws IOException {
				OutputStream os = exchange.getResponseBody();
				send(200);
				os.write(bytes);
				os.close();
			}

			public void sendResponse(String response) throws IOException {
				OutputStream os = exchange.getResponseBody();
				byte[] bytes = response.getBytes(Charset.forName("UTF-8"));
				exchange.sendResponseHeaders(200, bytes.length);
				os.write(bytes);
				os.close();
			}

			@Override
			public void sendError() {
				send(500);
			}

			public void sendForbidden() {
				send(400);
			}

			public void sendNotfound() {
				send(404);
			}

			private void send(int statusCode) {
				try {
					exchange.sendResponseHeaders(statusCode, 0);
				} catch (IOException x) {
					LOG.log(Level.WARNING, "Could not send status code", x);
				}
			}
		};
	}

	private static void start(boolean secure) {
		int port = getPort(secure);
		if (port > 0) {
			LOG.info("Start " + Application.getInstance().getClass().getSimpleName() + " web "
					+ (useWebSocket ? "socket " : "") + "frontend on port " + port + (secure ? " (Secure)" : ""));
			try {
				InetSocketAddress addr = new InetSocketAddress(port);
				HttpServer server = secure ? HttpsServer.create(addr, 0) : HttpServer.create(addr, 0);
				HttpContext context = server.createContext("/");
				MjHttpHandler handler = new MjHttpHandler();
				context.setHandler(e -> handler.handle(adapt(e)));
				server.start();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private static int getPort(boolean secure) {
		String portString = Configuration.get("MjFrontendPort" + (secure ? "Ssl" : ""), secure ? "-1" : "8080");
		return !StringUtils.isEmpty(portString) ? Integer.valueOf(portString) : -1;
	}

	public static void start() {
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