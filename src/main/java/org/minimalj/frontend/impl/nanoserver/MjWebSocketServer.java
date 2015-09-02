package org.minimalj.frontend.impl.nanoserver;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.minimalj.frontend.impl.json.JsonHandler;
import org.minimalj.frontend.impl.nanoserver.httpd.NanoWebSocketServer;
import org.minimalj.frontend.impl.nanoserver.httpd.NanoHTTPD.Response.Status;
import org.minimalj.frontend.impl.nanoserver.httpd.NanoWebSocketServer.WebSocketFrame.CloseCode;
import org.minimalj.util.resources.Resources;

public class MjWebSocketServer extends NanoWebSocketServer {
	private static final Logger logger = Logger.getLogger(MjWebSocketServer.class.getName());

	private JsonHandler handler = new JsonHandler();
	
	public MjWebSocketServer(int port, boolean secure) {
		super(port);
		if (secure) {
			try {
				// keytool.exe -keystore mjdevkeystore.jks -keyalg RSA -keysize 3072 -genkeypair -dname "cn=localhost, ou=MJ, o=Minimal-J, c=CH" -storepass mjdev1 -keypass mjdev1
				// keytool.exe -keystore mjdevkeystore.jks -storepass mjdev1 -keypass mjdev1 -export -file mj.cer
				
				makeSecure(makeSSLSocketFactory("/mjdevkeystore.jks", "mjdev1".toCharArray()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
    public Response serve(String uri, Method method, Map<String, String> headers, Map<String, String> parms,
            Map<String, String> files) {
		if (uri.equals("/")) {
			return newChunkedResponse(Status.OK, "text/html", this.getClass().getClassLoader().getResourceAsStream("index.html"));
		} else if (uri.equals("/mj.css")) {
			return newChunkedResponse(Status.OK, "text/css", this.getClass().getClassLoader().getResourceAsStream("mj.css"));

		} else if (uri.startsWith("/") && uri.endsWith("css")) {
			return newChunkedResponse(Status.OK, "text/css", this.getClass().getClassLoader().getResourceAsStream(uri.substring(1)));

		} else if (uri.startsWith("/") && uri.endsWith("js")) {
			return newChunkedResponse(Status.OK, "application/javascript", this.getClass().getClassLoader().getResourceAsStream(uri.substring(1)));

		} else if (uri.equals("/field_error.png")) {
			return newChunkedResponse(Status.OK, "image/png", Resources.class.getResourceAsStream("icons/field_error.png"));
			
		} else {
			return newFixedLengthResponse(Status.NOT_FOUND, "text/html", uri + " not found");
		}
	}


	@Override
	protected void onPong(WebSocket webSocket, WebSocketFrame pongFrame) {
		// TODO Auto-generated method stub
	}

	@Override
	protected void onMessage(WebSocket webSocket, WebSocketFrame messageFrame) {
		String result = handler.handle(messageFrame.getTextPayload());
		try {
			webSocket.send(result);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Send response failed", e);
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
