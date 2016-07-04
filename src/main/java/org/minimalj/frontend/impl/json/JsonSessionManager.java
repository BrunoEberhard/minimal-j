package org.minimalj.frontend.impl.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JsonSessionManager {
	private static final Logger logger = Logger.getLogger(JsonSessionManager.class.getName());
	private static final int MAX_SESSIONS = Integer.valueOf(System.getProperty("MjMaxSessions", "30"));

	private final Map<String, JsonPageManager> sessions = new HashMap<>();
	private final List<String> sessionList = new ArrayList<>();
	
	private JsonPageManager getSession(String sessionId) {
		// update last access (move it at the end of the list)
		sessionList.remove(sessionId);
		sessionList.add(sessionId);
		
		return sessions.get(sessionId);
	}
	
	private String createSession() {
		if (sessionList.size() >= MAX_SESSIONS) {
			String sessionId = sessionList.get(0);
			sessions.remove(sessionId);
			sessionList.remove(sessionId);
		}
		
		String sessionId = UUID.randomUUID().toString();
		JsonPageManager session = new JsonPageManager();
		sessions.put(sessionId, session);

		return sessionId;
	}
	
	public String handle(String json) {
		Map<String, Object> data = (Map<String, Object>) new JsonReader().read(json);

		String sessionId = (String) data.get("session");
		boolean invalidSession = sessionId != null && !sessions.containsKey(sessionId);
		JsonPageManager session;
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

		if (invalidSession) {
			// TODO better invalid session management
			output.add("error", "Invalid session. Please close and reopen tab.");
		}
		
		output.add("session", sessionId);
		
		return output.toString();
	}


}
