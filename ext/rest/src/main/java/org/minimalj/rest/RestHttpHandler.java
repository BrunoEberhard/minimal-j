package org.minimalj.rest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;

import org.minimalj.application.Application;
//import org.minimalj.application.Application;
import org.minimalj.backend.Backend;
import org.minimalj.frontend.impl.web.MjHttpExchange;
import org.minimalj.frontend.impl.web.MjHttpHandler;
import org.minimalj.metamodel.model.MjEntity;
import org.minimalj.metamodel.model.MjModel;
import org.minimalj.model.Api;
import org.minimalj.model.Model;
import org.minimalj.repository.query.By;
import org.minimalj.repository.query.Query;
import org.minimalj.rest.openapi.OpenAPIFactory;
import org.minimalj.security.Subject;
import org.minimalj.transaction.InputStreamTransaction;
import org.minimalj.transaction.OutputStreamTransaction;
import org.minimalj.transaction.Transaction;
import org.minimalj.util.CloneHelper;
import org.minimalj.util.IdUtils;
import org.minimalj.util.LocaleContext;
import org.minimalj.util.LocaleContext.AcceptedLanguageLocaleSupplier;
import org.minimalj.util.SerializationContainer;
import org.minimalj.util.StringUtils;
import org.minimalj.util.resources.Resources;

public class RestHttpHandler implements MjHttpHandler {
	private static final Logger LOG = Logger.getLogger(RestHttpHandler.class.getName());

	private final MjHttpHandler next;
	private final String path;
	private final int pathLength;
	private final Model model;
	private final Api api;

	private final Map<String, Class<?>> classByName = new LinkedHashMap<>();

	public RestHttpHandler() {
		this(null);
	}

	public RestHttpHandler(MjHttpHandler next) {
		this(null, Application.getInstance(), next);
	}

	public RestHttpHandler(Model model, MjHttpHandler next) {
		this(null, model, next);
	}
	
	public RestHttpHandler(String path, Model model, MjHttpHandler next) {
		this.path = preparePath(path);
		this.pathLength = this.path.length();
		this.model = model;
		this.next = next;
		
		MjModel mjModel = new MjModel(model.getEntityClasses());
		for (MjEntity entity : mjModel.entities) {
			if ((entity.getClazz().getModifiers() & Modifier.ABSTRACT) == 0) {
				classByName.put(entity.getClassName(), entity.getClazz());
			}
		}
		
		if (model instanceof Api) {
			api = (Api) model;
			Class<?>[] transactionClasses = api.getTransactionClasses();
			for (Class<?> transactionClass : transactionClasses) {
				if (!Transaction.class.isAssignableFrom(transactionClass)) {
					throw new IllegalArgumentException(transactionClass.getSimpleName() + " must implement " + Transaction.class.getSimpleName());
				}
				classByName.put(transactionClass.getSimpleName(), transactionClass);
			}
		} else {
			api = null;
		}
	}
	
	private static String preparePath(String path) {
		if (path == null) {
			return "/";
		}
		return path.endsWith("/") ? path : path + "/";
	}

	@Override
	public void handle(MjHttpExchange exchange) {
		String path = exchange.getPath();
		if (path.startsWith(this.path)) {
			path = path.substring(pathLength);
			try {
				LocaleContext.setLocale(new AcceptedLanguageLocaleSupplier(exchange.getHeader(AcceptedLanguageLocaleSupplier.ACCEPTED_LANGUAGE_HEADER)));
				handle(exchange, exchange.getPath(), path);
			} finally {
				LocaleContext.resetLocale();
			}
		} else {
			next.handle(exchange);
		}
	}
	
	private void handle(MjHttpExchange exchange, String uriString, String path) {
		var pathElements = path.split("/");
		
		Class<?> clazz = null;
		if (pathElements.length > 0 && !StringUtils.isEmpty(pathElements[0]) && !Character.isLowerCase(pathElements[0].charAt(0))) {
			clazz = classByName.get(pathElements[0]);
			if (clazz == null) {
				exchange.sendResponse(HttpsURLConnection.HTTP_NOT_FOUND, "Class " + pathElements[0] + " not found", "text/html");
				return;
			} else if (Transaction.class.isAssignableFrom(clazz)) {
				if (!exchange.getMethod().equals("POST")) {
					exchange.sendResponse(HttpsURLConnection.HTTP_BAD_METHOD, pathElements[0] + " only supports POST", "text/html");
					return;
				} else if (pathElements.length > 1) {
					exchange.sendResponse(HttpsURLConnection.HTTP_BAD_REQUEST, pathElements[0] + " only must not have URL parameters. Request must be sent in request body.", "text/html");
					return;
				}
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
						exchange.sendResponse(HttpsURLConnection.HTTP_OK,
								getClass().getResourceAsStream(uriString.substring(pathLength - 1) + "index.html").readAllBytes(), "text/html");
					} catch (NullPointerException x) {
						exchange.sendResponse(HttpsURLConnection.HTTP_INTERNAL_ERROR, "Not found: " + uriString + "index.html", "text/plain");
					} catch (IOException x) {
						LOG.log(Level.WARNING, x.getMessage(), x);
						exchange.sendResponse(HttpsURLConnection.HTTP_INTERNAL_ERROR, x.getMessage(), "text/plain");
					}
					return;
				} else if (StringUtils.equals("swagger.json", pathElements[1])) {
					exchange.sendResponse(HttpsURLConnection.HTTP_OK, new OpenAPIFactory().create(model), "text/json");
					return;
				} else {
					int pos = uriString.lastIndexOf('.');
					String mimeType = Resources.getMimeType(uriString.substring(pos + 1));
					try {
						exchange.sendResponse(HttpsURLConnection.HTTP_OK, getClass().getResourceAsStream(uriString.substring(pathLength - 1)).readAllBytes(), mimeType);
					} catch (IOException x) {
						LOG.log(Level.WARNING, x.getMessage(), x);
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
				if (!Transaction.class.isAssignableFrom(clazz)) {
					if (api != null && !api.canCreate(clazz)) {
						exchange.sendResponse(HttpsURLConnection.HTTP_BAD_METHOD, "insert of" + clazz.getSimpleName() + " not possible", "text/plain");
						return;
					}
					Object inputObject = EntityJsonReader.read(clazz, exchange.getRequest());
					Object id = Backend.insert(inputObject);
					exchange.sendResponse(HttpsURLConnection.HTTP_OK, id.toString(), "text/plain");
					return;
				} else {
					try {
						Class<?> inputClass = clazz.getConstructors()[0].getParameters()[0].getType();
						Object inputObject = EntityJsonReader.read(inputClass, exchange.getRequest());
						Transaction<?> transaction = (Transaction<?>) clazz.getConstructors()[0].newInstance(inputObject);
						Object outputObject = transaction.execute();
						exchange.sendResponse(HttpsURLConnection.HTTP_OK, EntityJsonWriter.write(outputObject), "text/json");
					} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
						throw new RuntimeException(e);
					}
				}
			}
			break;
		case "DELETE":
			if (clazz != null) {
				if (api != null && !api.canDelete(clazz)) {
					exchange.sendResponse(HttpsURLConnection.HTTP_BAD_METHOD, "delete of" + clazz.getSimpleName() + " not possible", "text/plain");
					return;
				}
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
				if (api != null && !api.canUpdate(clazz)) {
					exchange.sendResponse(HttpsURLConnection.HTTP_BAD_METHOD, "update of" + clazz.getSimpleName() + " not possible", "text/plain");
					return;
				}
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
