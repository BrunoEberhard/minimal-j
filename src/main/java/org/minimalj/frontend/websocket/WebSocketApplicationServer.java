package org.minimalj.frontend.websocket;

import org.minimalj.application.Application;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.json.JsonFrontend;


public class WebSocketApplicationServer {
	
	public static void main(final String[] args) throws Exception {
		Frontend.setInstance(new JsonFrontend());
		Application.initApplication(args);
		MjWebSocketServer server = new MjWebSocketServer(8080);
		server.start();
		
        try {
            System.in.read();
        } catch (Throwable ignored) {
        }

        server.stop();
        System.out.println("Server stopped.\n");
	}

}
