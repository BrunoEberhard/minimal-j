package org.minimalj.frontend.websocket;

import java.io.IOException;
import java.util.Map;

import org.minimalj.frontend.json.JsonClientSession;
import org.minimalj.frontend.json.JsonInput;
import org.minimalj.frontend.json.JsonOutput;
import org.minimalj.frontend.json.JsonReader;
import org.minimalj.util.resources.Resources;

import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.NanoWebSocketServer;
import fi.iki.elonen.NanoWebSocketServer.WebSocketFrame.CloseCode;

public class MjWebSocketServer extends NanoWebSocketServer {

	public MjWebSocketServer(int port) {
		super(port);
	}

	@Override
    public Response serve(String uri, Method method, Map<String, String> headers, Map<String, String> parms,
            Map<String, String> files) {
		if (uri.equals("/")) {
			return new Response(Status.OK, "text/html", this.getClass().getClassLoader().getResourceAsStream("index.html"));
		} else if (uri.equals("/mj.css")) {
			return new Response(Status.OK, "text/css", this.getClass().getClassLoader().getResourceAsStream("mj.css"));

		} else if (uri.startsWith("/") && uri.endsWith("css")) {
			return new Response(Status.OK, "text/css", this.getClass().getClassLoader().getResourceAsStream(uri.substring(1)));

		} else if (uri.startsWith("/") && uri.endsWith("js")) {
			return new Response(Status.OK, "application/javascript", this.getClass().getClassLoader().getResourceAsStream(uri.substring(1)));

		} else if (uri.equals("/field_error.png")) {
			return new Response(Status.OK, "image/png", Resources.class.getResourceAsStream("icons/field_error.png"));
			
		} else {
			return new Response(Status.NOT_FOUND, "text/html", uri + " not found");
		}
	}


	@Override
	protected void onPong(WebSocket webSocket, WebSocketFrame pongFrame) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onMessage(WebSocket webSocket, WebSocketFrame messageFrame) {
		Map<String, Object> data = (Map<String, Object>) new JsonReader().read(messageFrame.getTextPayload());
		String sessionId = (String) data.get("session");
		if (sessionId == null) {
			sessionId = JsonClientSession.createSession();
		}
		JsonClientSession session = JsonClientSession.getSession(sessionId);
		JsonInput input = new JsonInput(data);
		JsonOutput output = session.handle(input);
		output.add("session", sessionId);
		
		try {
			webSocket.send(output.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void onClose(WebSocket webSocket, CloseCode code, String reason, boolean initiatedByRemote) {
		System.out.println("Close " + reason + " / " + initiatedByRemote);

	}

	@Override
	protected void onException(WebSocket webSocket, IOException e) {
		// TODO Auto-generated method stub

	}

}
