package org.minimalj.frontend.impl.json;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.minimalj.application.Application;
import org.minimalj.application.Application.AuthenticatonMode;
import org.minimalj.application.Configuration;
import org.minimalj.backend.Backend;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.action.ActionGroup;
import org.minimalj.frontend.action.Separator;
import org.minimalj.frontend.impl.json.JsonComponent.JsonPropertyListener;
import org.minimalj.frontend.impl.json.JsonSessionManager.JsonSessionInfo;
import org.minimalj.frontend.impl.util.PageAccess;
import org.minimalj.frontend.impl.util.PageList;
import org.minimalj.frontend.impl.util.PageStore;
import org.minimalj.frontend.page.ExpiredPage;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.page.Page.Dialog;
import org.minimalj.frontend.page.Page.WheelPage;
import org.minimalj.frontend.page.PageManager;
import org.minimalj.frontend.page.Routing;
import org.minimalj.security.Authentication;
import org.minimalj.security.Authorization;
import org.minimalj.security.RememberMeAuthentication;
import org.minimalj.security.Subject;
import org.minimalj.util.EqualsHelper;
import org.minimalj.util.StringUtils;

public class JsonPageManager implements PageManager {
	private static final Logger logger = Logger.getLogger(JsonPageManager.class.getName());

	private final String sessionId;
	private final Authentication authentication;
	private long lastUsed = System.currentTimeMillis();
	
	private Subject subject;
	private Runnable onLogin;
	@Deprecated
	private boolean initializing;
	private String locationFragment;
	private final Map<String, JsonComponent> componentById = new HashMap<>(100);
	private List<Object> navigation;
	private final PageList visiblePageAndDetailsList = new PageList();
	private final Set<String> horizontalPageIds = new HashSet<>();
	private final Map<Dialog, JsonDialog> visibleDialogs = new HashMap<>();
	// this makes this class not thread safe. Caller of handle have to synchronize.
	private JsonOutput output;
	private final JsonPropertyListener propertyListener = new JsonSessionPropertyChangeListener();

	private final PageStore pageStore = new PageStore();

	private final JsonPush push;

	public JsonPageManager() {
		this(null);
	}

	public JsonPageManager(JsonPush push) {
		sessionId = UUID.randomUUID().toString();
		authentication = Backend.getInstance().getAuthentication();
		this.push = push;
	}

	public String getSessionId() {
		return sessionId;
	}
	
	public String getLocationFragment() {
		return locationFragment;
	}
	
	public long getLastUsed() {
		return lastUsed;
	}

	private void updateNavigation() {
		output.add("navigation", navigation);
		output.add("hasSearchPages", Application.getInstance().hasSearch());
	}

	public String handle(String inputString) {
		JsonInput input = new JsonInput(inputString);
		JsonOutput output = handle(input);
		return output.toString();
	}

	private Thread thread;

	public JsonOutput handle(JsonInput input) {
		lastUsed = System.currentTimeMillis();
		
		Long retry = (Long) input.getObject("retry");
		if (retry == null || thread == null) {
			retry = 0L;
			thread = new Thread(new Runnable() {
				public void run() {
					try {
						JsonFrontend.setSession(JsonPageManager.this);
						Subject.setCurrent(subject);
						handle_(input);
					} catch (ComponentUnknowException x) {
						logger.log(Level.WARNING, x.getMessage(), x);
						output = new JsonOutput();
						show(visiblePageAndDetailsList.getPageIds(), true);
					} catch (Exception x) {
						output.add("error", x.getClass().getSimpleName() + ":\n" + x.getMessage());
						logger.log(Level.SEVERE, x.getMessage(), x);
					} finally {
						subject = Subject.getCurrent();
						Subject.setCurrent(null);
						JsonFrontend.setSession(null);
					}
				}
			});
			thread.start();
		}

		try {
			thread.join(2000);
		} catch (InterruptedException t) {
		}

		if (!thread.isAlive()) {
			output.add("session", sessionId);
			return output;
		} else {
			JsonOutput output = new JsonOutput();
			output.add("wait", ++retry);
			return output;
		}
	}
	
	public String export(String id) {
		Object content = componentById.get(id);
		if (content instanceof JsonTable) {
			try {
				Subject.setCurrent(subject);
				return ((JsonTable<?>) content).export();
			} finally {
				Subject.setCurrent(null);
			}
		} else {
			return null;
		}
	}

	private static class ComponentUnknowException extends Exception {
		private static final long serialVersionUID = 1L;

		public ComponentUnknowException(Object id) {
			super("Component not registred: " + id);
		}
	}

	private JsonComponent getComponentById(Object id) throws ComponentUnknowException {
		JsonComponent result = componentById.get(id);
		if (result == null && Configuration.isDevModeActive()) {
			throw new ComponentUnknowException(id);
		}
		return result;
	}

	private JsonOutput handle_(JsonInput input) throws ComponentUnknowException {
		JsonFrontend.setUseInputTypes(Boolean.TRUE.equals(input.getObject("inputTypes")));

		output = new JsonOutput();

		Object initialize = input.getObject(JsonInput.INITIALIZE);
		initializing = initialize != null; 
		if (initializing) {
			locationFragment = (String) input.getObject("locationFragment");
			if (initialize instanceof List) {
				List<String> pageIds = (List<String>) initialize;
				if (!pageIds.isEmpty() && pageStore.valid(pageIds)) {
					onLogin = () -> show(pageIds, false);
				} else {
					onLogin = () -> show(Application.getInstance().createDefaultPage());
				}
			} else if (initialize instanceof String) {
				String path = (String) initialize;
				onLogin = () -> show(Routing.createPageSafe(path));
			}
			
			if (subject == null && authentication instanceof RememberMeAuthentication) {
				RememberMeAuthentication rememberMeAuthentication = (RememberMeAuthentication) authentication;
				String token = (String) input.getObject("rememberMeToken");
				if (token != null) {
					login(rememberMeAuthentication.remember(token));
				}
			}

			login(subject);
		} else {
			locationFragment = null;
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

			JsonComponent component = getComponentById(componentId);
			if (component != null) {
				((JsonInputComponent<?>) component).changedValue(newValue);
				output.add("source", componentId);
			}
		}

		String actionId = (String) input.getObject(JsonInput.ACTIVATED_ACTION);
		if (actionId != null) {
			JsonAction action = (JsonAction) getComponentById(actionId);
			action.run();
		}

		Map<String, Object> tableAction = input.get(JsonInput.TABLE_ACTION);
		if (tableAction != null && !tableAction.isEmpty()) {
			JsonTable<?> table = (JsonTable<?>) getComponentById(tableAction.get("table"));
			int row = ((Long) tableAction.get("row")).intValue();
			table.action(row);
		}

		Map<String, Object> tableSortAction = input.get("tableSortAction");
		if (tableSortAction != null && !tableSortAction.isEmpty()) {
			JsonTable<?> table = (JsonTable<?>) getComponentById(tableSortAction.get("table"));
			int column = ((Long) tableSortAction.get("column")).intValue();
			table.sort(column);
		}

		Map<String, Object> tableSelection = input.get("tableSelection");
		if (tableSelection != null && !tableSelection.isEmpty()) {
			JsonTable<?> table = (JsonTable<?>) getComponentById(tableSelection.get("table"));
			List<Number> rows = ((List<Number>) tableSelection.get("rows"));
			table.selection(rows);
		}

		String tablePage = (String) input.getObject("tablePage");
		if (tablePage != null) {
			JsonTable<?> table = (JsonTable<?>) getComponentById(tablePage);
			String direction = (String) input.getObject("direction");
			table.page(direction);
		}

		String tableFilterVisible = (String) input.getObject("tableFilter");
		if (tableFilterVisible != null) {
			JsonTable<?> table = (JsonTable<?>) getComponentById(tableFilterVisible);
			boolean filter = Boolean.TRUE.equals(input.getObject("filter"));
			table.setFilterVisible(filter);
		}
		
		Map<String, Object> cellAction = input.get("cellAction");
		if (cellAction != null && !cellAction.isEmpty()) {
			JsonTable<?> table = (JsonTable<?>) getComponentById(cellAction.get("table"));
			int row = ((Long) cellAction.get("row")).intValue();
			int column = ((Long) cellAction.get("column")).intValue();
			table.cellAction(row, column);
		}
		
		String search = (String) input.getObject("search");
		if (search != null && Application.getInstance().hasSearch()) {
			Application.getInstance().search(search);
		}

		String loadSuggestions = (String) input.getObject("loadSuggestions");
		if (loadSuggestions != null) {
			JsonTextField textField = (JsonTextField) getComponentById(loadSuggestions);
			String searchText = (String) input.getObject("searchText");
			List<String> suggestions = textField.getSuggestions().search(searchText);
			suggestions = suggestions != null && suggestions.size() > 50 ? suggestions.subList(0, 50) : suggestions;
			output.add("suggestions", suggestions);
			output.add("loadSuggestions", loadSuggestions);
		}

		String openLookupDialog = (String) input.getObject("openLookupDialog");
		if (openLookupDialog != null) {
			JsonLookup lookup = (JsonLookup) getComponentById(openLookupDialog);
			lookup.showLookupDialog();
		}
		
		Long wheel = (Long) input.getObject("wheel");
		if (wheel != null) {
			String pageId = (String) input.getObject("page");
			Page page = pageStore.get(pageId);
			if (page instanceof WheelPage) {
				((WheelPage) page).wheel(wheel.intValue());
				String route = Routing.getRouteSafe(page);
				if (route != null) {
					output.add("route", route);
				}
			}
		}
		
		if (input.containsObject("logout")) {
			componentById.clear();
			pageStore.clear();
			if (Subject.getCurrent() != null) {
				Backend.getInstance().getAuthentication().getLogoutAction().run();
			}
			if (Application.getInstance().getAuthenticatonMode() == AuthenticatonMode.REQUIRED) {
				Backend.getInstance().getAuthentication().showLogin();
			}
		}
		if (input.containsObject("login")) {
			Backend.getInstance().getAuthentication().getLoginAction().run();
		}

		List<String> pageIds = (List<String>) input.getObject("showPages");
		if (pageIds != null) {
			if (pageStore.valid(pageIds)) {
				show(pageIds, true);
			} else {
				Page page = new ExpiredPage();
				String pageId = pageStore.put(page);
				output.add("showPages", Collections.singletonList(createJson(page, pageId, null)));
			}
		}
		
		register(output);
		return output;
	}

	@Override
	public void show(Page page) {
		show(page, null);
		updateTitle(page);
	}

	@Override
	public void showDetail(Page mainPage, Page detail, boolean horizontalDetailLayout) {
		int pageIndex = visiblePageAndDetailsList.indexOf(detail);
		String pageId;
		if (pageIndex < 0) {
			String mainPageId = visiblePageAndDetailsList.getId(mainPage);
			pageId = show(detail, mainPageId);
		} else {
			pageId = visiblePageAndDetailsList.getId(pageIndex);
			output.add("pageId", pageId);
			output.add("title", detail.getTitle());
		}
		if (horizontalDetailLayout) {
			horizontalPageIds.add(pageId);
		} else {
			horizontalPageIds.remove(pageId);
		}
		output.add("horizontalDetailLayout", Boolean.valueOf(horizontalDetailLayout));
	}
	
	private String show(Page page, String masterPageId) {
		if (!Authorization.hasAccess(Subject.getCurrent(), page)) {
			if (authentication == null) {
				throw new IllegalStateException("Page " + page.getClass().getSimpleName() + " is annotated with @Role but authentication is not configured.");
			}
			onLogin = () -> show(page, masterPageId);
			authentication.showLogin();
			return null;
		}
		if (masterPageId == null) {
			visiblePageAndDetailsList.clear();
			componentById.clear();
			// navigation is not part of the output. Needs special registration
			register(navigation);
		} else {
			visiblePageAndDetailsList.removeAllAfter(masterPageId);
		}

		String pageId = pageStore.getId(page);
		if (pageId != null && page instanceof WheelPage) {
			output.add("updatePage", createJson(page, pageId, masterPageId));
		} else {
			pageId = pageStore.put(page);
			output.add("showPage", createJson(page, pageId, masterPageId));
		}
		visiblePageAndDetailsList.put(pageId, page);
		visibleDialogs.clear();
		return pageId;
	}

	private void show(List<String> pageIds, boolean loginNotAuthorized) {
		if (!pageStore.valid(pageIds)) {
			Frontend.show(Application.getInstance().createDefaultPage());
			return;
		}
		List<JsonComponent> jsonList = new ArrayList<>();
		visiblePageAndDetailsList.clear();
		String previousId = null;
		Page firstPage = null;
		boolean authorized = true;
		for (String pageId : pageIds) {
			Page page = pageStore.get(pageId);
			if (Authorization.hasAccess(Subject.getCurrent(), page)) {
				visiblePageAndDetailsList.put(pageId, page);
				visibleDialogs.clear();
				jsonList.add(createJson(page, pageId, previousId));
				if (previousId == null) {
					firstPage = page;
				}
				previousId = pageId;
			} else {
				authorized = false;
			}
		}
		if (authorized) {
			output.add("horizontalDetailLayout", pageIds.size() > 0 && horizontalPageIds.contains(pageIds.get(pageIds.size() - 1)));
			output.add("showPages", jsonList);
			updateTitle(firstPage != null ? firstPage : null);
		} else if (loginNotAuthorized) {
			onLogin = () -> show(pageIds, false);
			if (authentication != null) {
				authentication.showLogin();
			}
		} else {
			show(Application.getInstance().createDefaultPage());
		}
	}
	
	@Override
	public void login(Subject subject) {
		if (this.subject == null || !EqualsHelper.equalsById(this.subject, subject)) {
			this.subject = subject;
			Subject.setCurrent(subject);
			
			if (Application.getInstance().getAuthenticatonMode() != AuthenticatonMode.NOT_AVAILABLE) {
				output.add("canLogin", subject == null);
				output.add("canLogout", subject != null);
			}

			if (navigation != null) {
				unregister(navigation);
			}
			navigation = createNavigation();
		}
		updateNavigation();
		
		if (subject == null && /* initializing && */ Application.getInstance().getAuthenticatonMode().showLoginAtStart()) {
			Backend.getInstance().getAuthentication().showLogin();
		} else {
			onLogin();
		}
	}
	
	private void onLogin() {
		updateNavigation();
		if (onLogin != null) {
			try {
				onLogin.run();
			} finally {
				onLogin = null;
			}
		} else {
			Frontend.show(Application.getInstance().createDefaultPage());
		}
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
		json.put("className", page.getClass().getSimpleName());
		String route = Routing.getRouteSafe(page);
		if (route != null) {
			json.put("route", route);
			String navigationRoute = Routing.navigation(page);
			if (!route.equals(navigationRoute)) {
				json.put("navigationRoute", navigationRoute);
			}
		}

		JsonComponent content = (JsonComponent) PageAccess.getContent(page);
		json.put("content", content);

		List<Object> actionMenu = createActionMenu(page);
		json.put("actionMenu", actionMenu);

		json.put("minWidth", page.getMinWidth());
		json.put("maxWidth", page.getMaxWidth());
		
		Integer heightBasis = page.getHeightBasis();
		if (heightBasis != null) {
			json.put("heightBasis", heightBasis);
		}
		float heightGrow = page.getHeightGrow();
		if (heightGrow != 1) {
			json.put("heightGrow", heightGrow);
		}
		float heightShrink = page.getHeightShrink();
		if (heightShrink != 1) {
			json.put("heightShrink", heightShrink);
		}
		
		json.put("wheel", page instanceof WheelPage);

		return json;
	}

	@Override
	public void hideDetail(Page page) {
		if (isDetailShown(page)) {
			output.add("hidePage", visiblePageAndDetailsList.getId(page));
			visiblePageAndDetailsList.removeAllFrom(page);
		}
	}

	@Override
	public boolean isDetailShown(Page page) {
		return visiblePageAndDetailsList.contains(page);
	}

	@Override
	public void showDialog(Dialog dialog) {
		JsonDialog jsonDialog = new JsonDialog(dialog.getTitle(), dialog.getContent(), dialog.getSaveAction(), dialog.getCancelAction(), dialog.getActions());
		jsonDialog.put("className", dialog.getClass().getSimpleName());
		jsonDialog.put("height", dialog.getHeight());
		jsonDialog.put("width", dialog.getWidth());
		output.add("dialog", jsonDialog);
		visibleDialogs.put(dialog, jsonDialog);
	}
	

	public void showLogin(Dialog dialog) {
		Action[] actions;
		if (Application.getInstance().getAuthenticatonMode() != AuthenticatonMode.REQUIRED) {
			SkipLoginAction skipLoginAction = new SkipLoginAction();
			actions = new org.minimalj.frontend.action.Action[] {skipLoginAction, dialog.getSaveAction()};
		} else {
			actions = new org.minimalj.frontend.action.Action[] {dialog.getSaveAction()};
		}
		Page page = new Page() {
			@Override
			public IContent getContent() {
				return new JsonLoginContent(dialog.getContent(), dialog.getSaveAction(), actions);
			}
			
			@Override
			public String getTitle() {
				return dialog.getTitle();
			}
		};

		// don't use normal show(page) because login page should get part of history
		visiblePageAndDetailsList.clear();
		componentById.clear();
		// At login there should be no application actions but maybe some help links in the navigation
		register(navigation);
		output.add("showPage", createJson(page, null, null));
		updateTitle(page);
	}
	
	private class SkipLoginAction extends Action {
		
		@Override
		public void run() {
			Frontend.getInstance().login(null);
		}
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

	static List<Object> createActions(List<Action> actions) {
		List<Object> items = new ArrayList<>();
		for (Action action : actions) {
			items.add(createAction(action));
		}
		return items;
	}

	static List<Object> createActions(Action[] actions) {
		return createActions(Arrays.asList(actions));
	}

	static JsonComponent createAction(Action action) {
		JsonComponent item;
		if (action instanceof ActionGroup) {
			ActionGroup actionGroup = (ActionGroup) action;
			item = new JsonAction.JsonActionGroup();
			item.put("items", createActions(actionGroup.getItems()));
		} else if (action instanceof Separator){
			item = new JsonComponent("Separator");
		} else {
			item = new JsonAction(action);
		}
		item.put("name", action.getName());
		return item;
	}

	public void register(Object o) {
		travers(o, c -> {
			if (c instanceof JsonComponent) {
				JsonComponent component = (JsonComponent) c;
				String id = component.getId();
				if (id != null) {
					componentById.put(component.getId(), component);
				}
				component.setPropertyListener(propertyListener);
			}
		});
	}

	public void unregister(Object o) {
		travers(o, c -> {
			if (c instanceof JsonComponent) {
				JsonComponent component = (JsonComponent) c;
				componentById.remove(component.getId());
			}
		}); 
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void travers(Object o, Consumer<Object> c) {
		if (o instanceof JsonComponent) {
			c.accept((JsonComponent) o);
		}
		if (o instanceof Map) {
			((Map) o).values().forEach(v -> travers(v, c));
		}
		if (o instanceof Collection) {
			((Collection) o).forEach(v -> travers(v, c));
		}
		if (o instanceof JsonOutput) {
			((JsonOutput) o).forEach(v -> travers(v, c));
		}
		if (o != null && o.getClass().isArray() && !o.getClass().getComponentType().isPrimitive()) {
			Arrays.stream((Object[]) o).forEach(v -> travers(v, c));
		}
	}
	
	@Override
	public void closeDialog(Dialog dialog) {
		JsonDialog jsonDialog = visibleDialogs.get(dialog);
		if (jsonDialog != null) {
			closeDialog(jsonDialog);
		}
		visibleDialogs.remove(dialog);
	}

	public void closeDialog(JsonDialog jsonDialog) {
		unregister(jsonDialog);
		output.addElement("closeDialog", jsonDialog.getId());
	}

	public void replaceContent(JsonSwitch jsonSwitch, JsonComponent content) {
		boolean switchIsRegistred = componentById.containsKey(jsonSwitch.getId());
		if (switchIsRegistred) {
			if (JsonFrontend.getClientSession() != null) {
				replaceContent(output, jsonSwitch, content);
			} else {
				JsonOutput output = new JsonOutput();
				replaceContent(output, jsonSwitch, content);
				push.push(output.toString());
			}
		}
	}

	private void replaceContent(JsonOutput output, JsonSwitch jsonSwitch, JsonComponent content) {
		if (!jsonSwitch.isEmpty()) {
			jsonSwitch.values().forEach(this::unregister);
			output.removeContent(jsonSwitch.getId());
		}
		if (content != null) {
			output.addContent(jsonSwitch.getId(), content);
		}
	}

	public void addContent(String elementId, JsonComponent content) {
		output.addContent(elementId, content);
	}

	public void show(String url) {
		output.add("showUrl", url);
	}
	
	public void showNewTab(String url) {
		output.add("showUrlNewTab", url);
	}

	public void setRememberMeCookie(String rememberMeCookie) {
		output.add("rememberMeToken", rememberMeCookie != null ? rememberMeCookie : "");
	}
	
	private class JsonSessionPropertyChangeListener implements JsonPropertyListener {

		@Override
		public void propertyChange(JsonComponent component, String property, Object value) {
			if (JsonFrontend.getClientSession() != null) {
				output.propertyChange(component.getId(), property, value);
			} else if (component instanceof JsonText) {
				JsonOutput output = new JsonOutput();
				output.propertyChange(component.getId(), property, value);
				push.push(output.toString());
			}
		}
	}

	JsonSessionInfo getSessionInfo() {
		JsonSessionInfo info = new JsonSessionInfo();
		info.sessionId = sessionId;
		info.lastUsed = LocalDateTime.ofEpochSecond(lastUsed / 1000, 0, OffsetDateTime.now().getOffset());
		info.subject = subject != null ? subject.getName() : null;
		info.components = componentById.size();
		List<String> pageIds = visiblePageAndDetailsList.getPageIds();
		if (pageIds.size() > 0) {
			info.page = pageStore.get(pageIds.get(pageIds.size() - 1)).getTitle();
		}
		info.storedPages = pageStore.getSize();
		return info;
	}
}
