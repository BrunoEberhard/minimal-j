package org.minimalj.example.demo;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.minimalj.application.Application;
import org.minimalj.frontend.impl.json.JsonPageManager;

@ServerEndpoint(value = "/ws/{application}")
public class DemoWebSocketServlet {
	private static final Logger logger = Logger.getLogger(DemoWebSocketServlet.class.getName());

	private Map<Session, JsonPageManager> pageManagers = new HashMap<>();
	
    public DemoWebSocketServlet() {
    }

    @OnOpen
    public void start(Session session) {
    	pageManagers.put(session, new JsonPageManager());
    }

    @OnClose
    public void end(Session session) {
    	pageManagers.remove(session);
    }

    @OnMessage
    public void incoming(@PathParam("application") String applicationName, String message, Session session) {
    	((ExamplesApplication) Application.getInstance()).setCurrentApplication(applicationName);
    	
    	JsonPageManager pageManager = pageManagers.get(session);
    	String result = pageManager.handle(message);
    	try{
    		session.getBasicRemote().sendText(result);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Send response failed", e);
		}
    }

    @OnError
    public void onError(Throwable t) throws Throwable {
        logger.severe("Error: " + t.toString());
    }
}