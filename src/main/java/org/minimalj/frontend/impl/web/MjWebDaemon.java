package org.minimalj.frontend.impl.web;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Locale.LanguageRange;
import java.util.Map;

import org.minimalj.application.Application;
import org.minimalj.application.Configuration;
import org.minimalj.frontend.impl.json.JsonFrontend;
import org.minimalj.frontend.impl.json.JsonSessionManager;
import org.minimalj.frontend.page.Page;
import org.minimalj.util.resources.Resources;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;

public class MjWebDaemon extends NanoHTTPD {
	private JsonSessionManager sessionManager = new JsonSessionManager();
	
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
	
	@Override
    public Response serve(String uri, Method method, Map<String, String> headers, Map<String, String> parms,
            Map<String, String> files) {
		return serve(sessionManager, uri, method, headers, parms, files);
	}

    static Response serve(JsonSessionManager sessionManager, String uriString, Method method, Map<String, String> headers, Map<String, String> parms,
            Map<String, String> files) {
    	URI uri = URI.create(uriString);
		String path = uri.getPath();
		if (path.equals("/ajax_request.xml")) {
			String data = files.get("postData");
			String result = sessionManager.handle(data);
			return newFixedLengthResponse(Status.OK, "text/xml", result);
		} else if (path.equals("/application.png")) {			
			return newChunkedResponse(Status.OK, "png", Application.getInstance().getIcon());
		} else if (path.contains(".")) {
			if (path.contains("..")) {
				return newFixedLengthResponse(Status.BAD_REQUEST, "text/html", uri + " bad request");
			}

			int index = uriString.lastIndexOf('.');
			if (index > -1 && index < uriString.length() - 1) {
				String postfix = uriString.substring(index + 1);
				String mimeType = Resources.getMimeType(postfix);
				if (mimeType != null) {
					InputStream inputStream = MjWebDaemon.class.getResourceAsStream(uriString);
					if (inputStream != null) {
						return newChunkedResponse(Status.OK, mimeType, inputStream);
					}
				}
			}

			return newFixedLengthResponse(Status.NOT_FOUND, "text/html", uri + " not found");
		} else {
			if (path.length() > 1 && !Page.validateRoute(path.substring(1))) {
				return newFixedLengthResponse(Status.BAD_REQUEST, "text/html", uri + " bad request");
			}
			String htmlTemplate = JsonFrontend.getHtmlTemplate();
			Locale locale = getLocale(headers.get("accept-language"));
			htmlTemplate = htmlTemplate.replace("$SEND", WebServer.useWebSocket ? "sendWebSocket" : "sendAjax");
			String html = JsonFrontend.fillPlaceHolder(htmlTemplate, locale, path);
			return newFixedLengthResponse(Status.OK, "text/html", html);
		}

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
