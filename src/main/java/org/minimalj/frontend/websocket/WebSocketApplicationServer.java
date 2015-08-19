package org.minimalj.frontend.websocket;

import org.minimalj.application.Application;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.json.JsonFrontend;


public class WebSocketApplicationServer {
	private static final int TIME_OUT = 5 * 60 * 1000;
	
	public static void main(final String[] args) throws Exception {
		Frontend.setInstance(new JsonFrontend());
		Application.initApplication(args);
		MjWebSocketServer secureServer = new MjWebSocketServer(443, true);
		MjWebSocketServer server = new MjWebSocketServer(8080, false);
		secureServer.start(TIME_OUT);
		server.start(TIME_OUT);
		
        try {
            System.in.read();
        } catch (Throwable ignored) {
        }

        secureServer.stop();
        server.stop();
        System.out.println("Server stopped.\n");
	}

}
