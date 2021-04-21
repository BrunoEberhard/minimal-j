package org.minimalj.rest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

import org.minimalj.application.Application;
import org.minimalj.backend.Backend;
import org.minimalj.frontend.impl.web.MjHttpExchange;
import org.minimalj.frontend.impl.web.MjHttpHandler;
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

public class RestHttpHandler implements MjHttpHandler {

	private final MjHttpHandler next;
	private final Map<String, Class<?>> classByName;

	public RestHttpHandler() {
		this(null);
	}
	
	public RestHttpHandler(MjHttpHandler next) {
		this.next = next;
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
	public void handle(MjHttpExchange exchange) {
		String[] pathElements;
		String uriString = exchange.getPath();
		String path = uriString;
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		pathElements = path.split("/");
		
		Class<?> clazz = null;
		if (pathElements.length > 0 && !StringUtils.isEmpty(pathElements[0]) && !Character.isLowerCase(pathElements[0].charAt(0))) {
			clazz = classByName.get(pathElements[0]);
			if (clazz == null) {
				exchange.sendResponse(HttpsURLConnection.HTTP_NOT_FOUND, "Class " + pathElements[0] + " not found", "text/html");
				return;
			}
		}
		
		switch (exchange.getMethod()) {
		case "GET":
			if (StringUtils.equals("swagger-ui", pathElements[0])) {
				if (pathElements.length == 1) {
					if (!exchange.getPath().endsWith("/")) {
						exchange.addHeader("Location", uriString + "/");
						exchange.sendResponse(HttpsURLConnection.HTTP_MOVED_PERM, "", "text/plain");
						return;
					}
					try {
						exchange.sendResponse(HttpsURLConnection.HTTP_OK, getClass().getResourceAsStream(uriString + "index.html").readAllBytes(), "text/html");
					} catch (NullPointerException x) {
						exchange.sendResponse(HttpsURLConnection.HTTP_INTERNAL_ERROR, "Not found: " + uriString + "index.html", "text/plain");
					} catch (IOException x) {
						x.printStackTrace();
						exchange.sendResponse(HttpsURLConnection.HTTP_INTERNAL_ERROR, x.getMessage(), "text/plain");
					}
					return;
				} else if (StringUtils.equals("swagger.json", pathElements[1])) {
					exchange.sendResponse(HttpsURLConnection.HTTP_OK, new OpenAPIFactory().create(Application.getInstance()), "text/json");
					return;
				} else {
					int pos = uriString.lastIndexOf('.');
					String mimeType = Resources.getMimeType(uriString.substring(pos + 1));
					try {
						exchange.sendResponse(HttpsURLConnection.HTTP_OK, getClass().getResourceAsStream(uriString).readAllBytes(), mimeType);
					} catch (IOException x) {
						x.printStackTrace();
						exchange.sendResponse(HttpsURLConnection.HTTP_INTERNAL_ERROR, x.getMessage(), "text/plain");
					}
					return;
				}
			}
			if (pathElements.length == 1 && clazz != null) {
				// GET entity (get all or pages of size x)
				Query query = By.all();
				String sizeParameter = exchange.getParameter("size");
				if (!StringUtils.isBlank(sizeParameter)) {
					int offset = 0;
					String offsetParameter = exchange.getParameter("offset");
					if (!StringUtils.isBlank(offsetParameter)) {
						try {
							offset = Integer.valueOf(offsetParameter);
						} catch (NumberFormatException e) {
							exchange.sendResponse(HttpsURLConnection.HTTP_NOT_FOUND, "page parameter invalid: " + offsetParameter, "text/plain");
							return;
						}
					}
					try {
						int size = Integer.valueOf(sizeParameter);
						query = query.limit(offset, size);
					} catch (NumberFormatException e) {
						exchange.sendResponse(HttpsURLConnection.HTTP_NOT_FOUND, "size parameter invalid: " + sizeParameter, "text/plain");
						return;
					}
				}
				List<?> object = Backend.find(clazz, query);
				exchange.sendResponse(HttpsURLConnection.HTTP_OK, EntityJsonWriter.write(object), "text/json");
				return;
			}
			if (pathElements.length == 2) {
				// GET entity/id (get one)
				String id = pathElements[1];
				Object object = Backend.read(clazz, id);
				if (object != null) {
					exchange.sendResponse(HttpsURLConnection.HTTP_OK, EntityJsonWriter.write(object), "text/json");
				} else {
					exchange.sendResponse(HttpsURLConnection.HTTP_NOT_FOUND, clazz.getSimpleName() + " with id " + id + " not found", "text/plain");
				}
				return;
			}
			break;
		case "POST":
			if (clazz != null) {
//				String inputString = files.get("postData");
//				if (inputString == null) {
//					exchange.sendResponse(HttpsURLConnection.HTTP_BAD_REQUEST, "No Input", "text/plain");
//					return;
//				}
				Object inputObject = EntityJsonReader.read(clazz, exchange.getRequest());
				Object id = Backend.insert(inputObject);
				exchange.sendResponse(HttpsURLConnection.HTTP_OK, id.toString(), "text/plain");
				return;
			}
			break;
		case "DELETE":
			if (clazz != null) {
				if (pathElements.length >= 2) {
					String id = pathElements[1];
					Object idOnlyObject = CloneHelper.newInstance(clazz);
					IdUtils.setId(idOnlyObject, id);
					Backend.delete(idOnlyObject);
 				} else {
					exchange.sendResponse(HttpsURLConnection.HTTP_BAD_REQUEST, "Post expects id in url", "text/plain");
					return;
 				}
			}
			break;
		case "PUT":
//			String inputFileName = files.get("content");
//			if (inputFileName == null) {
//				exchange.sendResponse(HttpsURLConnection.HTTP_BAD_REQUEST, "No Input", "text/plain");
//				return;
//			}
	
			if (pathElements.length >= 1 && StringUtils.equals("java-transaction", pathElements[0])) {
				transaction(exchange, exchange.getRequest());
				return;
			} else if (clazz != null && pathElements.length == 2) {
				String id = pathElements[1];
				Object object = Backend.read(clazz, id);
				Object inputObject = EntityJsonReader.read(object, exchange.getRequest());
				IdUtils.setId(object, id); // don't let the id be changed
				object = Backend.save(inputObject);
				exchange.sendResponse(HttpsURLConnection.HTTP_OK, EntityJsonWriter.write(object), "text/json");
				return;
			} else {
				exchange.sendResponse(HttpsURLConnection.HTTP_BAD_REQUEST, "Put excepts class/id in url", "text/plain");
				return;
			}
		case "OPTIONS":
			exchange.addHeader("Access-Control-Allow-Origin", "*");
			exchange.addHeader("Access-Control-Allow-Methods", "POST, GET, DELETE, PUT, PATCH, OPTIONS");
			exchange.addHeader("Access-Control-Allow-Headers", "API-Key,accept, Content-Type");
			exchange.addHeader("Access-Control-Max-Age", "1728000");
			exchange.sendResponse(HttpsURLConnection.HTTP_OK, (String) null, "text/json");
			return;
		default:
			exchange.sendResponse(HttpsURLConnection.HTTP_BAD_METHOD, "Method not allowed: " + exchange.getMethod(), "text/plain");
			return;
		}
		next.handle(exchange);
	}

	private void transaction(MjHttpExchange exchange, InputStream is) {
		if (Backend.getInstance().isAuthenticationActive()) {
			String token = exchange.getHeader("token");
			transaction(exchange, token, is);
		} else {
			doTransaction(exchange, is);
		}
	}
	
	private void transaction(MjHttpExchange exchange, String token, InputStream is) {
		if (!StringUtils.isEmpty(token)) {
			Subject subject = Backend.getInstance().getAuthentication().getUserByToken(UUID.fromString(token));
			if (subject != null) {
				Subject.setCurrent(subject);
			} else {
				exchange.sendResponse(HttpsURLConnection.HTTP_NOT_AUTHORITATIVE, "Invalid token", "text/plain");
				return;
			}
		}
		try {
			doTransaction(exchange, is);
		} finally {
			Subject.setCurrent(null);
		}
	}
	
	private void doTransaction(MjHttpExchange exchange, InputStream inputStream) {
		try (ObjectInputStream ois = new ObjectInputStream(inputStream)) {
			Object input = ois.readObject();
			if (!(input instanceof Transaction)) {
				exchange.sendResponse(HttpsURLConnection.HTTP_BAD_REQUEST, "Input not a Transaction but a " + input.getClass().getName(), "text/plain");
				return;
			}
			Transaction<?> transaction = (Transaction<?>) input;

			if (transaction instanceof InputStreamTransaction) {
				InputStreamTransaction<?> inputStreamTransaction = (InputStreamTransaction<?>) transaction;
				inputStreamTransaction.setStream(ois);
			}
			if (transaction instanceof OutputStreamTransaction) {
				exchange.sendResponse(HttpsURLConnection.HTTP_NOT_IMPLEMENTED, "OutputStreamTransaction not implemented", "text/plain");
				return;
			}

			Object output;
			try {
				output = Backend.execute((Transaction<?>) transaction);
			} catch (Exception e) {
				exchange.sendResponse(HttpsURLConnection.HTTP_INTERNAL_ERROR, e.getMessage(), "text/plain");
				return;
			}

			try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(byteArrayOutputStream)) {
				oos.writeObject(SerializationContainer.wrap(output));
				oos.flush();
				byte[] bytes = byteArrayOutputStream.toByteArray();
				exchange.sendResponse(HttpsURLConnection.HTTP_OK, bytes, "application/octet-stream");
			}
		} catch (Exception e) {
			exchange.sendResponse(HttpsURLConnection.HTTP_BAD_REQUEST, e.getMessage(), "text/plain");
			return;
		}
	}

}
