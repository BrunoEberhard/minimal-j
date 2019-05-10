package org.minimalj.frontend.impl.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

import org.minimalj.application.Application;
import org.minimalj.backend.Backend;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.action.ActionGroup;
import org.minimalj.frontend.impl.json.JsonComponent.JsonPropertyListener;
import org.minimalj.frontend.impl.util.PageAccess;
import org.minimalj.frontend.impl.util.PageList;
import org.minimalj.frontend.impl.util.PageStore;
import org.minimalj.frontend.page.IDialog;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.page.PageManager;
import org.minimalj.frontend.page.Routing;
import org.minimalj.security.Authentication.LoginListener;
import org.minimalj.security.AuthenticationFailedPage;
import org.minimalj.security.Subject;
import org.minimalj.util.LocaleContext;
import org.minimalj.util.StringUtils;

public class JsonPageManager implements PageManager, LoginListener {

	private Subject subject;
	private final Map<String, JsonComponent> componentById = new HashMap<>(100);
	private List<Object> navigation;
	private Runnable onLogin;
	private final PageList visiblePageAndDetailsList = new PageList();
	private JsonOutput output;
	private final JsonPropertyListener propertyListener = new JsonSessionPropertyChangeListener();

	private final PageStore pageStore = new PageStore();

	public JsonPageManager() {
		//
	}

	@Override
	public void loginSucceded(Subject subject) {
		this.subject = subject;
		Subject.setCurrent(subject);

		initialize();
	}

	private void initialize() {
		componentById.clear();
		navigation = createNavigation();
		register(navigation);
		output.add("navigation", navigation);

		if (onLogin != null) {
			onLogin.run();
			onLogin = null;
		} else {
			show(Application.getInstance().createDefaultPage(), null);
		}
	}

	@Override
	public void loginCancelled() {
		if (subject == null && Application.getInstance().isLoginRequired()) {
			show(new AuthenticationFailedPage());
		}
	};

	public String handle(String inputString) {
		JsonInput input = new JsonInput(inputString);
		JsonOutput output = handle(input);
		return output.toString();
	}

	public JsonOutput handle(JsonInput input) {
		JsonFrontend.setSession(this);
		Subject.setCurrent(subject);
		String locale = (String) input.getObject("locale");
		if (locale != null) {
			LocaleContext.setCurrent(Locale.forLanguageTag(locale));
		}
		JsonFrontend.setUseInputTypes(Boolean.TRUE.equals(input.getObject("inputTypes")));

		output = new JsonOutput();

		Object initialize = input.getObject(JsonInput.INITIALIZE);
		if (initialize != null) {
			onLogin = null;
			if (initialize instanceof List) {
				List<String> pageIds = (List<String>) initialize;
				if (pageStore.valid(pageIds)) {
					onLogin = () -> show(pageIds);
				}
			} else if (initialize instanceof String) {
				String path = (String) initialize;
				if (path.length() > 1) {
					Page page = Routing.createPageSafe(path.substring(1));
					if (page != null) {
						onLogin = () -> show(page, null);
					}
				}
			}

			updateTitle(null);
			if (subject == null && Frontend.loginAtStart() && !Boolean.TRUE.equals(input.getObject("dialogVisible"))) {
				Backend.getInstance().getAuthentication().login(this);
			} else {
				initialize();
			}
		}

		if (input.containsObject("closePage")) {
			String pageId = (String) input.getObject("closePage");
			visiblePageAndDetailsList.removeAllFrom(pageId);
		}

		Map<String, Object> changedValues = input.get(JsonInput.CHANGED_VALUE);
		for (Map.Entry<String, Object> entry : changedValues.entrySet()) {
			String componentId = entry.getKey();
			Object newValue = entry.getValue(); // most of the time a String,
												// but not for password

			JsonComponent component = componentById.get(componentId);
			((JsonInputComponent<?>) component).changedValue(newValue);
			output.add("source", componentId);
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

		Map<String, Object> tableSortAction = input.get("tableSortAction");
		if (tableSortAction != null && !tableSortAction.isEmpty()) {
			JsonTable<?> table = (JsonTable<?>) componentById.get(tableSortAction.get("table"));
			int column = ((Long) tableSortAction.get("column")).intValue();
			table.sort(column);
		}

		Map<String, Object> tableSelection = input.get("tableSelection");
		if (tableSelection != null && !tableSelection.isEmpty()) {
			JsonTable<?> table = (JsonTable<?>) componentById.get(tableSelection.get("table"));
			List<Number> rows = ((List<Number>) tableSelection.get("rows"));
			table.selection(rows);
		}

		String tableExtendContent = (String) input.getObject("tableExtendContent");
		if (tableExtendContent != null) {
			JsonTable<?> table = (JsonTable<?>) componentById.get(tableExtendContent);
			output.add("tableId", tableExtendContent);
			output.add("tableExtendContent", table.extendContent());
			output.add("extendable", table.isExtendable());
		}

		String search = (String) input.getObject("search");
		if (search != null) {
			Page searchPage = Application.getInstance().createSearchPage(search);
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

		String openLookupDialog = (String) input.getObject("openLookupDialog");
		if (openLookupDialog != null) {
			JsonLookup lookup = (JsonLookup) componentById.get(openLookupDialog);
			lookup.showLookupDialog();
		}

		String login = (String) input.getObject("login");
		if (login != null || subject == null && Frontend.loginAtStart()
				&& !Boolean.TRUE.equals(input.getObject("dialogVisible"))) {
			Backend.getInstance().getAuthentication().login(this);
		}

		List<String> pageIds = (List<String>) input.getObject("showPages");
		if (pageIds != null) {
			show(pageIds);
		}

		Subject.setCurrent(null);
		JsonFrontend.setSession(null);
		return output;
	}

	@Override
	public void show(Page page) {
		show(page, null);
		updateTitle(page);
	}

	@Override
	public void showDetail(Page mainPage, Page detail) {
		int pageIndex = visiblePageAndDetailsList.indexOf(detail);
		if (pageIndex < 0) {
			String mainPageId = visiblePageAndDetailsList.getId(mainPage);
			show(detail, mainPageId);
		} else {
			String pageId = visiblePageAndDetailsList.getId(pageIndex);
			output.add("pageId", pageId);
			output.add("title", detail.getTitle());
		}
	}

	private void show(Page page, String masterPageId) {
		if (masterPageId == null) {
			visiblePageAndDetailsList.clear();
			componentById.clear();
			register(navigation);
		} else {
			visiblePageAndDetailsList.removeAllAfter(masterPageId);
		}

		String pageId = pageStore.put(page);
		output.add("showPage", createJson(page, pageId, masterPageId));
		visiblePageAndDetailsList.put(pageId, page);
	}

	private void show(List<String> pageIds) {
		List<JsonComponent> jsonList = new ArrayList<>();
		visiblePageAndDetailsList.clear();
		String previousId = null;
		Page firstPage = null;
		for (String pageId : pageIds) {
			Page page = pageStore.get(pageId);
			visiblePageAndDetailsList.put(pageId, page);
			jsonList.add(createJson(page, pageId, previousId));
			if (previousId == null) {
				firstPage = page;
			}
			previousId = pageId;
		}
		output.add("showPages", jsonList);
		updateTitle(firstPage != null ? firstPage : null);
	}

	private void updateTitle(Page page) {
		String title = page != null ? page.getTitle() : null;
		if (StringUtils.isEmpty(title)) {
			title = Application.getInstance().getName();
		}
		if (StringUtils.isEmpty(title)) {
			title = "Minimal-J";
		}
		output.add("title", title);
	}
	
	private JsonComponent createJson(Page page, String pageId, String masterPageId) {
		JsonComponent json = new JsonComponent("page");

		json.put("masterPageId", masterPageId);
		json.put("pageId", pageId);
		json.put("title", page.getTitle());
		String route = Routing.getRouteSafe(page);
		if (route != null) {
			json.put("route", route.endsWith("/") ? route : route + "/");
		}

		JsonComponent content = (JsonComponent) PageAccess.getContent(page);
		register(content);
		json.put("content", content);

		List<Object> actionMenu = createActionMenu(page);
		register(actionMenu);
		json.put("actionMenu", actionMenu);

		return json;
	}

	@Override
	public void hideDetail(Page page) {
		output.add("hidePage", visiblePageAndDetailsList.getId(page));
		visiblePageAndDetailsList.removeAllFrom(page);
	}

	@Override
	public boolean isDetailShown(Page page) {
		return visiblePageAndDetailsList.contains(page);
	}

	@Override
	public IDialog showDialog(String title, IContent content, Action saveAction, Action closeAction, Action... actions) {
		JsonDialog dialog = new JsonDialog(title, content, saveAction, actions);
		openDialog(dialog);
		return dialog;
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
		List<Action> navigationActions = Application.getInstance().getNavigation();
		List<Object> navigationItems = createActions(navigationActions);
		return navigationItems;
	}

	private List<Object> createActionMenu(Page page) {
		List<Action> actions = PageAccess.getActions(page);
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
		travers(o, component -> {
			String id = component.getId();
			if (id != null) {
				componentById.put(component.getId(), component);
			}
			component.setPropertyListener(propertyListener);
		});
	}

	public void unregister(Object o) {
		travers(o, component -> componentById.remove(component.getId()));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void travers(Object o, Consumer<JsonComponent> c) {
		if (o instanceof JsonComponent) {
			c.accept((JsonComponent) o);
		}
		if (o instanceof Map) {
			((Map) o).values().forEach(v -> travers(v, c));
		}
		if (o instanceof Collection) {
			((Collection) o).forEach(v -> travers(v, c));
		}
	}

	public void openDialog(JsonDialog jsonDialog) {
		register(jsonDialog);
		output.add("dialog", jsonDialog);
	}

	public void closeDialog(JsonDialog dialog) {
		unregister(dialog);
		output.addElement("closeDialog", dialog.getId());
	}

	public void clearContent(JsonSwitch jsonSwitch) {
		jsonSwitch.values().forEach(this::unregister);
		output.removeContent(jsonSwitch.getId());
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
