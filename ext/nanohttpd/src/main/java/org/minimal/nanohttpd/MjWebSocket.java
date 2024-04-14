package org.minimal.nanohttpd;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.minimalj.frontend.impl.json.JsonInput;
import org.minimalj.frontend.impl.json.JsonOutput;
import org.minimalj.frontend.impl.json.JsonPageManager;
import org.minimalj.frontend.impl.json.JsonReader;
import org.minimalj.frontend.impl.json.JsonSessionManager;
import org.minimalj.util.StringUtils;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoWSD.WebSocket;
import fi.iki.elonen.NanoWSD.WebSocketFrame;
import fi.iki.elonen.NanoWSD.WebSocketFrame.CloseCode;

public class MjWebSocket extends WebSocket {
	private static final Logger LOG = Logger.getLogger(MjWebSocket.class.getName());

	private JsonPageManager session;
	
	public MjWebSocket(IHTTPSession handshakeRequest) {
		super(handshakeRequest);
	}

	@Override
	protected void onOpen() {
		// nothing to do
	}

	@Override
	protected void onMessage(WebSocketFrame message) {
		@SuppressWarnings("unchecked")
		Map<String, Object> data = (Map<String, Object>) JsonReader.read(message.getTextPayload());
		String sessionId = (String) data.get("session");
		if (session == null || !StringUtils.equals(sessionId, session.getSessionId())) {
			session = JsonSessionManager.getInstance().getSession(data);
		}
		JsonInput input = new JsonInput(data);
		JsonOutput output;
		synchronized (session) {
			output = session.handle(input);
		}
		try {
			send(output.toString());
		} catch (IOException e) {
			LOG.log(Level.SEVERE, "Send response failed", e);
		}
	}
	
	@Override
	protected void onPong(WebSocketFrame pong) {
		// nothing to do
	}

	@Override
	protected void onClose(CloseCode code, String reason, boolean initiatedByRemote) {
		LOG.fine("Close " + reason + " / " + initiatedByRemote);
	}

	@Override
	protected void onException(IOException exception) {
		// nothing to do
	}
}