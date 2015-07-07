package org.minimalj.frontend.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.minimalj.application.Application;
import org.minimalj.application.ApplicationContext;
import org.minimalj.frontend.json.JsonComponent.JsonPropertyListener;
import org.minimalj.frontend.page.ActionGroup;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.toolkit.Action;
import org.minimalj.util.StringUtils;
import org.minimalj.util.resources.Resources;

public class JsonClientSession {

	private static final Map<String, JsonClientSession> sessions = new HashMap<>();
	private final ApplicationContext applicatonContext;
	private Page visiblePage;
	private String visiblePageId;
	private Map<String, JsonComponent> componentById = new HashMap<>(100);
	private Map<String, Page> pageById = new HashMap<>();
	private JsonOutput output;
	private final JsonPropertyListener propertyListener = new JsonSessionPropertyChangeListener();

	public JsonClientSession(ApplicationContext context) {
		this.applicatonContext = context;
	}
	
	public static JsonClientSession getSession(String sessionId) {
		return sessions.get(sessionId);
	}
	
	public static String createSession() {
		String sessionId = UUID.randomUUID().toString();
		JsonClientSession session = new JsonClientSession(null);
		sessions.put(sessionId, session);
		return sessionId;
	}
	
	public JsonOutput handle(JsonInput input) {
		JsonClientToolkit.setSession(this);
		output = new JsonOutput();

		if (input.containsObject(JsonInput.SHOW_PAGE)) {
			String pageId = (String) input.getObject(JsonInput.SHOW_PAGE);
			Page page;
			if (pageById.containsKey(pageId)) {
				page = pageById.get(pageId);
			} else {
				page = Application.getApplication().createDefaultPage();
			}
			showPage(page, pageId);
		}
		
		Map<String, Object> changedValue = input.get(JsonInput.CHANGED_VALUE);
		for (Map.Entry<String, Object> entry : changedValue.entrySet()) {
			String componentId = entry.getKey();
			String newValue = (String) entry.getValue();
			
			JsonComponent component = componentById.get(componentId);
			((JsonInputComponent) component).changedValue(newValue); 
		}
		
		String actionId = (String) input.getObject(JsonInput.ACTIVATED_ACTION);
		if (actionId != null) {
			JsonAction action = (JsonAction) componentById.get(actionId);
			action.action();
		}
		
		Map<String, Object> tableAction = input.get(JsonInput.TABLE_ACTION);
		if (tableAction != null && !tableAction.isEmpty()) {
			JsonTable<?> table = (JsonTable<?>) componentById.get(tableAction.get("table"));
			int row = ((Long) tableAction.get("row")).intValue();
			table.action(row);
		}
		
		String search = (String) input.getObject("search");
		if (search != null) {
			Page searchPage = Application.getApplication().createSearchPage(search);
			showPage(searchPage);
		}

		JsonClientToolkit.setSession(null);
		return output;
	}

	public void showPage(Page page) {
		showPage(page, UUID.randomUUID().toString());
	}
	
	public void showPage(Page page, String pageId) {
		componentById.clear();
		
		JsonComponent content = (JsonComponent) page.getContent();
		register(content);
		output.add("content", content);
		output.add("title", page.getTitle());

		Object menu = createMenu(page);
		register(menu);
		output.add("menu", menu);
		
		pageById.put(pageId, page);
		output.add("pageId", pageId);
		
		this.visiblePage = page;
		this.visiblePageId = pageId;
	}
	
	public void refresh() {
		showPage(visiblePage, visiblePageId);
	}

	private List<Object> createMenu(Page page) {
		List<Object> items = new ArrayList<>();
		items.add(createMenu());
		Object objectMenu = createObjectMenu(page);
		if (objectMenu != null) {
			items.add(objectMenu);
		}
		return items;
	}
	
	private Map<String, Object> createMenu() {
		Map<String, Object> menu = createMenu("menu");
		List<Action> menuActions = Application.getApplication().getMenu();
		List<Object> menuItems = createActions(menuActions);
		menu.put("items", menuItems);
		
		return menu;
	}
	
	List<Object> createActions(List<Action> actions) {
		List<Object> items = new ArrayList<>();
		for (Action action : actions) {
			items.add(createAction(action));
		}
		return items;
	}
	
	List<Object> createActions(Action[] actions) {
		return createActions(Arrays.asList(actions));
	}

	JsonComponent createAction(Action action) {
		JsonComponent item;
		if (action instanceof ActionGroup) {
			ActionGroup actionGroup = (ActionGroup) action;
			item = new JsonAction.JsonActionGroup();
			item.put("items", createActions(actionGroup.getItems()));
		} else {
			item = new JsonAction(action);
		}
		item.put("name", action.getName());
		return item;
	}

	public void register(Object o) {
		if (o instanceof JsonComponent) {
			JsonComponent component = (JsonComponent) o;
			String id = component.getId();
			if (id != null) {
				componentById.put(component.getId(), component);
			}
			component.setPropertyListener(propertyListener);
		}
		if (o instanceof Map) {
			@SuppressWarnings("rawtypes")
			Map map = (Map) o;
			for (Object o2 : map.values()) {
				register(o2);
			}
		}
		if (o instanceof List) {
			@SuppressWarnings("rawtypes")
			List list = (List) o;
			for (Object o2 : list) {
				register(o2);
			}
		}
	}

	private Map<String, Object> createObjectMenu(Page page) {
		ActionGroup actionGroup = page.getMenu();
		if (actionGroup != null && actionGroup.getItems() != null) {
			Map<String, Object> objectMenu = createAction(actionGroup);
			objectMenu.put("items", createActions(actionGroup.getItems()));
			return objectMenu;
		}
		return null;
	}
	
	private Map<String, Object> createMenu(String resourceName) {
		Map<String, Object> menu = new LinkedHashMap<>();
		menu.put("name", Resources.getString("Menu." + resourceName));
		String description = Resources.getString("Menu." + resourceName + ".description", Resources.OPTIONAL);
		if (!StringUtils.isEmpty(description)) {
			menu.put("description", description);
		}
		return menu;
	}

	public void openDialog(JsonDialog jsonDialog) {
		register(jsonDialog);
		output.add("dialog", jsonDialog);
	}

	public void closeDialog(String id) {
		output.add("closeDialog", id);
	}
	
	public void clearContent(String elementId) {
		output.removeContent(elementId);
	}
	
	public void addContent(String elementId, JsonComponent content) {
		register(content);
		output.addContent(elementId, content);
	}
	
	private class JsonSessionPropertyChangeListener implements JsonPropertyListener {
		
		public void propertyChange(String componentId, String property, Object value) {
			output.propertyChange(componentId, property, value);
		}
	}

}
