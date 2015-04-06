package org.minimalj.frontend.websocket;

import java.io.IOException;
import java.util.Map;

import org.minimalj.frontend.json.JsonClientSession;
import org.minimalj.frontend.json.JsonInput;
import org.minimalj.frontend.json.JsonOutput;
import org.minimalj.frontend.json.JsonReader;

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
		Response response = new Response(Status.OK, "text/html", this.getClass().getClassLoader().getResourceAsStream("index.html"));
		return response;
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
		
		try {
			webSocket.send(output.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void onClose(WebSocket webSocket, CloseCode code, String reason, boolean initiatedByRemote) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onException(WebSocket webSocket, IOException e) {
		// TODO Auto-generated method stub

	}

}
