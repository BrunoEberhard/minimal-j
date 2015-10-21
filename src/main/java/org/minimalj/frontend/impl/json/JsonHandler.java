package org.minimalj.frontend.impl.json;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.minimalj.util.LocaleContext;

public class JsonHandler {
	private static final Logger logger = Logger.getLogger(JsonHandler.class.getName());
	
	private final Map<String, JsonClientSession> sessions = new HashMap<>();
	
	private JsonClientSession getSession(String sessionId) {
		return sessions.get(sessionId);
	}
	
	private String createSession() {
		String sessionId = UUID.randomUUID().toString();
		JsonClientSession session = new JsonClientSession();
		sessions.put(sessionId, session);
		return sessionId;
	}
	
	@Deprecated // should be done outside -> setLocale should not be done in this class
	public String handle(String json) {
		Map<String, Object> data = (Map<String, Object>) new JsonReader().read(json);
		return handle(data);
	}

	public String handle(Map<String, Object> data) {
		// TODO move this
		String locale = (String) data.get("locale");
		if (locale != null) {
			LocaleContext.setLocale(Locale.forLanguageTag(locale));
		}
		
		String sessionId = (String) data.get("session");
		boolean invalidSession = sessionId != null && getSession(sessionId) == null;
		JsonClientSession session;
		if (sessionId != null && !invalidSession) {
			session = getSession(sessionId);
		} else {
			sessionId = createSession();
			session = getSession(sessionId);
		}
		
		JsonOutput output;
		if (invalidSession) {
			data = new HashMap<>();
			data.put(JsonInput.SHOW_DEFAULT_PAGE, "");
		}
		
		try {
			JsonInput input = new JsonInput(data);
			output = session.handle(input);
		} catch (Exception x) {
			output = new JsonOutput();
			output.add("error", x.getClass().getSimpleName() + ":\n" + x.getMessage());
			logger.log(Level.SEVERE, "Internal Error", x);
			// why does logger not work here?
			x.printStackTrace();
		}

//		if (invalidSession) {
//			output.add("error", "Invalid session");
//		}
		
		output.add("session", sessionId);
		
		return output.toString();
	}


}
