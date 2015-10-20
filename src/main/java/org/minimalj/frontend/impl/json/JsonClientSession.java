package org.minimalj.frontend.impl.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.minimalj.application.Application;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.Frontend.Search;
import org.minimalj.frontend.Frontend.TableActionListener;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.action.ActionGroup;
import org.minimalj.frontend.impl.json.JsonComponent.JsonPropertyListener;
import org.minimalj.frontend.impl.util.PageList;
import org.minimalj.frontend.impl.util.PageStore;
import org.minimalj.frontend.page.IDialog;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.page.PageManager;
import org.minimalj.security.LoginAction;
import org.minimalj.security.Subject;

public class JsonClientSession implements PageManager {

	private Subject subject;
	private String focusPageId;
	private final Map<String, JsonComponent> componentById = new HashMap<>(100);
	private List<Object> navigation;
	private final PageList visiblePageAndDetailsList = new PageList();
	private JsonOutput output;
	private final JsonPropertyListener propertyListener = new JsonSessionPropertyChangeListener();

	private final PageStore pageStore = new PageStore();
	
	public JsonClientSession() {
	}
	
	@Override
	public void setSubject(Subject subject) {
		this.subject = subject;
		
		componentById.clear();
		navigation = createNavigation();
		register(navigation);
		output.add("navigation", navigation);
	}
	
	@Override
	public Subject getSubject() {
		return subject;
	}
	
	public JsonOutput handle(JsonInput input) {
		JsonFrontend.setSession(this);
		output = new JsonOutput();

		if (input.containsObject("focusPageId")) {
			focusPageId = (String) input.getObject("focusPageId"); 
		} else {
			focusPageId = null;
		}
		
		if (input.containsObject(JsonInput.SHOW_DEFAULT_PAGE)) {
			Page page = Application.getApplication().createDefaultPage();
			String pageId = UUID.randomUUID().toString();
			show(page, pageId, null);

			navigation = createNavigation();
			register(navigation);
			output.add("navigation", navigation);
		}
		
		if (input.containsObject("closePage")) {
			String pageId = (String) input.getObject("closePage");
			visiblePageAndDetailsList.removeAllFrom(pageId);
		}
		
		Map<String, Object> changedValue = input.get(JsonInput.CHANGED_VALUE);
		for (Map.Entry<String, Object> entry : changedValue.entrySet()) {
			String componentId = entry.getKey();
			Object newValue = entry.getValue(); // most of the time a String, but not for password
			
			JsonComponent component = componentById.get(componentId);
			((JsonInputComponent<?>) component).changedValue(newValue); 
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
		
		String loadSuggestions = (String) input.getObject("loadSuggestions");
		if (loadSuggestions != null) {
			JsonTextField textField = (JsonTextField) componentById.get(loadSuggestions);
			String searchText = (String) input.getObject("searchText");
			List<String> suggestions = textField.getSuggestions().search(searchText);
			output.add("suggestions", suggestions);
			output.add("loadSuggestions", loadSuggestions);
		}

		String login = (String) input.getObject("login");
		if (login != null) {
			new LoginAction().action();
		}
		
		List<String> pageIds = (List<String>) input.getObject("showPages");
		if (pageIds != null) {
			show(pageIds);
		}
		
		JsonFrontend.setSession(null);
		return output;
	}
	
	@Override
	public void show(Page page) {
		show(page, UUID.randomUUID().toString(), null);
	}
	
	@Override
	public void showDetail(Page mainPage, Page detail) {
		int pageIndex = visiblePageAndDetailsList.indexOf(detail);
		if (pageIndex < 0) {
			show(detail, UUID.randomUUID().toString(), focusPageId);
		} else {
			String pageId = visiblePageAndDetailsList.getId(pageIndex);
			output.add("pageId", pageId);
			output.add("title", detail.getTitle());
		}
	}
	
	private void show(Page page, String pageId, String masterPageId) {
		if (masterPageId == null) {
			componentById.clear();
			register(navigation);
		}
		
		output.add("showPage", createJson(page, pageId, masterPageId));
		visiblePageAndDetailsList.put(pageId, page);
		pageStore.put(pageId, page);
	}
	
	private void show(List<String> pageIds) {
		List<JsonComponent> jsonList = new ArrayList<>();
		visiblePageAndDetailsList.clear();
		String previousId = null;
		for (String pageId : pageIds) {
			Page page = pageStore.get(pageId);
			visiblePageAndDetailsList.put(pageId, page);
			jsonList.add(createJson(page, pageId, previousId));
			previousId = pageId;
		}
		output.add("showPages", jsonList);
	}
	
	private JsonComponent createJson(Page page, String pageId, String masterPageId) {
		JsonComponent json = new JsonComponent("page");
		
		json.put("masterPageId", masterPageId);
		json.put("pageId", pageId);
		json.put("title", page.getTitle());
		
		JsonComponent content = (JsonComponent) page.getContent();
		register(content);
		json.put("content", content);
		
		List<Object> actionMenu = createActionMenu(page);
		register(actionMenu);
		json.put("actionMenu", actionMenu);
		
		return json;
	}
	
	@Override
	public void hideDetail(Page page) {
		visiblePageAndDetailsList.removeAllFrom(page);
	}
	
	@Override
	public boolean isDetailShown(Page page) {
		return visiblePageAndDetailsList.contains(page);
	}

	@Override
	public IDialog showDialog(String title, IContent content, Action saveAction, Action closeAction, Action... actions) {
		return new JsonDialog(title, content, saveAction, actions);
	}

	@Override
	public <T> IDialog showSearchDialog(Search<T> index, Object[] keys, TableActionListener<T> listener) {
		return new JsonDialog.JsonSearchDialog(index, keys, listener);
	}

	@Override
	public void showMessage(String text) {
		output.add("message", text);
	}

	@Override
	public void showError(String text) {
		output.add("error", text);
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
