package org.minimalj.rest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.minimalj.application.Application;
import org.minimalj.application.Configuration;
import org.minimalj.backend.Backend;
import org.minimalj.metamodel.model.MjEntity;
import org.minimalj.metamodel.model.MjModel;
import org.minimalj.repository.query.By;
import org.minimalj.repository.query.Query;
import org.minimalj.rest.openapi.OpenAPIFactory;
import org.minimalj.security.Subject;
import org.minimalj.transaction.InputStreamTransaction;
import org.minimalj.transaction.OutputStreamTransaction;
import org.minimalj.transaction.Transaction;
import org.minimalj.util.CloneHelper;
import org.minimalj.util.IdUtils;
import org.minimalj.util.SerializationContainer;
import org.minimalj.util.StringUtils;
import org.minimalj.util.resources.Resources;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;

public class RestHTTPD extends NanoHTTPD {

	private final Map<String, Class<?>> classByName;
	
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
		classByName = initClassMap();
	}
	
	protected Map<String, Class<?>> initClassMap() {
		Map<String, Class<?>> classByName = new HashMap<>();
		MjModel model = new MjModel(Application.getInstance().getEntityClasses());
		for (MjEntity entity : model.entities) {
			classByName.put(entity.getClassName(), entity.getClazz());
		}
		
		return classByName;
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
		
		Class<?> clazz = null;
		if (pathElements.length > 0 && !StringUtils.isEmpty(pathElements[0]) && !Character.isLowerCase(pathElements[0].charAt(0))) {
			clazz = classByName.get(pathElements[0]);
			if (clazz == null) {
				return newFixedLengthResponse(Status.NOT_FOUND, "text/html", "Class not available");
			}
		}
		
		if (method == Method.GET) {
			if (StringUtils.equals("swagger-ui", pathElements[0])) {
				if (pathElements.length == 1) {
					if (!uriString.endsWith("/")) {
						Response r = newFixedLengthResponse(Response.Status.REDIRECT, MIME_HTML, "");
			            r.addHeader("Location", uriString + "/");
			            return r;
					}
					return newChunkedResponse(Status.OK, "text/html",
							getClass().getResourceAsStream(uriString + "index.html"));
				} else if (StringUtils.equals("swagger.json", pathElements[1])) {
					return newFixedLengthResponse(Status.OK, "text/json",
							new OpenAPIFactory().create(Application.getInstance()));
				} else {
					int pos = uriString.lastIndexOf('.');
					String mimeType = Resources.getMimeType(uriString.substring(pos + 1));
					return newChunkedResponse(Status.OK, mimeType, getClass().getResourceAsStream(uriString));
				}
			}
			if (pathElements.length == 1 && clazz != null) {
				// GET entity (get all or pages of size x)
				Query query = By.all();
				String sizeParameter = parameters.get("size");
				if (!StringUtils.isBlank(sizeParameter)) {
					int offset = 0;
					String offsetParameter = parameters.get("offset");
					if (!StringUtils.isBlank(offsetParameter)) {
						try {
							offset = Integer.valueOf(offsetParameter);
						} catch (NumberFormatException e) {
							return newFixedLengthResponse(Status.BAD_REQUEST, "text/json",
									"page parameter invalid: " + offsetParameter);
						}
					}
					try {
						int size = Integer.valueOf(sizeParameter);
						query = query.limit(offset, size);
					} catch (NumberFormatException e) {
						return newFixedLengthResponse(Status.BAD_REQUEST, "text/json",
								"size parameter invalid: " + sizeParameter);
					}
				}
				List<?> object = Backend.find(clazz, query);
				return newFixedLengthResponse(Status.OK, "text/json", EntityJsonWriter.write(object));
			}
			if (pathElements.length == 2) {
				// GET entity/id (get one)
				String id = pathElements[1];
				Object object = Backend.read(clazz, id);
				if (object != null) {
					return newFixedLengthResponse(Status.OK, "text/json", EntityJsonWriter.write(object));
				} else {
					return newFixedLengthResponse(Status.NOT_FOUND, "text/plain",
							clazz.getSimpleName() + " with id " + id + " not found");
				}
			}
		} else if (method == Method.POST) {
			if (clazz != null) {
				String inputString = files.get("postData");
				if (inputString == null) {
					return newFixedLengthResponse(Status.BAD_REQUEST, "text/plain", "No Input");
				}
				Object inputObject = EntityJsonReader.read(clazz, inputString);
				Object id = Backend.insert(inputObject);
				return newFixedLengthResponse(Status.OK, "text/plain", id.toString());
			}
			
		} else if (method == Method.DELETE) {
			if (clazz != null) {
				if (pathElements.length >= 2) {
					String id = pathElements[1];
					Object idOnlyObject = CloneHelper.newInstance(clazz);
					IdUtils.setId(idOnlyObject, id);
					Backend.delete(idOnlyObject);
 				} else {
 					return newFixedLengthResponse(Status.BAD_REQUEST, "text/plain", "Post expects id in url");
 				}
			}
			
		} else if (method == Method.PUT) {
			String inputFileName = files.get("content");
			if (inputFileName == null) {
				return newFixedLengthResponse(Status.BAD_REQUEST, "text/plain", "No Input");
			}
	
			if (pathElements.length >= 1 && StringUtils.equals("java-transaction", pathElements[0])) {
				return transaction(headers, inputFileName);
			} else if (clazz != null && pathElements.length == 2) {
				String id = pathElements[1];
				Object object = Backend.read(clazz, id);
				try (FileInputStream fis = new FileInputStream(new File(inputFileName))) {
					Object inputObject = EntityJsonReader.read(object, fis);
					IdUtils.setId(object, id); // don't let the id be changed
					object = Backend.save(inputObject);
					return newFixedLengthResponse(Status.OK, "text/json", EntityJsonWriter.write(object));
				} catch (IOException x) {
					return newFixedLengthResponse(Status.BAD_REQUEST, "text/plain", "Could not read input");
				}
			} else {
				return newFixedLengthResponse(Status.BAD_REQUEST, "text/plain", "Put excepts class/id in url");
			}
		} else if (method == Method.OPTIONS) {
			Response response = newFixedLengthResponse(Status.OK, "text/plain", null);
			response.addHeader("Access-Control-Allow-Origin", "*");
			response.addHeader("Access-Control-Allow-Methods", "POST, GET, DELETE, PUT, PATCH, OPTIONS");
			response.addHeader("Access-Control-Allow-Headers", "API-Key,accept, Content-Type");
			response.addHeader("Access-Control-Max-Age", "1728000");
			
			return response;
		} else {
			return newFixedLengthResponse(Status.METHOD_NOT_ALLOWED, "text/plain", "Method not allowed: " + method);
		}
		return newFixedLengthResponse(Status.BAD_REQUEST, "text/plain", "Not a valid request url");
	}

	private Response transaction(Map<String, String> headers, String inputFileName) {
		try (InputStream is = new FileInputStream(inputFileName)) {
			if (Backend.getInstance().isAuthenticationActive()) {
				String token = headers.get("token");
				return transaction(token, is);
			} else {
				return transaction(is);
			}
		} catch (Exception e) {
			return newFixedLengthResponse(Status.BAD_REQUEST, "text/plain", e.getMessage());
		}
	}
	
	private Response transaction(String token, InputStream is) {
		if (!StringUtils.isEmpty(token)) {
			Subject subject = Backend.getInstance().getAuthentication().getUserByToken(UUID.fromString(token));
			if (subject != null) {
				Subject.setCurrent(subject);
			} else {
				return newFixedLengthResponse(Status.UNAUTHORIZED, "text/plain", "Invalid token");
			}
		}
		try {
			return transaction(is);
		} finally {
			Subject.setCurrent(null);
		}
	}
	
	private Response transaction(InputStream inputStream) {
		try (ObjectInputStream ois = new ObjectInputStream(inputStream)) {
			Object input = ois.readObject();
			if (!(input instanceof Transaction)) {
				return newFixedLengthResponse(Status.BAD_REQUEST, "text/plain", "Input not a Transaction but a " + input.getClass().getName());
			}
			Transaction<?> transaction = (Transaction<?>) input;

			if (transaction instanceof InputStreamTransaction) {
				InputStreamTransaction<?> inputStreamTransaction = (InputStreamTransaction<?>) transaction;
				inputStreamTransaction.setStream(ois);
			}
			if (transaction instanceof OutputStreamTransaction) {
				return newFixedLengthResponse(Status.NOT_IMPLEMENTED, "text/plain", "OutputStreamTransaction not implemented");
			}

			Object output;
			try {
				output = Backend.execute((Transaction<?>) transaction);
			} catch (Exception e) {
				return newFixedLengthResponse(Status.INTERNAL_ERROR, "text/plain", e.getMessage());
			}

			try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(byteArrayOutputStream)) {
				oos.writeObject(SerializationContainer.wrap(output));
				oos.flush();
				byte[] bytes = byteArrayOutputStream.toByteArray();
				try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes)) {
					return newFixedLengthResponse(Status.OK, "application/octet-stream", byteArrayInputStream, bytes.length);
				}
			}
		} catch (Exception e) {
			return newFixedLengthResponse(Status.BAD_REQUEST, "text/plain", e.getMessage());
		}
	}

}
