package org.minimalj.frontend.impl.json;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.minimalj.application.Application;
import org.minimalj.application.Configuration;
import org.minimalj.backend.Backend;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.page.PageManager;
import org.minimalj.model.Rendering;
import org.minimalj.util.LocaleContext;
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
	public PageManager getPageManager() {
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
	public IList createList(Action... actions) {
		return new JsonList(actions);
	}

	@Override
	public <T> Input<T> createComboBox(List<T> objects, InputComponentListener changeListener) {
		return new JsonCombobox<T>(objects, changeListener);
	}

	@Override
	public Input<Boolean> createCheckBox(InputComponentListener changeListener, String text) {
		return new JsonCheckBox(text, changeListener);
	}

	@Override
	public <T> ITable<T> createTable(Object[] keys, boolean multiSelect, TableActionListener<T> listener) {
		return new JsonTable<T>(keys, multiSelect, listener);
	}

	@Override
	public <T> IContent createTable(Search<T> search, Object[] keys, boolean multiSelect, TableActionListener<T> listener) {
		return new JsonSearchTable<T>(search, keys, multiSelect, listener);
	}

	@Override
	public IContent createFormTableContent(FormContent form, ITable<?> table) {
		((JsonTable<?>) table).put("overview", form);
		return table;
	}

	@Override
	public Input<String> createLookup(Input<String> stringInput, Runnable lookup) {
		return new JsonLookup(stringInput, lookup);
	}

	@Override
	public IComponent createComponentGroup(IComponent... components) {
		JsonComponent group = new JsonComponent("Group");
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
		return new JsonSwitch();
	}
	
	@Override
	public SwitchComponent createSwitchComponent() {
		return new JsonSwitch();
	}

	@Override
	public IContent createHtmlContent(String htmlOrUrl) {
		return new JsonHtmlContent(htmlOrUrl);
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
		return readStream(JsonFrontend.class.getResourceAsStream("/index.html"));
	}
	
	static final Map<String, String> THEMES = new HashMap<>();
	
	static {
		THEMES.put("", "");
		THEMES.put("material", "<link rel=\"stylesheet\" href=\"https://fonts.googleapis.com/css?family=Roboto\" /><link rel=\"stylesheet\" type=\"text/css\" href=\"miniterial.css\"/>");
		/*
		<link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Roboto" />
		<link rel="stylesheet" type="text/css" href="miniterial.css"/>
		*/
	}
	
	public static String fillPlaceHolder(String html, Locale locale, String path) {
		LocaleContext.setCurrent(locale);
		String result = html.replace("$LOCALE", locale.getLanguage());
		result = result.replace("$LOGIN", Boolean.toString(Backend.getInstance().isAuthenticationActive()));
		result = result.replace("$PORT", "");
		result = result.replace("$WS", "ws");
		result = result.replace("$DISABLED_SEARCH", Application.getInstance().hasSearchPages() ? "" : "disabled");
		result = result.replace("$SEARCH", Resources.getString("SearchAction"));
		result = result.replace("$MINIMALJ-VERSION", "Minimal-J Version: " + Application.class.getPackage().getImplementationVersion());
		result = result.replace("$APPLICATION-VERSION", "Application Version: " + Application.getInstance().getClass().getPackage().getImplementationVersion());
		result = result.replace("$ICON", getIconLink());
		result = result.replace("$BASE", base(path));
		result = result.replace("$PATH", path);
		result = result.replace("$THEME", THEMES.get(Configuration.get("MjTheme", "")));
		result = result.replace("$IMPORT", "");
		result = result.replace("$INIT", "");
		return result;
	}
	
    private static String base(String path) {
    	String base = "./";
    	int level = path.split("/").length - 1;
    	for (int i = 0; i<level; i++) {
    		base = base + "../";
    	}
    	return base;
    }
	
	private static String getIconLink() {
		if (Application.getInstance().getIcon() != null) {
			return "<link rel=\"icon\" href=\"application.png\" type=\"image/png\">";
		} else {
			return "";
		}
	}

}
