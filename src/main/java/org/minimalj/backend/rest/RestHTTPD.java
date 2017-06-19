package org.minimalj.backend.rest;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.minimalj.application.Application;
import org.minimalj.application.Configuration;
import org.minimalj.backend.Backend;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;

public class RestHTTPD extends NanoHTTPD {

	private Map<String, Class> classByName;
	
	public RestHTTPD( int port, boolean secure) {
		super(port);
		if (secure) {
			try {
				String keyAndTrustStoreClasspathPath = Configuration.get("MjKeystore"); // in example '/mjdevkeystore.jks'
				char[] passphrase = Configuration.get("MjKeystorePassphrase").toCharArray(); //  ub example 'mjdev1'
				
				makeSecure(makeSSLSocketFactory(keyAndTrustStoreClasspathPath, passphrase), null);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	protected Map<String, Class> getClassByName() {
		if (classByName == null) {
			classByName = new HashMap<>();
			for (Class entitiyClass : Application.getInstance().getEntityClasses()) {
				String name = entitiyClass.getSimpleName();
				if (classByName.containsKey(name)) {
					throw new IllegalArgumentException("Application contains two entity classes with same SimpleName " + name);
				}
				classByName.put(name, entitiyClass);
			}
		}
		return classByName;
	}
	
	
	protected Class<?> getClass(String simpleName) {
		return getClassByName().get(simpleName);
	}
	
	@Override
    public Response serve(String uriString, Method method, Map<String, String> headers, Map<String, String> parms,
            Map<String, String> files) {
		if (method == Method.GET) {
			try {
				URI uri = new URI(uriString);
				String path = uri.getPath();
				if (path.startsWith("/")) {
					path = path.substring(1);
				}
				String[] pathElements = path.split("/");
				if (pathElements.length == 0) {
					return newFixedLengthResponse(Status.BAD_REQUEST, "text/html", "Please specify class");
				}
				Class<?> clazz = getClass(pathElements[0]);
				if (clazz == null) {
					return newFixedLengthResponse(Status.NOT_FOUND, "text/html", "Class not available");
				}
				if (pathElements.length == 2) {
					String id = pathElements[1];
					Object object = Backend.read(clazz, id);
					return newFixedLengthResponse(Status.OK, "text/json", new EntityJsonWriter().write(object));
				}
				uri.getQuery();
				return newFixedLengthResponse(Status.BAD_REQUEST, "text/json", "Hallo");
			} catch (URISyntaxException e) {
				return newFixedLengthResponse(Status.BAD_REQUEST, "text/html", e.getMessage());
			}
		} else {
			return null;
		}
	}

}
