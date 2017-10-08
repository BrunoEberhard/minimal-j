package org.minimalj.rest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.minimalj.application.Application;
import org.minimalj.application.Configuration;
import org.minimalj.backend.Backend;
import org.minimalj.repository.query.By;
import org.minimalj.repository.query.Query;
import org.minimalj.repository.query.Query.QueryLimitable;
import org.minimalj.rest.openapi.OpenAPIFactory;
import org.minimalj.transaction.Transaction;
import org.minimalj.util.StringUtils;
import org.minimalj.util.resources.Resources;

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
    public Response serve(String uriString, Method method, Map<String, String> headers, Map<String, String> parameters,
            Map<String, String> files) {
		
		String[] pathElements;
		try {
			URI uri = new URI(uriString);
			String path = uri.getPath();
			if (path.startsWith("/")) {
				path = path.substring(1);
			}
			pathElements = path.split("/");
		} catch (URISyntaxException e) {
			return newFixedLengthResponse(Status.BAD_REQUEST, "text/html", e.getMessage());
		}

		if (method == Method.GET) {
				if (pathElements.length == 0) {
					return newFixedLengthResponse(Status.BAD_REQUEST, "text/html", "Please specify class");
				}
				if (StringUtils.equals("swagger-ui", pathElements[0])) {
					if (pathElements.length == 1) {
						return newChunkedResponse(Status.OK, "text/html", getClass().getResourceAsStream(uriString + "/index.html"));
					} else if (StringUtils.equals("swagger.json", pathElements[1])) {
						return newFixedLengthResponse(Status.OK, "text/json", OpenAPIFactory.create(Application.getInstance()));
					} else {
						int pos = uriString.lastIndexOf('.');
						String mimeType = Resources.getMimeType(uriString.substring(pos + 1));
						return newChunkedResponse(Status.OK, mimeType, getClass().getResourceAsStream(uriString));
					}
				}
				Class<?> clazz = getClass(pathElements[0]);
				if (clazz == null) {
					return newFixedLengthResponse(Status.NOT_FOUND, "text/html", "Class not available");
				}
				if (pathElements.length == 1) {
					// GET entity (get all or pages of size x)
					Query query = By.all();
					String sizeParameter = parameters.get("size");
					if (!StringUtils.isBlank(sizeParameter)) {
						int page = 0;
						String pageParameter = parameters.get("page");
						if (!StringUtils.isBlank(pageParameter)) {
							try {
								page = Integer.valueOf(pageParameter);
							} catch (NumberFormatException e) {
								return newFixedLengthResponse(Status.BAD_REQUEST, "text/json", "page parameter invalid: " + pageParameter);
							}
						}
						try {
							int size = Integer.valueOf(sizeParameter);
							query = ((QueryLimitable) query).limit(page != 0 ? page * size : null, size);
						} catch (NumberFormatException e) {
							return newFixedLengthResponse(Status.BAD_REQUEST, "text/json", "size parameter invalid: " + sizeParameter);
						}
					}
					List<?> object = Backend.find(clazz, query);
					return newFixedLengthResponse(Status.OK, "text/json", new EntityJsonWriter().write(object));
				}
				if (pathElements.length == 2) {
					// GET entity/id (get one)
					String id = pathElements[1];
					Object object = Backend.read(clazz, id);
					return newFixedLengthResponse(Status.OK, "text/json", new EntityJsonWriter().write(object));
				}
		} else if (method == Method.POST) {
			if (StringUtils.equals("java-transaction", pathElements[0])) {
				if (pathElements.length == 1 && pathElements[0].equals("java-transaction")) {
					Object input;
					String inputString = files.get("PostData");
					if (inputString == null) {
						return newFixedLengthResponse(Status.BAD_REQUEST, "text/plain", "No Input");
					}
					try {
						byte[] inputByteArray = Base64.getDecoder().decode(inputString);
						try (ByteArrayInputStream bis = new ByteArrayInputStream(inputByteArray)) {
							try (ObjectInputStream ois = new ObjectInputStream(bis)) {
								input = ois.readObject();
							} catch (Exception x) {
								return newFixedLengthResponse(Status.BAD_REQUEST, "text/plain", "Failed to read input: " + x.getMessage());
							}
						} catch (IOException e) {
							return newFixedLengthResponse(Status.INTERNAL_ERROR, "text/plain", "IOException " + e.getMessage());
						}
					} catch (IllegalArgumentException x) {
						return newFixedLengthResponse(Status.BAD_REQUEST, "text/plain", "Input not in valid Base64 scheme");
					}
					if (input == null) {
						return newFixedLengthResponse(Status.BAD_REQUEST, "text/plain", "No input");
					}
					if (!(input instanceof Transaction)) {
						return newFixedLengthResponse(Status.BAD_REQUEST, "text/plain", "Input not a Transaction but a " + input.getClass().getName());
					}
					
					Object output = ((Transaction<?>) input).execute();
					
					// TODO
//					ByteArrayOutputStream bos = new ByteArrayOutputStream(outputByteArray);
//					
//					byte[] outputByteArray = new ObjectOutputStream(new ByteArrayOutputStream());
//
//					String outputString = Base64.getEncoder().encodeToString(outputByteArray);
//					return newFixedLengthResponse(Status.OK, "application/base64", outputString);
					
				}
			}
		} else {
			return null;
		}
		return newFixedLengthResponse(Status.BAD_REQUEST, "text/plain", "Not a valid request url");
	}

}
