package org.minimalj.frontend.impl.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;
import java.util.Locale.LanguageRange;
import java.util.Map;
import java.util.Objects;

import org.minimalj.application.Application;
import org.minimalj.frontend.impl.json.JsonFrontend;
import org.minimalj.frontend.impl.json.JsonReader;
import org.minimalj.frontend.impl.json.JsonSessionManager;
import org.minimalj.frontend.page.Page;
import org.minimalj.util.resources.Resources;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class MjHttpHandler implements HttpHandler {
	private JsonSessionManager sessionManager = new JsonSessionManager();

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		URI uri = exchange.getRequestURI();
		String path = uri.getPath();
		OutputStream os = exchange.getResponseBody();
		if (path.equals("/ajax_request.xml")) {
			Map<String, Object> data = (Map<String, Object>) JsonReader.read(exchange.getRequestBody());
			String response = sessionManager.handle(data);
			sendResponse(exchange, response);
		} else if (path.equals("/application.png")) {
			exchange.sendResponseHeaders(200, 0);
			// Application.getInstance().getIcon().transferTo(os);
			transferTo(Application.getInstance().getIcon(), os);
		} else if (path.contains(".")) {
			if (path.contains("..")) {
				exchange.sendResponseHeaders(400, -1);
				return;
			}
			
			String uriString = uri.toString();
			int index = uriString.lastIndexOf('.');
			if (index > -1 && index < uriString.length() - 1) {
				String postfix = uriString.substring(index + 1);
				String mimeType = Resources.getMimeType(postfix);
				if (mimeType != null) {
					exchange.sendResponseHeaders(200, 0);
					InputStream inputStream = MjHttpHandler.class.getResourceAsStream(uriString);
//					inputStream.transferTo(exchange.getResponseBody());
					transferTo(inputStream, exchange.getResponseBody());
					os.close();
					return;
				}
			}

			exchange.sendResponseHeaders(404, -1);
			return;
		} else {
			if (path.length() > 1 && !Page.validateRoute(path.substring(1))) {
				exchange.sendResponseHeaders(400, -1);
				return;
			}
			String htmlTemplate = JsonFrontend.getHtmlTemplate();
			Locale locale = getLocale(exchange.getRequestHeaders().getFirst("accept-language"));
			htmlTemplate = htmlTemplate.replace("$SEND", WebServer.useWebSocket ? "sendWebSocket" : "sendAjax");
			String html = JsonFrontend.fillPlaceHolder(htmlTemplate, locale, path);
			sendResponse(exchange, html);
		}
	}

	private static final int TRANSFER_BUFFER_SIZE = 8192;

	// JDK 9
	public static long transferTo(InputStream is, OutputStream out) throws IOException {
		Objects.requireNonNull(out, "out");
		long transferred = 0;
		byte[] buffer = new byte[TRANSFER_BUFFER_SIZE];
		int read;
		while ((read = is.read(buffer, 0, TRANSFER_BUFFER_SIZE)) >= 0) {
			out.write(buffer, 0, read);
			transferred += read;
		}
		return transferred;
	}

	protected void sendResponse(HttpExchange exchange, String response) throws IOException {
		OutputStream os = exchange.getResponseBody();
		byte[] bytes = response.getBytes(Charset.forName("UTF-8"));
		exchange.sendResponseHeaders(200, bytes.length);
		os.write(bytes);
		os.close();
	}
	
	public static Locale getLocale(String userLocale) {
		if (userLocale == null) {
			return Locale.getDefault();
		}
		List<LanguageRange> ranges = Locale.LanguageRange.parse(userLocale);
		for (LanguageRange languageRange : ranges) {
			String localeString = languageRange.getRange();
			return Locale.forLanguageTag(localeString);
		}
		return Locale.getDefault();
	}

}
