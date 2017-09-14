package org.minimalj.example.demo;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.logging.Logger;

import org.minimalj.application.Application;
import org.minimalj.application.Configuration;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.impl.json.JsonFrontend;
import org.minimalj.frontend.impl.nanoserver.MjWebDaemon;
import org.minimalj.util.StringUtils;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;

public class DemoNanoWebServer {
	private static final Logger LOG = Logger.getLogger(DemoNanoWebServer.class.getName());
	
	private static final boolean SECURE = true;
	private static final int TIME_OUT = 5 * 60 * 1000;
	
	private static ExamplesApplication application;
	
	private static void start(boolean secure) {
		int port = getPort(secure);
		if (port > 0) {
			LOG.info("Start " + Application.getInstance().getClass().getSimpleName() + " web frontend on port " + port + (secure ? " (Secure)" : ""));
			NanoHTTPD nanoHTTPD = new DemoWebDaemon(port, secure);
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
	
	public static void start(ExamplesApplication application) {
		DemoNanoWebServer.application = application;

		Application.setInstance(application);
		Frontend.setInstance(new JsonFrontend());
		
		start(!SECURE);
        start(SECURE);
	}

	public static class DemoWebDaemon extends MjWebDaemon {
	
		public DemoWebDaemon(int port, boolean secure) {
			super(port, secure);
		}
	
		@Override
		public Response serve(String uriString, Method method, Map<String, String> headers, Map<String, String> parms, Map<String, String> files) {
			URI uri = URI.create(uriString);
			String path = uri.getPath();
			if (path.length() < 2) {
				return NanoHTTPD.newFixedLengthResponse(Status.BAD_REQUEST, "text/html", uriString + " invalid");
			}
			int applicationNameEnd = path.indexOf('/', 1);
			if (applicationNameEnd < 0) {
				return NanoHTTPD.newFixedLengthResponse(Status.BAD_REQUEST, "text/html", uriString + " invalid");
			}

			String applicationName = path.substring(1, applicationNameEnd);
			application.setCurrentApplication(applicationName);
			
			String uriWithoutApplicationName = path.substring(applicationNameEnd);
			return super.serve(uriWithoutApplicationName, method, headers, parms, files);
		}
		
	}

}
