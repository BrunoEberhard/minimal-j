package org.minimalj.frontend.websocket;

import org.minimalj.application.Application;
import org.minimalj.frontend.json.JsonClientToolkit;
import org.minimalj.frontend.toolkit.ClientToolkit;


public class WebSocketFrontend {
	
	public static void main(final String[] args) throws Exception {
		ClientToolkit.setToolkit(new JsonClientToolkit());
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
