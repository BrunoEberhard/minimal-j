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
import javax.websocket.server.ServerEndpoint;

import org.minimalj.frontend.json.JsonClientSession;
import org.minimalj.frontend.json.JsonInput;
import org.minimalj.frontend.json.JsonOutput;
import org.minimalj.frontend.json.JsonReader;

@ServerEndpoint(value = "/ws")
public class DemoWebSocketServlet {
	private static final Logger logger = Logger.getLogger(DemoWebSocketServlet.class.getName());

    private Session session;

    public DemoWebSocketServlet() {
    }

    @OnOpen
    public void start(Session session) {
        this.session = session;
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
    	
		String sessionId = (String) data.get("session");
		boolean invalidSession = sessionId != null && JsonClientSession.getSession(sessionId) == null;
		JsonClientSession session;
		if (sessionId != null && !invalidSession) {
			session = JsonClientSession.getSession(sessionId);
		} else {
			sessionId = JsonClientSession.createSession();
			session = JsonClientSession.getSession(sessionId);
		}
		
		JsonOutput output;
		if (invalidSession) {
			data = new HashMap<>();
			data.put(JsonInput.SHOW_DEFAULT_PAGE, "");
		}
		
		try {
			JsonInput input = new JsonInput(data);
			output = session.handle(input);
		} catch (Exception x) {
			output = new JsonOutput();
			output.add("error", x.getMessage());
			logger.log(Level.SEVERE, "Internal Error", x);
			// why does logger not work here?
			x.printStackTrace();
		}

		if (invalidSession) {
			output.add("error", "Invalid session");
		}
		
		output.add("session", sessionId);
		
		try {
			DemoWebSocketServlet.this.session.getBasicRemote().sendText(output.toString());
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Send response failed", e);
		}
    }


    @OnError
    public void onError(Throwable t) throws Throwable {
        logger.severe("Error: " + t.toString());
    }

}