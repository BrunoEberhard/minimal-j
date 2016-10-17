package org.minimalj.frontend.impl.nanoserver;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.minimalj.application.Configuration;
import org.minimalj.frontend.impl.json.JsonPageManager;
import org.minimalj.frontend.impl.json.JsonSessionManager;

import fi.iki.elonen.NanoWSD;
import fi.iki.elonen.NanoWSD.WebSocketFrame.CloseCode;

// its not possible to extend NanoWSD and MjWebDaemon at the same time
// if MjWebDaemon would extends NanoWSD then the nanohttpd-websocket dependency
// would be needed even if no websockets are used. 
public class MjWebSocketDaemon extends NanoWSD {
	private static final Logger logger = Logger.getLogger(MjWebSocketDaemon.class.getName());

	// sessionManager could also be ommitted. With WebSocket the SessionManagement is
	// done by keeping a session per connection. This sessionManager would only be used
	// for ajax calls which should not occur in this setup
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
		
		private JsonPageManager pageManager = new JsonPageManager();
		
		public MjWebSocket(IHTTPSession handshakeRequest) {
			super(handshakeRequest);
		}

		@Override
		protected void onOpen() {
			// nothing to do
		}

		@Override
		protected void onMessage(WebSocketFrame message) {
			String result = pageManager.handle(message.getTextPayload());
			try {
				send(result);
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
