package org.minimalj.example.demo;

import java.io.IOException;
import java.util.Map;

import org.minimalj.application.Application;
import org.minimalj.application.Configuration;
import org.minimalj.backend.Backend;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.impl.json.JsonFrontend;
import org.minimalj.frontend.impl.nanoserver.MjWebDaemon;
import org.minimalj.util.StringUtils;

import fi.iki.elonen.NanoHTTPD;

public class DemoNanoWebServer {
	private static final boolean SECURE = true;
	private static final int TIME_OUT = 5 * 60 * 1000;
	private static final ExamplesApplication application = new ExamplesApplication();
	
	private static int getPort(boolean secure) {
		String portString = Configuration.get("MjFrontendPort" + (secure ? "Ssl" : ""), secure ? "-1" : "8080");
		return !StringUtils.isEmpty(portString) ? Integer.valueOf(portString) : -1 ;
	}
	
	private static NanoHTTPD start(boolean secure) throws IOException {
		int port = getPort(secure);
		if (port > 0) {
			System.out.println("Start " + Application.getInstance().getClass().getSimpleName() + " web frontend on port " + port + (secure ? " (Secure)" : ""));
			NanoHTTPD daemon = new DemoWebDaemon(port, secure);
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
	
	public static void main(final String[] args) throws Exception {
		Application.setInstance(application);
		Frontend.setInstance(new JsonFrontend());
		Backend.setInstance(application.new ThreadLocalBackend());
		
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


	public static class DemoWebDaemon extends MjWebDaemon {
	
		public DemoWebDaemon(int port, boolean secure) {
			super(port, secure);
		}
	
		@Override
		public Response serve(String uri, Method method, Map<String, String> headers, Map<String, String> parms, Map<String, String> files) {
			String uriWithoutfile = uri.substring(0, uri.lastIndexOf('/'));
			String applicationName = uriWithoutfile.substring(uriWithoutfile.lastIndexOf('/') + 1);

			application.setCurrentApplication(applicationName);
			
			return super.serve(uri, method, headers, parms, files);
		}
		
	}

}
