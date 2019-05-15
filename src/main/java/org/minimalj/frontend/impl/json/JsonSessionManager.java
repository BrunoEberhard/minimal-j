package org.minimalj.frontend.impl.json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.minimalj.application.Configuration;

public class JsonSessionManager {
	private static final int MAX_SESSIONS = Integer.valueOf(Configuration.get("MjMaxSessions", "30"));

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
		Map<String, Object> data = (Map<String, Object>) JsonReader.read(json);

		boolean validSession = true;
		String sessionId = (String) data.get("session");
		if (!sessions.containsKey(sessionId)) {
			sessionId = createSession();
			if (!data.containsKey(JsonInput.INITIALIZE)) {
				data = Collections.singletonMap(JsonInput.INITIALIZE, "");
			}
		}
		JsonPageManager session = getSession(sessionId);
		
		JsonInput input = new JsonInput(data);
		JsonOutput output = session.handle(input);

		if (!validSession) {
			output.add("error", "Invalid session. Please close and reopen tab.");
		}
		
		output.add("session", sessionId);
		
		return output.toString();
	}


}
