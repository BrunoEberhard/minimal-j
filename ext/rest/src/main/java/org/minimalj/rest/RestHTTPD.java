package org.minimalj.rest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
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
import org.minimalj.repository.query.Query.QueryLimitable;
import org.minimalj.rest.openapi.OpenAPIFactory;
import org.minimalj.security.Subject;
import org.minimalj.transaction.InputStreamTransaction;
import org.minimalj.transaction.OutputStreamTransaction;
import org.minimalj.transaction.Transaction;
import org.minimalj.util.SerializationContainer;
import org.minimalj.util.StringUtils;
import org.minimalj.util.resources.Resources;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;

public class RestHTTPD extends NanoHTTPD {

	private final Map<String, Class> classByName;
	
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
	
	protected Map<String, Class> initClassMap() {
		Map<String, Class> classByName = new HashMap<>();
		MjModel model = new MjModel(Application.getInstance().getEntityClasses());
		for (MjEntity entity : model.entities) {
			classByName.put(entity.getClazz().getSimpleName(), entity.getClazz());
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
		if (pathElements.length > 0 && !Character.isLowerCase(pathElements[0].charAt(0))) {
			clazz = classByName.get(pathElements[0]);
			if (clazz == null) {
				return newFixedLengthResponse(Status.NOT_FOUND, "text/html", "Class not available");
			}
		}
		
		if (method == Method.GET) {
			if (pathElements.length == 0) {
				return newFixedLengthResponse(Status.BAD_REQUEST, "text/html", "Please specify class");
			}
			if (StringUtils.equals("swagger-ui", pathElements[0])) {
				if (pathElements.length == 1) {
					return newChunkedResponse(Status.OK, "text/html",
							getClass().getResourceAsStream(uriString + "/index.html"));
				} else if (StringUtils.equals("swagger.json", pathElements[1])) {
					return newFixedLengthResponse(Status.OK, "text/json",
							new OpenAPIFactory().create(Application.getInstance()));
				} else {
					int pos = uriString.lastIndexOf('.');
					String mimeType = Resources.getMimeType(uriString.substring(pos + 1));
					return newChunkedResponse(Status.OK, mimeType, getClass().getResourceAsStream(uriString));
				}
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
							return newFixedLengthResponse(Status.BAD_REQUEST, "text/json",
									"page parameter invalid: " + pageParameter);
						}
					}
					try {
						int size = Integer.valueOf(sizeParameter);
						query = ((QueryLimitable) query).limit(page != 0 ? page * size : null, size);
					} catch (NumberFormatException e) {
						return newFixedLengthResponse(Status.BAD_REQUEST, "text/json",
								"size parameter invalid: " + sizeParameter);
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
			if (clazz != null) {
				if (pathElements.length >= 2) {
					String id = pathElements[1];
					String inputString = files.get("content");
					if (inputString == null) {
						return newFixedLengthResponse(Status.BAD_REQUEST, "text/plain", "No Input");
					}
					Object inputObject = new EntityJsonReader().read(inputString);
					Backend.update(inputObject);
 				} else {
 					return newFixedLengthResponse(Status.BAD_REQUEST, "text/plain", "Post excepts id in url");
 				}
			}

		} else if (method == Method.PUT) {
			if (pathElements.length > 0) {
				if (StringUtils.equals("java-transaction", pathElements[0])) {

					if (Backend.getInstance().isAuthenticationActive()) {
						String token = headers.get("token");
						if (!StringUtils.isEmpty(token)) {
							Subject subject = Backend.getInstance().getAuthentication().getUserByToken(UUID.fromString(token));
							Subject.setCurrent(subject);
						}
					}

					String inputFileName = files.get("content");
					if (inputFileName == null) {
						return newFixedLengthResponse(Status.BAD_REQUEST, "text/plain", "No Input");
					}
					try (FileInputStream bis = new FileInputStream(inputFileName); ObjectInputStream ois = new ObjectInputStream(bis)) {
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
			
			if (clazz != null) {
				String inputFileName = files.get("content");
				if (inputFileName == null) {
					return newFixedLengthResponse(Status.BAD_REQUEST, "text/plain", "No Input");
				}
				String input = "";
				try {
					List<String> inputLines = Files.readAllLines(new File(inputFileName).toPath());
					for (String line : inputLines) {
						input = input + line;
					}
					Object inputObject = new EntityJsonReader().read(clazz, input);
					// IdUtils.setId(inputObject, null);
					Backend.insert(inputObject);
				} catch (IOException x) {
					return newFixedLengthResponse(Status.BAD_REQUEST, "text/plain", "Could not read input");
				}
			}
		} else if (method == Method.OPTIONS) {
			
//			TODO thank on StackOverflow
//	        header('Access-Control-Allow-Origin: *');
//	        header('Access-Control-Allow-Methods: POST, GET, DELETE, PUT, PATCH, OPTIONS');
//	        header('Access-Control-Allow-Headers: API-Key,accept, Content-Type');
//	        header('Access-Control-Max-Age: 1728000');
	        
	        
			Response response = newFixedLengthResponse(Status.OK, "text/plain", null);
			response.addHeader("Access-Control-Allow-Origin", "*");
			response.addHeader("Access-Control-Allow-Methods", "POST, GET, DELETE, PUT, PATCH, OPTIONS");
			response.addHeader("Access-Control-Allow-Headers", "API-Key,accept, Content-Type");
			response.addHeader("Access-Control-Max-Age", "1728000");
			
			return response;
		} else {
			return null;
		}
		return newFixedLengthResponse(Status.BAD_REQUEST, "text/plain", "Not a valid request url");
	}

}
