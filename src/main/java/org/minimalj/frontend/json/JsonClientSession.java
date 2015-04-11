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
import org.minimalj.frontend.page.ActionGroup;
import org.minimalj.frontend.page.ObjectPage;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.page.SearchPage;
import org.minimalj.frontend.toolkit.Action;
import org.minimalj.util.StringUtils;
import org.minimalj.util.resources.Resources;

public class JsonClientSession {

	private static final Map<String, JsonClientSession> sessions = new HashMap<>();
	private final ApplicationContext applicatonContext;
	private Page visiblePage;
	private Map<String, JsonComponent> componentById = new HashMap<>();
	private Map<String, Page> pageById = new HashMap<>();
	private Map<String, Action> actionById = new HashMap<>();
	private JsonOutput output;

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
			if (pageId != null) {
				page = pageById.get(pageId);
			} else {
				page = Application.getApplication().createDefaultPage();
			}
			showPage(page);
		}
		
		Map<String, Object> changedValue = input.get(JsonInput.CHANGED_VALUE);
		for (Map.Entry<String, Object> entry : changedValue.entrySet()) {
			String componentId = entry.getKey();
			String newValue = (String) entry.getValue();
			
			JsonComponent component = componentById.get(componentId);
			((JsonValueComponent) component).setValue((String) newValue); 
		}
		
		String actionId = (String) input.getObject(JsonInput.ACTIVATED_ACTION);
		if (actionId != null) {
			Action action = actionById.get(actionId);
			if (action != null) {
				action.action();
			}
		}
		
		String search = (String) input.getObject("search");
		if (search != null) {
			SearchPage searchPage = Application.getApplication().getSearchPages()[0];
			searchPage.setQuery(search);
			showPage(searchPage);
		}

		JsonClientToolkit.setSession(null);
		return output;
	}

	public void showPage(Page page) {
		actionById.clear();
		componentById.clear();
		
		JsonComponent content = (JsonComponent) page.getContent();
		output.add("content", content.getValues());

		Object menu = createMenu(page);
		output.add("menu", menu);
		
		String pageId = UUID.randomUUID().toString();
		pageById.put(pageId, page);
		output.add("pageId", pageId);
	}

	private List<Object> createMenu(Page page) {
		List<Object> items = new ArrayList<>();
		items.add(createFileMenu());
		Object objectMenu = createObjectMenu(page);
		if (objectMenu != null) {
			items.add(objectMenu);
		}
		return items;
	}
	
	private Map<String, Object> createFileMenu() {
		Map<String, Object> fileMenu = createMenu("file");
		List<Object> fileItems = new ArrayList<>();
		fileMenu.put("items", fileItems);
		
		List<Action> actionsNew = Application.getApplication().getActionsNew();
		if (!actionsNew.isEmpty()) {
			Map<String, Object> newMenu = createMenu("new");
			fileItems.add(newMenu);

			List<Object> itemsNew = createActions(actionsNew);
			newMenu.put("items", itemsNew);
			
			fileItems.add("separator");
		}
		
		List<Action> actionsImport = Application.getApplication().getActionImport();
		if (!actionsImport.isEmpty()) {
			Map<String, Object> importMenu = createMenu("import");
			fileItems.add(importMenu);
			
			List<Object> itemsImport = createActions(actionsImport);
			importMenu.put("items", itemsImport);
		}

		List<Action> actionsExport = Application.getApplication().getActionExport();
		if (!actionsExport.isEmpty()) {
			Map<String, Object> exportMenu = createMenu("export");
			fileItems.add(exportMenu);
			
			List<Object> itemsexport = createActions(actionsExport);
			exportMenu.put("items", itemsexport);
		}
		
//		if (!actionsImport.isEmpty() || !actionsExport.isEmpty()) {
//			fileItems.add("separator");
//		}
		
		return fileMenu;
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

	Map<String, Object> createAction(Action action) {
		Map<String, Object> item = new LinkedHashMap<String, Object>();
		item.put("name", action.getName());
		if (!StringUtils.isEmpty(action.getDescription())) {
			item.put("description", action.getDescription());
		}
		if (action instanceof ActionGroup) {
			ActionGroup actionGroup = (ActionGroup) action;
			item.put("items", createActions(actionGroup.getItems()));
		} else {
			String id = registerAction(action);
			item.put("id", id);
		}
		return item;
	}

	public String registerAction(Action action) {
		String id = UUID.randomUUID().toString();
		actionById.put(id, action);
		return id;
	}

	private Map<String, Object> createObjectMenu(Page page) {
		if (page instanceof ObjectPage) {
			ActionGroup actionGroup = ((ObjectPage<?>) page).getMenu();
			if (actionGroup != null && actionGroup.getItems() != null) {
				Map<String, Object> objectMenu = createAction(actionGroup);
				objectMenu.put("items", createActions(actionGroup.getItems()));
				return objectMenu;
			}
		}
		return null;
	}
	
	private Map<String, Object> createMenu(String resourceName) {
		Map<String, Object> menu = new LinkedHashMap<>();
		menu.put("name", Resources.getString("Menu." + resourceName));
		String description = Resources.getString("Menu." + resourceName + ".description");
		if (!StringUtils.isEmpty(description)) {
			menu.put("description", description);
		}
		return menu;
	}

	public void openDialog(JsonDialog jsonDialog) {
		output.add("dialog", jsonDialog.getValues());
	}

	public void closeDialog(String id) {
		output.add("closeDialog", id);
	}

	public void setProperty(String id, String property, Object value) {
		output.propertyChange(id, property, value);
	}
}
