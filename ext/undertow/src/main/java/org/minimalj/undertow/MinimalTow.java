package org.minimalj.undertow;

import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;

import org.minimalj.application.Application;
import org.minimalj.application.Configuration;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.impl.json.JsonFrontend;
import org.minimalj.model.test.ModelTest;
import org.minimalj.util.StringUtils;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.Undertow.Builder;

public class MinimalTow {
	private static final Logger LOG = Logger.getLogger(MinimalTow.class.getName());

	private static void start(boolean secure) {
		int port = getPort(secure);
		if (port > 0) {
			LOG.info("Start " + Application.getInstance().getClass().getSimpleName() + " undertow web frontend on port " + port + (secure ? " (Secure)" : ""));

			Builder builder = Undertow.builder();
			if (secure) {
				builder.addHttpsListener(port, "localhost", getSslContext());
			} else {
				builder.addHttpListener(port, "localhost");
			}

			boolean useWebSocket = Boolean.valueOf(Configuration.get("MjUseWebSocket", "false"));

			MinimalTowHandler minimalTowHandler = new MinimalTowHandler();
			if (useWebSocket) {
				builder.setHandler(Handlers.websocket(minimalTowHandler, minimalTowHandler));
			} else {
				builder.setHandler(minimalTowHandler);
			}

			builder.build().start();
		}
	}

	private static SSLContext getSslContext() {
		try {
			return SSLContext.getDefault();
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}

	private static int getPort(boolean secure) {
		String portString = Configuration.get("MjFrontendPort" + (secure ? "Ssl" : ""), secure ? "-1" : "8080");
		return !StringUtils.isEmpty(portString) ? Integer.valueOf(portString) : -1;
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
