package org.minimalj.example.demo;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.minimalj.application.Application;
import org.minimalj.backend.Backend;
import org.minimalj.example.empty.EmptyApplication;
import org.minimalj.example.helloworld.HelloWorldApplication;
import org.minimalj.example.helloworld2.GreetingApplication;
import org.minimalj.example.library.MjExampleApplication;
import org.minimalj.example.notes.NotesApplication;
import org.minimalj.example.numbers.NumbersApplication;
import org.minimalj.example.petclinic.PetClinicApplication;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.impl.json.JsonFrontend;
import org.minimalj.frontend.impl.nanoserver.MjWebDaemon;
import org.minimalj.util.StringUtils;

import fi.iki.elonen.NanoHTTPD;

public class NanoHttpdDemoApplication {
	private static final boolean SECURE = true;
	private static final int TIME_OUT = 5 * 60 * 1000;
	
	private static int getPort(boolean secure) {
		String portString = System.getProperty("MjFrontendPort" + (secure ? "Ssl" : ""), secure ? "-1" : "8080");
		return !StringUtils.isEmpty(portString) ? Integer.valueOf(portString) : -1 ;
	}
	
	private static NanoHTTPD start(boolean secure) throws IOException {
		int port = getPort(secure);
		if (port > 0) {
			System.out.println("Start web frontend on " + port + (secure ? " (Secure)" : ""));
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
		Frontend.setInstance(new JsonFrontend());
		
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
	
		private static final Map<String, Application> applications = new HashMap<>();
		private static  final Map<String, Backend> backends = new HashMap<>();

		static {
			applications.put("empty", new EmptyApplication());
			applications.put("notes", new NotesApplication());
			applications.put("helloWorld", new HelloWorldApplication());
			applications.put("greeting", new GreetingApplication());
			applications.put("numbers", new NumbersApplication());
			applications.put("library", new MjExampleApplication());
			applications.put("petClinic", new PetClinicApplication());
			
			applications.keySet().forEach((applicationName) -> backends.put(applicationName, new Backend()));
		}
		
		public DemoWebDaemon(int port, boolean secure) {
			super(port, secure);
		}
	
		@Override
		public Response serve(String uri, Method method, Map<String, String> headers, Map<String, String> parms, Map<String, String> files) {
			String uriWithoutfile = uri.substring(0, uri.lastIndexOf('/'));
			String applicationName = uriWithoutfile.substring(uriWithoutfile.lastIndexOf('/') + 1);

			Backend.setInstance(backends.get(applicationName));
			Application.setInstance(applications.get(applicationName));
			
			return super.serve(uri, method, headers, parms, files);
		}
		
	}

}
