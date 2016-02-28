package org.minimalj.frontend.impl.nanoserver;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Locale.LanguageRange;
import java.util.Map;
import java.util.logging.Logger;

import org.minimalj.frontend.impl.json.JsonFrontend;
import org.minimalj.frontend.impl.json.JsonHandler;
import org.minimalj.util.resources.Resources;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;

public class MjWebDaemon extends NanoHTTPD {
	private static final Logger logger = Logger.getLogger(MjWebDaemon.class.getName());

	private JsonHandler handler = new JsonHandler();
	
	public MjWebDaemon(int port, boolean secure) {
		super(port);
		if (secure) {
			try {
				// nanohttpd geht davon aus, dass keypass = storepass... ob das so sein darf / kann?
				// bei storepass dürfte nanohttpd auch null mitgeben um die Überprüfung auszulassen...
				
				// note 1: to first read the property MjKeystorePassphrase and then convert it to char[]
				// makes the whole char[] story senseless. But how to do it else? Maybe specify a filename
				// and then read it byte by byte.
				
				// note 2: nanohttpd implies that keypass and storepass are the same passwords. I don't
				// know if this is a good idea.
				
				// note 3: example to generate the store (todo: move to documentation)
				// keytool.exe -keystore mjdevkeystore.jks -keyalg RSA -keysize 3072 -genkeypair -dname "cn=localhost, ou=MJ, o=Minimal-J, c=CH" -storepass mjdev1 -keypass mjdev1
				// keytool.exe -keystore mjdevkeystore.jks -storepass mjdev1 -keypass mjdev1 -export -file mj.cer

				String keyAndTrustStoreClasspathPath = System.getProperty("MjKeystore"); // in example '/mjdevkeystore.jks'
				char[] passphrase = System.getProperty("MjKeystorePassphrase").toCharArray(); //  ub example 'mjdev1'
				
				makeSecure(makeSSLSocketFactory(keyAndTrustStoreClasspathPath, passphrase), null);
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
		} else if ("/ajax_request.xml".equals(uri)) {
			String data = files.get("postData");
			String result = handler.handle(data);
			return newFixedLengthResponse(Status.OK, "text/xml", result);
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

}
