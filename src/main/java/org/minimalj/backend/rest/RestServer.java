package org.minimalj.backend.rest;

import java.io.IOException;
import java.util.logging.Logger;

import org.minimalj.application.Application;
import org.minimalj.application.Configuration;
import org.minimalj.model.test.ModelTest;
import org.minimalj.util.StringUtils;

import fi.iki.elonen.NanoHTTPD;

public class RestServer {
	private static final Logger LOG = Logger.getLogger(RestServer.class.getName());
	
	private static final boolean SECURE = true;
	private static final int TIME_OUT = 5 * 60 * 1000;
	
	private static void start(boolean secure) {
		int port = getPort(secure);
		if (port > 0) {
			LOG.info("Start " + Application.getInstance().getClass().getSimpleName() + " rest server on port " + port + (secure ? " (Secure)" : ""));
			NanoHTTPD nanoHTTPD = new RestHTTPD(port, secure);
			try {
				nanoHTTPD.start(TIME_OUT, false); // false -> this will not start a 'java' daemon, but a normal thread which keeps JVM alive
			} catch (IOException x) {
				throw new RuntimeException(x);
			}
		}
	}
	
	private static int getPort(boolean secure) {
		String portString = Configuration.get("MjRestPort" + (secure ? "Ssl" : ""), secure ? "-1" : "8090");
		return !StringUtils.isEmpty(portString) ? Integer.valueOf(portString) : -1 ;
	}
	
	public static void start(Application application) {
		Application.setInstance(application);
		ModelTest.exitIfProblems();
		
		start(!SECURE);
        start(SECURE);
	}
	
	public static void main(String... args) {
		Application.initApplication(args);
		ModelTest.exitIfProblems();
		
		start(!SECURE);
        start(SECURE);
	}

}