package org.minimalj.frontend.impl.nanoserver;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Locale.LanguageRange;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.minimalj.frontend.impl.json.JsonFrontend;
import org.minimalj.frontend.impl.json.JsonHandler;
import org.minimalj.util.resources.Resources;

import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.NanoWebSocketServer;
import fi.iki.elonen.NanoWebSocketServer.WebSocketFrame.CloseCode;

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
	
	private static Locale getLocale(String userLocale) {
        final List<LanguageRange> ranges = Locale.LanguageRange.parse(userLocale);
        if (ranges != null) {
        	for (LanguageRange languageRange : ranges) {
                final String localeString = languageRange.getRange();
                final Locale locale = Locale.forLanguageTag(localeString);
                return locale;
            }
        }
        return null;
	}
	
	@Override
    public Response serve(String uri, Method method, Map<String, String> headers, Map<String, String> parms,
            Map<String, String> files) {
		if (uri.equals("/")) {
			String htmlTemplate = JsonFrontend.getHtmlTemplate();
			Locale locale = getLocale(headers.get("accept-language"));
			String html = JsonFrontend.fillPlaceHolder(htmlTemplate, locale);
			return newFixedLengthResponse(Status.OK, "text/html", html);
		} else {
			int index = uri.lastIndexOf('.');
			if (index > -1 && index < uri.length()-1) {
				String postfix = uri.substring(index+1);
				String mimeType = Resources.getMimeType(postfix);
				if (mimeType != null) {
					InputStream inputStream = Resources.getInputStream(uri.substring(1));
					return newChunkedResponse(Status.OK, mimeType, inputStream);
				}
			}
		}
		logger.warning("Could not serve: " + uri);
		return newFixedLengthResponse(Status.NOT_FOUND, "text/html", uri + " not found");
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
		logger.fine("Close " + reason + " / " + initiatedByRemote);
	}

	@Override
	protected void onException(WebSocket webSocket, IOException e) {
		// TODO Auto-generated method stub
	}
	
}
