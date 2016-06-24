package org.minimalj.frontend.impl.nanoserver;

import java.io.IOException;

import org.minimalj.application.Application;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.impl.json.JsonFrontend;
import org.minimalj.util.StringUtils;

import fi.iki.elonen.NanoHTTPD;

public class NanoWebServer {
	private static final boolean SECURE = true;
	private static final int TIME_OUT = 5 * 60 * 1000;
	
	private static boolean useWebSocket = Boolean.valueOf(System.getProperty("MjUseWebSocket", "false"));
	
	private static int getPort(boolean secure) {
		String portString = System.getProperty("MjFrontendPort" + (secure ? "Ssl" : ""), secure ? "-1" : "8080");
		return !StringUtils.isEmpty(portString) ? Integer.valueOf(portString) : -1 ;
	}
	
	private static NanoHTTPD start(boolean secure) throws IOException {
		int port = getPort(secure);
		if (port > 0) {
			System.out.println("Start web " + (useWebSocket ? "socket" : "") + " frontend on " + port + (secure ? " (Secure)" : ""));
			NanoHTTPD daemon = useWebSocket ? new MjWebSocketDaemon(port, secure) : new MjWebDaemon(port, secure);
			daemon.start(TIME_OUT);
			return daemon;
		} else {
			return null;
		}
	}
	
	private static void stop(NanoHTTPD daemon) {
		if (daemon != null) {
			System.out.println("Stop web frontend on " + daemon.getListeningPort());
			daemon.stop();
		}
	}
	
	public static boolean useWebSocket() {
		return useWebSocket;
	}
	
	public static void main(final String[] args) throws Exception {
		Frontend.setInstance(new JsonFrontend());
		Application.initApplication(args);
		
		NanoHTTPD daemon = null, secureDaemon = null;
        try {
        	daemon = start(!SECURE);
        	secureDaemon = start(SECURE);
            System.in.read();
        } finally {
        	stop(secureDaemon);
        	stop(daemon);
        }
	}

}
