package org.minimalj.frontend.json;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.minimalj.application.Application;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.Frontend.Search;
import org.minimalj.frontend.Frontend.TableActionListener;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.action.ActionGroup;
import org.minimalj.frontend.json.JsonComponent.JsonPropertyListener;
import org.minimalj.frontend.page.IDialog;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.page.PageBrowser;
import org.minimalj.util.StringUtils;
import org.minimalj.util.resources.Resources;

public class JsonClientSession implements PageBrowser {

	private static final Map<String, JsonClientSession> sessions = new HashMap<>();
	private String focusPageId;
	private Map<String, JsonComponent> componentById = new HashMap<>(100);
	private Map<String, Page> pageById = new HashMap<>();
	private Map<Page, String> idByPage = new HashMap<>();
	private JsonOutput output;
	private final JsonPropertyListener propertyListener = new JsonSessionPropertyChangeListener();

	public JsonClientSession() {
	}
	
	public static JsonClientSession getSession(String sessionId) {
		return sessions.get(sessionId);
	}
	
	public static String createSession() {
		String sessionId = UUID.randomUUID().toString();
		JsonClientSession session = new JsonClientSession();
		sessions.put(sessionId, session);
		return sessionId;
	}
	
	public JsonOutput handle(JsonInput input) {
		Frontend.setBrowser(this);
		output = new JsonOutput();

		if (input.containsObject("focusPageId")) {
			focusPageId = (String) input.getObject("focusPageId"); 
		} else {
			focusPageId = null;
		}
		
		if (input.containsObject(JsonInput.SHOW_PAGE)) {
			String pageId = (String) input.getObject(JsonInput.SHOW_PAGE);
			Page page;
			if (pageById.containsKey(pageId)) {
				page = pageById.get(pageId);
			} else {
				page = Application.getApplication().createDefaultPage();
			}
			show(page, pageId, null);
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
		
		Map<String, Object> tableSelection = input.get("tableSelection");
		if (tableSelection != null && !tableSelection.isEmpty()) {
			JsonTable<?> table = (JsonTable<?>) componentById.get(tableSelection.get("table"));
			int row = ((Long) tableSelection.get("row")).intValue();
			List<Number> rows = ((List<Number>) tableSelection.get("rows"));
			table.selection(row, rows);
		}
		
		String search = (String) input.getObject("search");
		if (search != null) {
			Page searchPage = Application.getApplication().createSearchPage(search);
			show(searchPage);
		}

		Frontend.setBrowser(null);
		return output;
	}

	@Override
	public void show(Page page) {
		show(page, UUID.randomUUID().toString(), null);
	}
	
	@Override
	public void showDetail(Page page) {
		show(page, UUID.randomUUID().toString(), focusPageId);
	}
	
	private void show(Page page, String pageId, String masterPageId) {
		if (masterPageId == null) {
			componentById.clear();
		}
		
		JsonComponent content = (JsonComponent) page.getContent();
		register(content);
		output.add("content", content);
		output.add("title", page.getTitle());
		output.add("masterPageId", masterPageId);
		
		// TODO move this to none page specific
		List<Object> navigation = createNavigation();
		register(navigation);
		output.add("navigation", navigation);
		
		List<Object> actionMenu = createActionMenu(page);
		register(actionMenu);
		output.add("actionMenu", actionMenu);
		
		pageById.put(pageId, page);
		idByPage.put(page, pageId);
		output.add("pageId", pageId);
	}
	
	@Override
	public void hideDetail(Page page) {
		if (idByPage.containsKey(page)) {
			String pageId = idByPage.get(page);
			output.add("hideDetail", pageId);
		}
	}
	
	@Override
	public boolean isDetailShown(Page page) {
		// TODO handling of open details in JSON client
		return true;
	}
	
	@Override
	public void refresh() {
		// TODO remove refresh from PageBrowser interface
		// show(visiblePage, visiblePageId, false);
	}

	@Override
	public IDialog showDialog(String title, IContent content, Action saveAction, Action closeAction, Action... actions) {
		// TODO use saveAction (Enter in TextFields should save the dialog)
		return new JsonDialog(title, content, closeAction, actions);
	}

	@Override
	public <T> IDialog showSearchDialog(Search<T> index, Object[] keys, TableActionListener<T> listener) {
		return new JsonDialog.JsonSearchDialog(index, keys, listener);
	}

	@Override
	public void showMessage(String text) {
		// TODO Auto-generated method stub
	}

	@Override
	public void showError(String text) {
		// TODO Auto-generated method stub
	}

	@Override
	public void showConfirmDialog(String message, String title, ConfirmDialogType type, DialogListener listener) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public OutputStream store(String buttonText) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InputStream load(String buttonText) {
		// TODO Auto-generated method stub
		return null;
	}	
	
	private List<Object> createNavigation() {
		List<Action> menuActions = Application.getApplication().getMenu();
		List<Object> menuItems = createActions(menuActions);
		return menuItems;
	}
	
	private List<Object> createActionMenu(Page page) {
		List<Action> actions = page.getActions();
		if (actions != null && actions.size() > 0) {
			return createActions(actions);
		}
		return null;
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
		
		@Override
		public void propertyChange(String componentId, String property, Object value) {
			output.propertyChange(componentId, property, value);
		}
	}

}
