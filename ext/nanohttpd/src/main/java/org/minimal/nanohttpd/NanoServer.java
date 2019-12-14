package org.minimal.nanohttpd;

import java.io.IOException;
import java.util.logging.Logger;

import org.minimalj.application.Application;
import org.minimalj.application.Configuration;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.impl.json.JsonFrontend;
import org.minimalj.frontend.impl.web.WebServer;
import org.minimalj.model.test.ModelTest;
import org.minimalj.util.StringUtils;

import fi.iki.elonen.NanoHTTPD;

public class NanoServer {
	private static final Logger LOG = Logger.getLogger(NanoServer.class.getName());

	private static final boolean SECURE = true;
	private static final int TIME_OUT = 5 * 60 * 1000;

	private static void start(boolean secure) {
		int port = getPort(secure);
		if (port > 0) {
			LOG.info("Start " + Application.getInstance().getClass().getSimpleName() + " NanoServer " + (WebServer.useWebSocket ? "socket " : "") + "frontend on port " + port
					+ (secure ? " (Secure)" : ""));

			NanoHTTPD nanoHTTPD = new MjWebDaemon(port, secure);
			try {
				nanoHTTPD.start(TIME_OUT, false); // false -> this will not start a 'java' daemon, but a normal thread which keeps
													// JVM alive
			} catch (IOException x) {
				throw new RuntimeException(x);
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