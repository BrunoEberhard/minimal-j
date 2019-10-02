package org.minimalj.frontend.impl.web;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.minimalj.application.Configuration;
import org.minimalj.frontend.impl.json.JsonInput;
import org.minimalj.frontend.impl.json.JsonOutput;
import org.minimalj.frontend.impl.json.JsonPageManager;
import org.minimalj.frontend.impl.json.JsonReader;
import org.minimalj.frontend.impl.json.JsonSessionManager;
import org.minimalj.util.StringUtils;

import fi.iki.elonen.NanoWSD;
import fi.iki.elonen.NanoWSD.WebSocketFrame.CloseCode;

// its not possible to extend NanoWSD and MjWebDaemon at the same time
// if MjWebDaemon would extends NanoWSD then the nanohttpd-websocket dependency
// would be needed even if no websockets are used. 
public class MjWebSocketDaemon extends NanoWSD {
	private static final Logger logger = Logger.getLogger(MjWebSocketDaemon.class.getName());

	private JsonSessionManager sessionManager = new JsonSessionManager();
	
	public MjWebSocketDaemon(int port, boolean secure) {
		super(port);
		if (secure) {
			try {
				// see notes in MjWebDaemon
				
				String keyAndTrustStoreClasspathPath = Configuration.get("MjKeystore"); // in example '/mjdevkeystore.jks'
				char[] passphrase = Configuration.get("MjKeystorePassphrase").toCharArray(); //  ub example 'mjdev1'
				
				makeSecure(makeSSLSocketFactory(keyAndTrustStoreClasspathPath, passphrase), null);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	protected WebSocket openWebSocket(IHTTPSession handshake) {
		return new MjWebSocket(handshake);
	}
	
	@Override
    public Response serve(String uri, Method method, Map<String, String> headers, Map<String, String> parms,
            Map<String, String> files) {
		return MjWebDaemon.serve(sessionManager, uri, method, headers, parms, files);
	}

	public class MjWebSocket extends WebSocket {
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
			Map<String, Object> data = (Map<String, Object>) JsonReader.read(message.getTextPayload());
			String sessionId = (String) data.get("session");
			if (session == null || !StringUtils.equals(sessionId, session.getSessionId())) {
				session = sessionManager.getSession(data);
			}
			JsonInput input = new JsonInput(data);
			JsonOutput output;
			synchronized (session) {
				output = session.handle(input);
			}
			try {
				send(output.toString());
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Send response failed", e);
			}
		}
		
		@Override
		protected void onPong(WebSocketFrame pong) {
			// nothing to do
		}

		@Override
		protected void onClose(CloseCode code, String reason, boolean initiatedByRemote) {
			logger.fine("Close " + reason + " / " + initiatedByRemote);
		}

		@Override
		protected void onException(IOException exception) {
			// nothing to do
		}
	}
	
}
