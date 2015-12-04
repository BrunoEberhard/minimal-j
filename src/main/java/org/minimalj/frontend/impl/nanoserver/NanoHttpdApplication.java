package org.minimalj.frontend.impl.nanoserver;

import org.minimalj.application.Application;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.impl.json.JsonFrontend;

public class NanoHttpdApplication {
	private static final int TIME_OUT = 5 * 60 * 1000;
	
	public static void main(final String[] args) throws Exception {
		Frontend.setInstance(new JsonFrontend());
		Application.initApplication(args);
		MjWebSocketDaemon secureWebSocketDaemon = new MjWebSocketDaemon(443, true);
		MjWebSocketDaemon webSocketDaemon = new MjWebSocketDaemon(8080, false);
		secureWebSocketDaemon.start(TIME_OUT);
		webSocketDaemon.start(TIME_OUT);
		
        try {
            System.in.read();
        } catch (Throwable ignored) {
        }

        secureWebSocketDaemon.stop();
        webSocketDaemon.stop();
        System.out.println("Server stopped.\n");
	}

}
