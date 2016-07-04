package org.minimalj.frontend.impl.nanoserver;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.minimalj.application.Application;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.impl.json.JsonFrontend;
import org.minimalj.util.StringUtils;

import fi.iki.elonen.NanoHTTPD;

public class NanoWebServer {
	private static final Logger LOG = Logger.getLogger(NanoWebServer.class.getName());
	
	private static final boolean SECURE = true;
	private static final int TIME_OUT = 5 * 60 * 1000;
	
	private static boolean useWebSocket = Boolean.valueOf(System.getProperty("MjUseWebSocket", "false"));
	
	private static void start(boolean secure) throws IOException {
		int port = getPort(secure);
		if (port > 0) {
			LOG.info("Start web " + (useWebSocket ? "socket" : "") + " frontend on " + port + (secure ? " (Secure)" : ""));
			NanoHTTPD nanoHTTPD = useWebSocket ? newMjWebSocketDaemon(port, secure) : new MjWebDaemon(port, secure);
			nanoHTTPD.start(TIME_OUT, false); // false -> this will not start a 'java' daemon, but a normal thread which keeps JVM alive
		}
	}
	
	private static int getPort(boolean secure) {
		String portString = System.getProperty("MjFrontendPort" + (secure ? "Ssl" : ""), null);
		if (!StringUtils.isEmpty(portString) && !Character.isDigit(portString.charAt(0))) {
			portString = System.getProperty(portString, null);
		}
		if (portString == null) {
			return secure ? -1 : 8080;
		} else if (portString.length() == 0) {
			// make it possible to deactive non secure port by add -DMjFrontendPort=""
			return -1;
		} else {
		    return Integer.valueOf(portString);	
		}
	}
	
	private static NanoHTTPD newMjWebSocketDaemon(int port, boolean secure) {
		try {
			return new MjWebSocketDaemonFactory().create(port, secure);
		} catch (NoClassDefFoundError e) {
			LOG.log(Level.SEVERE, "Probably the dependency to nanohttpd-websocket is missing");
			throw (e);
		}
	}
	
	// Without this factory the dependency to nanohttpd-websocket would be
	// needed even if the websockets are not used
	private static class MjWebSocketDaemonFactory {
		
		public NanoHTTPD create(int port, boolean secure) {
			return new MjWebSocketDaemon(port, secure);
		}
	}
	
	public static void main(final String[] args) throws Exception {
		Frontend.setInstance(new JsonFrontend());
		Application.initApplication(args);
		
		start(!SECURE);
        start(SECURE);
	}

}
