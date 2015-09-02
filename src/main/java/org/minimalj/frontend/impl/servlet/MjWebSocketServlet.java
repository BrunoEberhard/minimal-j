package org.minimalj.frontend.impl.servlet;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.minimalj.frontend.impl.json.JsonHandler;

@ServerEndpoint(value = "/ws")
public class MjWebSocketServlet {
	private static final Logger logger = Logger.getLogger(MjWebSocketServlet.class.getName());

	private static JsonHandler handler = new JsonHandler();
	
    private Session webSocketSession;

    public MjWebSocketServlet() {
    }

    @OnOpen
    public void start(Session webSocketSession) {
        this.webSocketSession = webSocketSession;
    }

    @OnClose
    public void end() {
    }

    @OnMessage
    public void incoming(String message) {
    	String result = handler.handle(message);
    	try{
    		MjWebSocketServlet.this.webSocketSession.getBasicRemote().sendText(result);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Send response failed", e);
		}
    }


    @OnError
    public void onError(Throwable t) throws Throwable {
        logger.severe("Error: " + t.toString());
    }

}