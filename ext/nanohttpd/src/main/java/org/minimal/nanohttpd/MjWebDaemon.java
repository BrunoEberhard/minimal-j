package org.minimal.nanohttpd;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.minimalj.application.Configuration;
import org.minimalj.frontend.impl.web.MjHttpExchange;
import org.minimalj.frontend.impl.web.WebApplication;
import org.minimalj.frontend.impl.web.WebServer;
import org.minimalj.util.LocaleContext;

import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.NanoWSD;

public class MjWebDaemon extends NanoWSD {

	public MjWebDaemon(int port, boolean secure) {
		super(port);
		if (secure) {
			try {
				// note 1: to first read the property MjKeystorePassphrase and then convert it
				// to char[] makes the whole char[] story senseless. But how to do it else?
				// Maybe specify a filename and then read it byte by byte.

				// note 2: nanohttpd implies that keypass and storepass are the same passwords.
				// I don't know if this is a good idea.

				// note 3: example to generate the store (todo: move to documentation)
				// keytool.exe -keystore mjdevkeystore.jks -keyalg RSA -keysize 3072 -genkeypair
				// -dname "cn=localhost, ou=MJ, o=Minimal-J, c=CH" -storepass mjdev1 -keypass
				// mjdev1
				// keytool.exe -keystore mjdevkeystore.jks -storepass mjdev1 -keypass mjdev1
				// -export -file mj.cer

				String keyAndTrustStoreClasspathPath = Configuration.get("MjKeystore"); // in example '/mjdevkeystore.jks'
				char[] passphrase = Configuration.get("MjKeystorePassphrase").toCharArray(); // ub example 'mjdev1'

				makeSecure(makeSSLSocketFactory(keyAndTrustStoreClasspathPath, passphrase), null);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static class NanoHttpExchange extends MjHttpExchange {
		private final IHTTPSession session;
		private Response response;

		public NanoHttpExchange(IHTTPSession session) {
			this.session = session;
		}

		public Response getResponse() {
			return response;
		}

		@Override
		public void sendResponse(int statusCode, String body, String contentType) {
			response = newFixedLengthResponse(Status.lookup(statusCode), contentType, body);
		}

		@Override
		public void sendResponse(int statusCode, byte[] bytes, String contentType) {
			response = newChunkedResponse(Status.lookup(statusCode), contentType, new ByteArrayInputStream(bytes));
		}

		@Override
		public String getPath() {
			return session.getUri();
		}

		@Override
		public InputStream getRequest() {
			return session.getInputStream();
		}

		@Override
		public Map<String, List<String>> getParameters() {
			if (session.getMethod() == Method.GET) {
				return session.getParameters();
			} else {
				String requestBody = WebServer.convertStreamToString(getRequest());
				return decodeParameters(requestBody);
			}
		}
	}

	@Override
	public Response serve(IHTTPSession session) {
		if (WebServer.useWebSocket) {
			return super.serve(session);
		} else {
			return serveHttp(session);
		}
	}

	@Override
	protected Response serveHttp(final IHTTPSession session) {
		NanoHttpExchange exchange = new NanoHttpExchange(session);
		try {
			LocaleContext.setCurrent(MjHttpExchange.getLocale(session.getHeaders().get("accept-language")));
			WebApplication.handle(exchange);
			return exchange.getResponse();
		} finally {
			LocaleContext.setCurrent(null);
		}
	}

	@Override
	protected WebSocket openWebSocket(IHTTPSession handshake) {
		return new MjWebSocket(handshake);
	}

}
