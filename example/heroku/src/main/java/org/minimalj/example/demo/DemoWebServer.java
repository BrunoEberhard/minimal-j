package org.minimalj.example.demo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.logging.Logger;

import org.minimalj.application.Application;
import org.minimalj.example.empty.EmptyApplication;
import org.minimalj.example.helloworld.HelloWorldApplication;
import org.minimalj.example.helloworld2.GreetingApplication;
import org.minimalj.example.library.MjExampleApplication;
import org.minimalj.example.minimalclinic.MinimalClinicApplication;
import org.minimalj.example.notes.NotesApplication;
import org.minimalj.example.numbers.NumbersApplication;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.impl.json.JsonFrontend;
import org.minimalj.frontend.impl.web.MjHttpExchange;
import org.minimalj.frontend.impl.web.WebApplication;
import org.minimalj.frontend.impl.web.WebServer;
import org.minimalj.frontend.impl.web.WebServer.WebServerHttpExchange;
import org.minimalj.util.LocaleContext;
import org.minimalj.util.LocaleContext.AcceptedLanguageLocaleSupplier;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsServer;

public class DemoWebServer {

	private static final Map<String, Application> APPLICATIONS = Map.of(
			"empty", new EmptyApplication(),
			"notes", new NotesApplication(),
			"helloWorld", new HelloWorldApplication(),
			"greeting", new GreetingApplication(),
			"numbers", new NumbersApplication(),
			"library", new MjExampleApplication(),
			// "petClinic", new PetClinicApplication(),
			"minimalClinic", new MinimalClinicApplication());

	private static final Logger LOG = Logger.getLogger(DemoWebServer.class.getName());

	public static class MultiApplicationWebServerHttpExchange extends WebServerHttpExchange {

		private final int applicationContextLength;
		
		protected MultiApplicationWebServerHttpExchange(HttpExchange exchange, int applicationContextLength) {
			super(exchange);
			this.applicationContextLength = applicationContextLength;
		}

		@Override
		public String getPath() {
			return super.getPath().substring(applicationContextLength);
		}
	}

	private static void handle(HttpExchange exchange) {
		try {
			LocaleContext.setLocale(new AcceptedLanguageLocaleSupplier(exchange.getRequestHeaders().getFirst(AcceptedLanguageLocaleSupplier.ACCEPTED_LANGUAGE_HEADER)));
			String path = exchange.getRequestURI().getPath();
			int applicationContextLength = path.indexOf('/', 1);
			String applicationContext = path.substring(1, applicationContextLength);
			Application currentApplication = APPLICATIONS.get(applicationContext);
			MjHttpExchange mjHttpExchange = new MultiApplicationWebServerHttpExchange(exchange, applicationContextLength);
			if (currentApplication == null) {
				mjHttpExchange.sendNotfound();
				return;
			}
			ThreadLocalApplication.INSTANCE.setCurrentApplication(currentApplication);
			WebApplication.handle(mjHttpExchange);
		} finally {
			LocaleContext.resetLocale();
		}
	}

	private static void start(boolean secure) {
		int port = WebServer.getPort(secure);
		if (port > 0) {
			LOG.info("Start " + Application.getInstance().getClass().getSimpleName() + " web frontend on port " + port + (secure ? " (Secure)" : ""));
			try {
				InetSocketAddress addr = new InetSocketAddress(port);
				HttpServer server = secure ? HttpsServer.create(addr, 0) : HttpServer.create(addr, 0);
				HttpContext context = server.createContext("/");
				context.setHandler(DemoWebServer::handle);
				server.start();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static void start() {
		if (WebServer.useWebSocket) {
			System.err.println("WebSockets are not supported in JDK Server. Please use MinimalTow or NanoHttp ext - projects for WebSockets.");
			System.exit(-1);
		}

		Frontend.setInstance(new JsonFrontend());

		start(!WebServer.SECURE);
		start(WebServer.SECURE);
	}

	
	public static void main(String... args) {
		ThreadLocalApplication.INSTANCE.setApplications(APPLICATIONS.values());
		Application.setInstance(ThreadLocalApplication.INSTANCE);
		start();
	}

}