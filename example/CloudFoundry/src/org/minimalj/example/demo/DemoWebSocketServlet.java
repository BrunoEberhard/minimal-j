package org.minimalj.example.demo;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.minimalj.frontend.impl.json.JsonHandler;
import org.minimalj.frontend.impl.json.JsonReader;

@ServerEndpoint(value = "/wsDemo")
public class DemoWebSocketServlet {
	private static final Logger logger = Logger.getLogger(DemoWebSocketServlet.class.getName());

	private static JsonHandler handler = new JsonHandler();
	
    private Session webSocketSession;

    public DemoWebSocketServlet() {
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
    	Map<String, Object> data = (Map<String, Object>) new JsonReader().read(message);

		String pathname = (String) data.get("context");
		if (pathname.endsWith("/")) {
			pathname = pathname.substring(0, pathname.length() - 1);
		}
    	int start = pathname.lastIndexOf("/");
    	String context = pathname.substring(start+1);
    	DemoContext.setContext(context);
    	
    	String result = handler.handle(data);
    	
    	try{
    		DemoWebSocketServlet.this.webSocketSession.getBasicRemote().sendText(result);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Send response failed", e);
		}
    }


    @OnError
    public void onError(Throwable t) throws Throwable {
        logger.severe("Error: " + t.toString());
    }

}