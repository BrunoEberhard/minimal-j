package org.minimalj.frontend.impl.json;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.minimalj.application.Configuration;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.page.TablePage;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;

public class JsonSessionManager extends TimerTask {
	private static final Logger logger = Logger.getLogger(JsonSessionManager.class.getName());

	private static final int MAX_SESSIONS = Integer.valueOf(Configuration.get("MjMaxSessions", "200"));

	private final Map<String, JsonPageManager> sessions = new HashMap<>();

	private static final JsonSessionManager instance = new JsonSessionManager();

	private JsonSessionManager() {
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(this, 1 * 60 * 1000, 1 * 60 * 1000);
	}

	public static JsonSessionManager getInstance() {
		return instance;
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
			sessions.put(session.getSessionId(), session);
			if (sessions.size() > MAX_SESSIONS) {
				logger.warning("Session count too high: " + sessions.size());
			}
		}
		return session;
	}

	public String handle(InputStream inputStream) {
		@SuppressWarnings("unchecked")
		Map<String, Object> data = (Map<String, Object>) JsonReader.read(inputStream);
		return handle(data);
	}

	public String handle(Map<String, Object> data) {
		JsonPageManager session = getSession(data);
		JsonInput input = new JsonInput(data);
		String output;
		synchronized (session) {
			JsonOutput jsonOutput = session.handle(input);
			output = jsonOutput.toString();
		}
		return output;
	}

	public String export(String sessionId, String id) {
		JsonPageManager session = sessions.get(sessionId);
		if (session != null) {
			return session.export(id);
		} else {
			return null;
		}
	}
	
	public static class JsonSessionInfo {
		public static final JsonSessionInfo $ = Keys.of(JsonSessionInfo.class);
		
		public String sessionId;
		@Size(Size.TIME_WITH_SECONDS)
		public LocalDateTime lastUsed;
		public String subject;
		public Integer components;
		public String page;
		public Integer storedPages;
		
		public String getSession() {
			if (Keys.isKeyObject(this)) {
				return Keys.methodOf(this, "session");
			}
			return sessionId.substring(0, 8);
		}
	}
	
	List<JsonSessionInfo> getSessionInfos() {
		return sessions.values().stream().map(JsonPageManager::getSessionInfo).collect(Collectors.toList());
	}
	
	public static class JsonSessionTablePage extends TablePage<JsonSessionInfo> {
		
		@Override
		protected Object[] getColumns() {
			return new Object[] { JsonSessionInfo.$.getSession(), JsonSessionInfo.$.subject, JsonSessionInfo.$.lastUsed, JsonSessionInfo.$.page, JsonSessionInfo.$.components, JsonSessionInfo.$.storedPages }; 
		}

		@Override
		protected List<JsonSessionInfo> load() {
			return instance.getSessionInfos();
		}

		@Override
		protected boolean allowMultiselect() {
			return true;
		}

		@Override
		public List<Action> getTableActions() {
			return Collections.singletonList(new JsonSessionRemoveAction());
		}
		
		private class JsonSessionRemoveAction extends AbstractObjectsAction<JsonSessionInfo> {
			
			@Override
			public void run() {
				getSelectedObjects().forEach(info -> {
					instance.sessions.remove(info.sessionId);
				});
				refresh();
			}
		}
		
	}

}
