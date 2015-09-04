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

		String pagename = (String) data.get("pagename");
		if (pagename.endsWith(".html")) {
			pagename = pagename.substring(0, pagename.length() - 5);
		}
    	DemoContext.setContext(pagename);
    	
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