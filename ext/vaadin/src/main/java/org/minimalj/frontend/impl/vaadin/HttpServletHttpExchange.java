package org.minimalj.frontend.impl.vaadin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.minimalj.frontend.impl.web.MjHttpExchange;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class HttpServletHttpExchange extends MjHttpExchange {
	private final HttpServletRequest request;
	private final HttpServletResponse response;
	private boolean responseSent;

	public HttpServletHttpExchange(HttpServletRequest request, HttpServletResponse response) {
		this.request = request;
		this.response = response;
	}

	@Override
	public String getPath() {
		String contextPath = request.getServletPath();
		String requestURI = request.getRequestURI();
		String uri = requestURI.substring(contextPath.length());
		return uri;
	}

	@Override
	public InputStream getRequest() {
		try {
			return request.getInputStream();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Map<String, Collection<String>> getParameters() {
		return Collections.emptyMap();
	}

	@Override
	public void sendResponse(int statusCode, byte[] bytes, String contentType) {
        response.setContentType(contentType);
		response.setStatus(statusCode);
		try {
			OutputStream os = response.getOutputStream();
			responseSent = true;
			os.write(bytes);
			os.close();
		} catch (IOException x) {
			throw new RuntimeException(x);
		}
	}

	@Override
	public void sendResponse(int statusCode, String body, String contentType) {
        sendResponse(statusCode, body.getBytes(StandardCharsets.UTF_8), contentType + "; charset=utf-8");
	}

	@Override
	public boolean isResponseSent() {
		return responseSent;
	}
}