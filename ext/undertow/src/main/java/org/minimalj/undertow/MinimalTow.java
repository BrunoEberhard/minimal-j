package org.minimalj.undertow;

import java.util.logging.Logger;

import org.minimalj.application.Application;
import org.minimalj.application.Configuration;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.impl.json.JsonFrontend;
import org.minimalj.frontend.impl.web.WebServer;
import org.minimalj.model.test.ModelTest;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.Undertow.Builder;

public class MinimalTow {
	private static final Logger LOG = Logger.getLogger(MinimalTow.class.getName());

	private static void start(boolean secure) {
		int port = WebServer.getPort(secure);
		if (port > 0) {
			LOG.info("Start " + Application.getInstance().getClass().getSimpleName() + " undertow web frontend on port " + port + (secure ? " (Secure)" : ""));
			try {
				Builder builder = Undertow.builder();
				if (secure) {
					builder.addHttpsListener(port, "0.0.0.0", WebServer.createSslContext());
				} else {
					builder.addHttpListener(port, "0.0.0.0");
				}
	
				boolean useWebSocket = Boolean.valueOf(Configuration.get("MjUseWebSocket", "false"));
	
				MinimalTowHandler minimalTowHandler = new MinimalTowHandler();
				if (useWebSocket) {
					builder.setHandler(Handlers.websocket(minimalTowHandler, minimalTowHandler));
				} else {
					builder.setHandler(minimalTowHandler);
				}
	
				builder.build().start();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}				
		}
	}

	public static void start() {
		ModelTest.exitIfProblems();
		Frontend.setInstance(new JsonFrontend());

		start(true);
		start(false);
	}

	public static void start(Application application) {
		Application.setInstance(application);
		start();
	}

	public static void main(final String[] args) {
		Application.initApplication(args);
		start();
	}
}
