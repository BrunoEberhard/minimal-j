package org.minimalj.frontend.impl.json;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import org.minimalj.application.Configuration;

public class JsonSessionManager extends TimerTask {
	private static final Logger logger = Logger.getLogger(JsonSessionManager.class.getName());

	private static final int MAX_SESSIONS = Integer.valueOf(Configuration.get("MjMaxSessions", "200"));

	private final Map<String, JsonPageManager> sessions = new HashMap<>();

	public JsonSessionManager() {
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(this, 1 * 60 * 1000, 1 * 60 * 1000);
	}

	@Override
	public void run() {
		logger.fine("Check unused sessions");
		Set<String> old = new HashSet<>();
		long limit = System.currentTimeMillis() - 10 * 60 * 1000;
		for (Map.Entry<String, JsonPageManager> entry : sessions.entrySet()) {
			if (entry.getValue().getLastUsed() < limit) {
				logger.fine("Drop session: " + entry.getKey());
				old.add(entry.getKey());
			}
		}
		old.forEach(key -> sessions.remove(key));
	}
	
	public JsonPageManager getSession(Map<String, Object> input) {
		String sessionId = (String) input.get("session");
		JsonPageManager session = sessions.get(sessionId);
		if (session == null) {
			session = new JsonPageManager();
			input.clear();
			input.put(JsonInput.INITIALIZE, "");
			// input.put(JsonInput.INITIALIZE, sessionId != null ? UNKNOWN_SESSION :
			// NEW_SESSION);
			sessions.put(session.getSessionId(), session);
			if (sessions.size() > MAX_SESSIONS) {
				logger.warning("Session count too high: " + sessions.size());
			}
		}
		return session;
	}

	public void refreshSession(JsonPageManager session) {
		sessions.put(session.getSessionId(), session);
	}

	public String handle(String json) {
		Map<String, Object> data = (Map<String, Object>) JsonReader.read(json);
		JsonPageManager session = getSession(data);
		JsonInput input = new JsonInput(data);
		JsonOutput output = session.handle(input);
		return output.toString();
	}


}
