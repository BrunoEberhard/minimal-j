package org.minimalj.frontend.impl.servlet;

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

import org.minimalj.frontend.impl.json.JsonPageManager;

public class MjWebSocketServlet {
	private static final Logger logger = Logger.getLogger(MjWebSocketServlet.class.getName());

	private Map<Session, JsonPageManager> pageManagers = new HashMap<>();
	
    public MjWebSocketServlet() {
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
    public void incoming(String message, Session session) {
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
        logger.severe("Error: " + t);
    }
}