package org.minimalj.frontend.impl.cheerpj;

import java.io.IOException;
import java.util.logging.Logger;

import org.minimalj.application.Application;
import org.minimalj.application.Configuration;
import org.minimalj.util.StringUtils;

import fi.iki.elonen.NanoHTTPD;

public class CheerpjServer {
	private static final Logger LOG = Logger.getLogger(CheerpjServer.class.getName());
	
	private static final boolean SECURE = true;
	private static final int TIME_OUT = 5 * 60 * 1000;
	
	private static void start(boolean secure) {
		int port = getPort(secure);
		if (port > 0) {
			LOG.info("Start SimpleServer on port " + port + (secure ? " (Secure)" : ""));
			NanoHTTPD nanoHTTPD = new CheerpjHTTPD(port, secure);
			try {
				nanoHTTPD.start(TIME_OUT, false); // false -> this will not start a 'java' daemon, but a normal thread which keeps JVM alive
			} catch (IOException x) {
				throw new RuntimeException(x);
			}
		}
	}
	
	private static int getPort(boolean secure) {
		String portString = Configuration.get("MjFrontendPort" + (secure ? "Ssl" : ""), secure ? "-1" : "8080");
		return !StringUtils.isEmpty(portString) ? Integer.valueOf(portString) : -1 ;
	}

	public static void start() {
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