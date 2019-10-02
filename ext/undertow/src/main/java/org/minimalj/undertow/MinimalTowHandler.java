package org.minimalj.undertow;

import java.io.InputStream;
import java.util.Locale;
import java.util.Map;

import org.minimalj.application.Application;
import org.minimalj.frontend.impl.json.JsonFrontend;
import org.minimalj.frontend.impl.json.JsonInput;
import org.minimalj.frontend.impl.json.JsonOutput;
import org.minimalj.frontend.impl.json.JsonPageManager;
import org.minimalj.frontend.impl.json.JsonReader;
import org.minimalj.frontend.impl.json.JsonSessionManager;
import org.minimalj.frontend.impl.web.MjWebDaemon;
import org.minimalj.frontend.impl.web.WebServer;
import org.minimalj.frontend.page.Page;
import org.minimalj.util.StringUtils;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.util.Headers;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import io.undertow.websockets.spi.WebSocketHttpExchange;

public class MinimalTowHandler implements HttpHandler, WebSocketConnectionCallback {

	private JsonSessionManager sessionManager = new JsonSessionManager();
	private ResourceHandler resourceHandler = new ResourceHandler(new ClassPathResourceManager(this.getClass().getClassLoader()));

	@Override
	public void handleRequest(HttpServerExchange exchange) throws Exception {
		// TODO rewrite this in Undertow style
		String path = exchange.getRelativePath();
		if (path.equals("/ajax_request.xml")) {
			exchange.dispatch(() -> {
				exchange.startBlocking();
				String data = sessionManager.handle(exchange.getInputStream());
				exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/json");
				exchange.getResponseSender().send(data);
				exchange.getResponseSender().close();
			});
		} else if (path.equals("/application.png")) {
			exchange.dispatch(() -> {
				exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "img/png");
				exchange.startBlocking();
				// Java 9: exchange.getResponseSender().send(new ByteBuffer(is.readAllBytes()));
				InputStream is = Application.getInstance().getIcon();
				int b;
				try {
					while ((b = is.read()) > -1) {
						exchange.getOutputStream().write(b);
					}
				} catch (Exception x) {
					throw new RuntimeException(x);
				}
				exchange.getResponseSender().close();
			});
		} else if (path.contains(".")) {
			if (path.contains("..")) {
				exchange.setStatusCode(400);
				return;
			}
			exchange.dispatch(resourceHandler);
		} else {
			if (path.length() > 1 && !Page.validateRoute(path.substring(1))) {
				exchange.setStatusCode(400);
				return;
			}
			String htmlTemplate = JsonFrontend.getHtmlTemplate();
			Locale locale = MjWebDaemon.getLocale(exchange.getRequestHeaders().get("accept-language").getFirst());
			htmlTemplate = htmlTemplate.replace("$SEND", WebServer.useWebSocket ? "sendWebSocket" : "sendAjax");
			String html = JsonFrontend.fillPlaceHolder(htmlTemplate, locale, path);
			exchange.getResponseSender().send(html);
		}
	}

	@Override
	public void onConnect(WebSocketHttpExchange exchange, WebSocketChannel channel) {
		channel.getReceiveSetter().set(new AbstractReceiveListener() {
			private JsonPageManager session;

			@Override
			protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) {
				Map<String, Object> data = (Map<String, Object>) JsonReader.read(message.getData());
				String sessionId = (String) data.get("session");
				if (session == null || !StringUtils.equals(sessionId, session.getSessionId())) {
					session = sessionManager.getSession(data);
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
