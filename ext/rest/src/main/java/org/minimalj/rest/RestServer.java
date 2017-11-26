package org.minimalj.rest;

import java.io.IOException;
import java.util.logging.Logger;

import org.minimalj.application.Application;
import org.minimalj.application.Configuration;
import org.minimalj.frontend.impl.nanoserver.NanoWebServer;
import org.minimalj.model.test.ModelTest;
import org.minimalj.util.StringUtils;

import fi.iki.elonen.NanoHTTPD;

public class RestServer {
	private static final Logger LOG = Logger.getLogger(RestServer.class.getName());
	
	private static final boolean SECURE = true;
	private static final int TIME_OUT = 5 * 60 * 1000;
	
	public static void start(boolean secure) {
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
	
	public static void start() {
		if (Application.getInstance().getEntityClasses().length == 0) {
			LOG.severe("Server not started! You must have at least declare one entity class in your application. Please override 'getEntityClasses'.");
			return;
		}
		
		ModelTest.exitIfProblems();
		
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
	
	/**
	 * To use inner classes as main class you have to use
	 * <pre>
	 * java org.minimalj.rest.RestServer$WithFrontend
	 * </pre>
	 * 
	 * Note the $ instead of .
	 */
	public static class WithFrontend {

		public static void main(String[] args) {
			Application.initApplication(args);
			NanoWebServer.start();
			RestServer.start();
		}
	}

}