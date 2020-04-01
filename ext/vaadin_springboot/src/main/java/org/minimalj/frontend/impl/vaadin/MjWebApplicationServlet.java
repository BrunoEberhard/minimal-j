package org.minimalj.frontend.impl.vaadin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.minimalj.frontend.impl.web.MjHttpExchange;
import org.minimalj.frontend.impl.web.WebApplication;

public class MjWebApplicationServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpServletHttpExchange exchange = new HttpServletHttpExchange(request, response);
		WebApplication.handle(exchange);
	}

	public static class HttpServletHttpExchange extends MjHttpExchange {
		private final HttpServletRequest request;
		private final HttpServletResponse response;
		private boolean responseSent;

		public HttpServletHttpExchange(HttpServletRequest request, HttpServletResponse response) {
			this.request = request;
			this.response = response;
		}

		@Override
		public String getPath() {
			return request.getPathInfo();
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
		public Map<String, List<String>> getParameters() {
			// TODO Parameter conversion in HttpServletHttpExchange
			return Collections.emptyMap();
		}

		@Override
		public void sendResponse(int statusCode, byte[] bytes, String contentType) {
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
			sendResponse(statusCode, body.getBytes(Charset.forName("utf-8")), contentType + "; charset=utf-8");
		}

		@Override
		public boolean isResponseSent() {
			return responseSent;
		}
	}

}