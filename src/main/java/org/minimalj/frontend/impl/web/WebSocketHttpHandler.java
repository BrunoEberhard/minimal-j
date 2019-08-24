package org.minimalj.frontend.impl.web;

import java.io.IOException;
import java.util.Map;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoWSD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoWSD.WebSocketFrame;
import fi.iki.elonen.NanoWSD.WebSocketFrame.CloseCode;

public abstract class WebSocketHttpHandler implements HttpHandler {

    public static final String HEADER_UPGRADE = "upgrade";
    public static final String HEADER_UPGRADE_VALUE = "websocket";
    public static final String HEADER_CONNECTION = "connection";
    public static final String HEADER_CONNECTION_VALUE = "Upgrade";
    public static final String HEADER_WEBSOCKET_VERSION = "sec-websocket-version";
    public static final String HEADER_WEBSOCKET_VERSION_VALUE = "13";
    public static final String HEADER_WEBSOCKET_KEY = "sec-websocket-key";
    public static final String HEADER_WEBSOCKET_ACCEPT = "sec-websocket-accept";
    public static final String HEADER_WEBSOCKET_PROTOCOL = "sec-websocket-protocol";
    private final static String WEBSOCKET_KEY_MAGIC = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

    protected abstract void onOpen();

    protected abstract void onClose(CloseCode code, String reason, boolean initiatedByRemote);

    protected abstract void onMessage(WebSocketFrame message);

    protected abstract void onPong(WebSocketFrame pong);

    protected abstract void onException(IOException exception);
        
	@Override
	public void handle(HttpExchange exchange) throws IOException {
    	Headers headers = exchange.getRequestHeaders();
		if (isWebsocketRequested(headers)) {
            if (!HEADER_WEBSOCKET_VERSION_VALUE.equalsIgnoreCase(headers.getFirst(HEADER_WEBSOCKET_VERSION))) {
            	exchange.sendResponseHeaders(400, -1);
            	return;
//            	return newFixedLengthResponse(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT,
//                        "Invalid Websocket-Version " + headers.get(NanoWSD.HEADER_WEBSOCKET_VERSION));
            }
		
            if (!headers.containsKey(HEADER_WEBSOCKET_KEY)) {
            	exchange.sendResponseHeaders(400, -1);
            	return;
//                return newFixedLengthResponse(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, "Missing Websocket-Key");
            }
            
		}
		
		handleHttp(exchange);
	}
	
    protected boolean isWebsocketRequested(Headers headers) {
        String upgrade = headers.getFirst(NanoWSD.HEADER_UPGRADE);
        boolean isCorrectConnection = isWebSocketConnectionHeader(headers);
        boolean isUpgrade = NanoWSD.HEADER_UPGRADE_VALUE.equalsIgnoreCase(upgrade);
        return isUpgrade && isCorrectConnection;
    }
    
    private boolean isWebSocketConnectionHeader(Headers headers) {
        String connection = headers.getFirst(HEADER_CONNECTION);
        return connection != null && connection.toLowerCase().contains(HEADER_CONNECTION_VALUE.toLowerCase());
    }
	
    public abstract void handleHttp(HttpExchange exchange) throws IOException;

	
}
