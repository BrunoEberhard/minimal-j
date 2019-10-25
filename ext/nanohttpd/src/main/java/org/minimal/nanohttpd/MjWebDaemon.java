package org.minimal.nanohttpd;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.minimalj.application.Configuration;
import org.minimalj.frontend.impl.web.MjHttpExchange;
import org.minimalj.frontend.impl.web.MjHttpHandler;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;

public class MjWebDaemon extends NanoHTTPD {
	private MjHttpHandler handler = new MjHttpHandler();
	
	public MjWebDaemon(int port, boolean secure) {
		super(port);
		if (secure) {
			try {
				// note 1: to first read the property MjKeystorePassphrase and then convert it to char[]
				// makes the whole char[] story senseless. But how to do it else? Maybe specify a filename
				// and then read it byte by byte.
				
				// note 2: nanohttpd implies that keypass and storepass are the same passwords. I don't
				// know if this is a good idea.
				
				// note 3: example to generate the store (todo: move to documentation)
				// keytool.exe -keystore mjdevkeystore.jks -keyalg RSA -keysize 3072 -genkeypair -dname "cn=localhost, ou=MJ, o=Minimal-J, c=CH" -storepass mjdev1 -keypass mjdev1
				// keytool.exe -keystore mjdevkeystore.jks -storepass mjdev1 -keypass mjdev1 -export -file mj.cer

				String keyAndTrustStoreClasspathPath = Configuration.get("MjKeystore"); // in example '/mjdevkeystore.jks'
				char[] passphrase = Configuration.get("MjKeystorePassphrase").toCharArray(); //  ub example 'mjdev1'
				
				makeSecure(makeSSLSocketFactory(keyAndTrustStoreClasspathPath, passphrase), null);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	static class NanoHttpExchange implements MjHttpExchange {
		private final IHTTPSession session;
		private Response response;

		public NanoHttpExchange(IHTTPSession session) {
			this.session = session;
		}

		public Response getResponse() {
			return response;
		}

		@Override
		public void sendResponse(String body) throws IOException {
			response = newFixedLengthResponse(body);
		}

		@Override
		public void sendResponse(byte[] bytes) throws IOException {
			response = newChunkedResponse(Status.OK, "png", new ByteArrayInputStream(bytes));
		}

		@Override
		public void sendError() {
			response = newFixedLengthResponse(Status.INTERNAL_ERROR, "text/html", session.getUri() + " not found");
		}

		@Override
		public void sendNotfound() {
			response = newFixedLengthResponse(Status.NOT_FOUND, "text/html", session.getUri() + " not found");
		}

		@Override
		public void sendForbidden() {
			response = newFixedLengthResponse(Status.FORBIDDEN, "text/html", session.getUri() + " not found");
		}

		@Override
		public String getPath() {
			return session.getUri();
		}

		@Override
		public InputStream getRequest() throws IOException {
			return session.getInputStream();
		}

		@Override
		public Locale getLocale() {
			return MjHttpExchange.getLocale(session.getHeaders().get("accept-language"));

		}

	}

	@Override
	public Response serve(IHTTPSession session) {
		NanoHttpExchange exchange = new NanoHttpExchange(session);
		handler.handle(exchange);
		return exchange.getResponse();
	}

}
