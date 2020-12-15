package org.minimalj.frontend.impl.json;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.minimalj.application.Application;
import org.minimalj.application.Configuration;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.action.ActionGroup;
import org.minimalj.frontend.impl.web.WebApplication;
import org.minimalj.frontend.impl.web.WebServer;
import org.minimalj.model.Rendering;
import org.minimalj.util.StringUtils;
import org.minimalj.util.resources.Resources;

public class JsonFrontend extends Frontend {
	private static ThreadLocal<JsonPageManager> sessionByThread = new ThreadLocal<>();
	private static ThreadLocal<Boolean> useInputTypesByThread = new ThreadLocal<>();

	public static void setSession(JsonPageManager session) {
		sessionByThread.set(session);
	}

	public static JsonPageManager getClientSession() {
		return sessionByThread.get();
	}
	
	public static void setUseInputTypes(boolean compact){
		useInputTypesByThread.set(compact);
	}
	
	@Override
	public JsonPageManager getPageManager() {
		return getClientSession();
	}
	
	@Override
	public IComponent createText(String string) {
		return new JsonText(string);
	}
	
	@Override
	public IComponent createText(Action action) {
		return new JsonAction(action);
	}
	
	@Override
	public IComponent createText(Rendering rendering) {
		return new JsonText(rendering);
	}

	@Override
	public IComponent createTitle(String string) {
		JsonComponent component = new JsonComponent("Title");
		component.put(JsonInputComponent.VALUE, string);
		return component;
	}

	@Override
	public Input<String> createReadOnlyTextField() {
		return new JsonText((String) null);
	}

	@Override
	public Input<String> createTextField(int maxLength, String allowedCharacters, Search<String> suggestionSearch, InputComponentListener changeListener) {
		return new JsonTextField("TextField", maxLength, allowedCharacters, null, suggestionSearch, changeListener);
	}
	
	@Override
	public Optional<Input<String>> createInput(int maxLength, InputType inputType, InputComponentListener changeListener) {
		if (useInputTypesByThread.get()) {
			return Optional.of(new JsonTextField("TextField", maxLength, null, inputType, null, changeListener));
		} else {
			return Optional.empty();
		}
	}

	@Override
	public PasswordField createPasswordField(InputComponentListener changeListener, int maxLength) {
		return new JsonPasswordField(maxLength, changeListener);
	}
	
	@Override
	public Input<String> createAreaField(int maxLength, String allowedCharacters, InputComponentListener changeListener) {
		return new JsonTextField("AreaField", maxLength, allowedCharacters, null, null, changeListener);
	}

	@Override
	public Input<byte[]> createImage(InputComponentListener changeListener) {
		return new JsonImage(changeListener);
	};

	@Override
	public <T> Input<T> createComboBox(List<T> objects, InputComponentListener changeListener) {
		return new JsonCombobox<>(objects, changeListener);
	}

	@Override
	public Input<Boolean> createCheckBox(InputComponentListener changeListener, String text) {
		return new JsonCheckBox(text, changeListener);
	}

	@Override
	public <T> ITable<T> createTable(Object[] keys, boolean multiSelect, TableActionListener<T> listener) {
		return new JsonTable<>(keys, multiSelect, listener);
	}

	@Override
	public <T> IContent createTable(Search<T> search, Object[] keys, boolean multiSelect, TableActionListener<T> listener) {
		return new JsonSearchTable<>(search, keys, multiSelect, listener);
	}

	@Override
	public IContent createFormTableContent(FormContent form, ITable<?> table) {
		((JsonTable<?>) table).put("overview", form);
		return table;
	}

	@Override
	public Input<String> createLookup(Input<String> input, Runnable lookup) {
		return new JsonLookup(input, lookup);
	}

	@Override
	public Input<String> createLookup(Input<String> input, ActionGroup actions) {
		return new JsonLookupActions(input, actions);
	}

	@Override
	public IComponent createVerticalGroup(IComponent... components) {
		return createComponentGroup("groupVertical", components);
	}

	@Override
	public IComponent createHorizontalGroup(IComponent... components) {
		return createComponentGroup("groupHorizontal", components);
	}

	private IComponent createComponentGroup(String type, IComponent... components) {
		JsonComponent group = new JsonComponent(type);
		if (components.length > 0) {
			// click on the caption label should focus first component, not the group
			group.put("firstId", ((JsonComponent) components[0]).get("id"));
		}
		group.put("components", Arrays.asList(components));
		return group;
	}

	@Override
	public FormContent createFormContent(int columns, int columnWidth) {
		return new JsonFormContent(columns, columnWidth);
	}

	@Override
	public SwitchContent createSwitchContent() {
		return new JsonSwitch(getClientSession());
	}
	
	@Override
	public SwitchComponent createSwitchComponent() {
		return new JsonSwitch(getClientSession());
	}

	@Override
	public IContent createHtmlContent(String html) {
		return new JsonHtmlContent(html);
	}

	@Override
	public IContent createHtmlContent(URL url) {
		return new JsonHtmlContent(url);
	}

	@Override
	public void showBrowser(String url) {
		getClientSession().show(url);
	}

	@Override
	public IContent createQueryContent() {
		String caption = Resources.getString("Application.queryCaption", Resources.OPTIONAL);
		return new JsonQueryContent(caption);
	}
	
	//
	
	public static String readStream(InputStream inputStream) {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
			return reader.lines().collect(Collectors.joining(System.getProperty("line.separator")));
		} catch (IOException x) {
			throw new RuntimeException(x);
		}
	}

	public static String getHtmlTemplate() {
		return readStream(WebApplication.class.getResourceAsStream("index.html"));
	}
	
	private static String stylesheets;
	
	static {
		List<String> cssHrefs = new ArrayList<>();
		if (StringUtils.equals(Configuration.get("MjTheme", ""), "material")) {
			cssHrefs.add("https://fonts.googleapis.com/css?family=Roboto");
			cssHrefs.add("material.css");
		}
		String[] customCss = Configuration.get("MjCss", "").split(",");
		cssHrefs.addAll(Arrays.asList(customCss));
		stylesheets = "";
		cssHrefs.forEach(css -> {
			stylesheets += "<link rel=\"stylesheet\" href=\"" + css + "\" />\n";
		});
	}
	
	public static String fillPlaceHolder(String html, String path) {
		String result = html.replace("$PORT", "");
		result = result.replace("$WS", "ws");
		result = result.replace("$MINIMALJ-VERSION", "Minimal-J Version: " + Application.class.getPackage().getImplementationVersion());
		result = result.replace("$APPLICATION-VERSION", "Application Version: " + Application.getInstance().getClass().getPackage().getImplementationVersion());
		result = result.replace("$TITLE", Application.getInstance().getName());
		result = result.replace("$META", getMeta());
		result = result.replace("$ICON", getIconLink());
		result = result.replace("$PATH", path);
		result = result.replace("$THEME", stylesheets);
		result = result.replace("$IMPORT", "");
		result = result.replace("$INIT", "");
		result = result.replace("$NOSCRIPT", Resources.getString("html.noscript"));
		result = result.replace("$SEND", WebServer.useWebSocket ? "sendWebSocket" : "sendAjax");
		return result;
	}

	private static String getIconLink() {
		if (Application.getInstance().getIcon() != null) {
			return "<link rel=\"icon\" href=\"application.png\" type=\"image/png\">";
		} else {
			return "";
		}
	}

	public static Map<String, String> getMetas() {
		Map<String, String> metas = new HashMap<>();
		if (Resources.isAvailable("Application.description")) {
			metas.put("description", Resources.getString("Application.description"));
		}
		if (Resources.isAvailable("Application.keywords")) {
			metas.put("keywords", Resources.getString("Application.keywords"));
		}
		if (Resources.isAvailable("Application.google-site-verification")) {
			metas.put("google-site-verification", Resources.getString("Application.google-site-verification"));
		}
		return metas;
	}

	private static String getMeta() {
		StringBuilder s = new StringBuilder(400);
		for (Map.Entry<String, String> entry : getMetas().entrySet()) {
			s.append("<meta name=\"").append(entry.getKey()).append("\" content=\"").append(entry.getValue()).append("\">");
		}
		return s.toString();
	}
}
