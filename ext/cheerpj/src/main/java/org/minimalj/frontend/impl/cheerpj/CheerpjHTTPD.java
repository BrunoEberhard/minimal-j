package org.minimalj.frontend.impl.cheerpj;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.Map;
import java.util.UUID;

import org.minimalj.application.Application;
import org.minimalj.application.Configuration;
import org.minimalj.backend.Backend;
import org.minimalj.frontend.impl.json.JsonFrontend;
import org.minimalj.security.Subject;
import org.minimalj.transaction.InputStreamTransaction;
import org.minimalj.transaction.OutputStreamTransaction;
import org.minimalj.transaction.Transaction;
import org.minimalj.util.SerializationContainer;
import org.minimalj.util.StringUtils;
import org.minimalj.util.resources.Resources;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;

public class CheerpjHTTPD extends NanoHTTPD {

	public CheerpjHTTPD(int port, boolean secure) {
		super(port);
		if (secure) {
			try {
				String keyAndTrustStoreClasspathPath = Configuration.get("MjKeystore"); // in example '/mjdevkeystore.jks'
				char[] passphrase = Configuration.get("MjKeystorePassphrase").toCharArray(); // ub example 'mjdev1'

				makeSecure(makeSSLSocketFactory(keyAndTrustStoreClasspathPath, passphrase), null);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public Response serve(IHTTPSession session) {
		if (session.getMethod() == Method.POST) {
			try {
				Field methodField = HTTPSession.class.getDeclaredField("method");
				methodField.setAccessible(true);
				methodField.set(session, Method.PUT);
			} catch (Exception x) {
				x.printStackTrace();
			}
		}
		return super.serve(session);
	}

	@Override
	public Response serve(String uriString, Method method, Map<String, String> headers, Map<String, String> parameters, Map<String, String> files) {

		URI uri = URI.create(uriString);
		System.out.println("Looking for: " + uriString);
		String path = uri.getPath();

		if (path.endsWith("/")) {
			String htmlTemplate = JsonFrontend.getHtmlTemplate();
			htmlTemplate = htmlTemplate.replace("$SEND", "sendCheerpj");
			htmlTemplate = htmlTemplate.replace("$IMPORT", "<script src=\"https://cjrtnc.leaningtech.com/1.3/loader.js\"></script>");
			htmlTemplate = htmlTemplate.replace("$INIT", getInit());
			String html = JsonFrontend.fillPlaceHolder(htmlTemplate, path);
			return newFixedLengthResponse(Status.OK, "text/html", html);
		} else if (method == Method.PUT && path.startsWith("/java-transaction/")) {
			String inputFileName = files.get("content");
			if (inputFileName == null) {
				return newFixedLengthResponse(Status.BAD_REQUEST, "text/plain", "No Input (content)");
			}
			return transaction(headers, inputFileName);
		} else if (path.equals("/application.png")) {
			return newChunkedResponse(Status.OK, "png", Application.getInstance().getIcon());
		} else {
			int index = uriString.lastIndexOf('.');
			if (index > -1 && index < uriString.length() - 1) {
				String postfix = uriString.substring(index + 1);
				String mimeType = StringUtils.equals("jar", postfix) ? "application/java" : Resources.getMimeType(postfix);
				if (mimeType != null) {
					InputStream inputStream = CheerpjHTTPD.class.getResourceAsStream(uriString);
					if (inputStream != null) {
						try {
							return newFixedLengthResponse(Status.OK, mimeType, inputStream, inputStream.available());
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		return newFixedLengthResponse(Status.NOT_FOUND, "text/html", uri + " not found");
	}

	private static String getInit() {
		String applicationClassName = Application.getInstance().getClass().getName();
		return "cheerpjInit();\n"
				+ "cheerpjRunMain(\"org.minimalj.frontend.impl.cheerpj.Cheerpj\", \"/app/m.jar:/app/h.jar\", \"" + applicationClassName + "\");\n";

		// "-DMjRepository=org.minimalj.repository.memory.InMemoryRepository",
		// "-DMjUserFile=users.txt", "-DMjDevMode=true");
	}

	private Response transaction(Map<String, String> headers, String inputFileName) {
		try (InputStream is = new FileInputStream(inputFileName)) {
			return transaction(headers, is);
		} catch (Exception e) {
			return newFixedLengthResponse(Status.BAD_REQUEST, "text/plain", e.getMessage());
		}
	}

	private Response transaction(Map<String, String> headers, InputStream is) {
		if (Backend.getInstance().isAuthenticationActive()) {
			String token = headers.get("token");
			return transaction(token, is);
		} else {
			return transaction(is);
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

			try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
					ObjectOutputStream oos = new ObjectOutputStream(byteArrayOutputStream)) {
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
