package org.minimalj.undertow;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Map;

import org.minimalj.frontend.impl.json.JsonInput;
import org.minimalj.frontend.impl.json.JsonOutput;
import org.minimalj.frontend.impl.json.JsonPageManager;
import org.minimalj.frontend.impl.json.JsonReader;
import org.minimalj.frontend.impl.web.MjHttpExchange;
import org.minimalj.frontend.impl.web.MjHttpHandler;
import org.minimalj.util.StringUtils;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import io.undertow.websockets.spi.WebSocketHttpExchange;

public class MinimalTowHandler implements HttpHandler, WebSocketConnectionCallback {

	private MjHttpHandler handler = new MjHttpHandler();

	@Override
	public void handleRequest(HttpServerExchange exchange) throws Exception {
		MjHttpExchange mjExchange = new MjHttpExchange() {

			@Override
			public void sendResponse(String body) throws IOException {
				exchange.getResponseSender().send(body);
			}

			@Override
			public void sendResponse(byte[] bytes) throws IOException {
				exchange.setResponseContentLength(bytes.length);
				exchange.startBlocking();
				OutputStream os = exchange.getOutputStream();
				os.write(bytes);
				exchange.getResponseSender().close();
			}

			@Override
			public void sendNotfound() {
				exchange.setStatusCode(404);
			}

			@Override
			public void sendForbidden() {
				exchange.setStatusCode(400);
			}

			@Override
			public void sendError() {
				exchange.setStatusCode(500);
			}

			@Override
			public InputStream getRequest() throws IOException {
				return exchange.getInputStream();
			}

			@Override
			public String getPath() {
				return exchange.getRelativePath();
			}

			@Override
			public Locale getLocale() {
				return MjHttpExchange.getLocale(exchange.getRequestHeaders().get("accept-language").getFirst());
			}
		};

		exchange.dispatch(() -> {
			handler.handle(mjExchange);
		});
	}

	@Override
	public void onConnect(WebSocketHttpExchange exchange, WebSocketChannel channel) {
		channel.getReceiveSetter().set(new AbstractReceiveListener() {
			private JsonPageManager session;

			@Override
			protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) {
				@SuppressWarnings("unchecked")
				Map<String, Object> data = (Map<String, Object>) JsonReader.read(message.getData());
				String sessionId = (String) data.get("session");
				if (session == null || !StringUtils.equals(sessionId, session.getSessionId())) {
					session = handler.getSessionManager().getSession(data);
				}
				JsonInput input = new JsonInput(data);
				JsonOutput output;
				synchronized (session) {
					output = session.handle(input);
				}
				WebSockets.sendText(output.toString(), channel, null);
			}
		});
		channel.resumeReceives();
	}

}
