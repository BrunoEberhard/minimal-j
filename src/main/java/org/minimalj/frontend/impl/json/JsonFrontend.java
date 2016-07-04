package org.minimalj.frontend.impl.json;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.minimalj.application.Application;
import org.minimalj.backend.Backend;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.page.PageManager;
import org.minimalj.model.Rendering;
import org.minimalj.util.LocaleContext;
import org.minimalj.util.resources.Resources;

public class JsonFrontend extends Frontend {

	private static boolean useWebSocket = Boolean.valueOf(System.getProperty("MjUseWebSocket", "false"));
	
	private static ThreadLocal<JsonPageManager> sessionByThread = new ThreadLocal<>();

	public static void setSession(JsonPageManager session) {
		sessionByThread.set(session);
	}

	public static JsonPageManager getClientSession() {
		return sessionByThread.get();
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
		return new JsonTextField("ReadOnlyTextField");
	}

	@Override
	public Input<String> createTextField(int maxLength, String allowedCharacters, InputType inputType, Search<String> suggestionSearch,
			InputComponentListener changeListener) {
		return new JsonTextField("TextField", maxLength, allowedCharacters, inputType, suggestionSearch, changeListener);
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
	public Input<byte[]> createImage(Size size, InputComponentListener changeListener) {
		throw new RuntimeException("Image not yet implemented in JsonFrontend");
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
	public <T> ITable<T> createTable(Object[] keys, TableActionListener<T> listener) {
		return new JsonTable<T>(keys, listener);
	}

	@Override
	public <T> Input<T> createLookup(InputComponentListener changeListener, Search<T> index, Object[] keys) {
		return new JsonLookup<T>(changeListener, index, keys);
	}

	@Override
	public IComponent createComponentGroup(IComponent... components) {
		JsonComponent group = new JsonComponent("Group", true);
		group.put("components", Arrays.asList(components));
		return group;
	}

	@Override
	public FormContent createFormContent(int columns, int columnWidth) {
		return new JsonFormContent(columns, columnWidth);
	}

	@Override
	public SwitchContent createSwitchContent() {
		return new JsonSwitchContent();
	}

	@Override
	public IContent createHtmlContent(String htmlOrUrl) {
		return new JsonHtmlContent(htmlOrUrl);
	}
	
	//
	
	
	public static boolean useWebSocket() {
		return useWebSocket;
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
		return readStream(Resources.getInputStream("index.html"));
	}
	
	public static String fillPlaceHolder(String html, Locale locale) {
		LocaleContext.setCurrent(locale);
		String result = html.replace("$LOCALE", locale.getLanguage());
		result = result.replace("$AUTHORIZATION", Boolean.toString(Backend.isAuthorizationActive()));
		result = result.replace("$WEB_SOCKET", Boolean.toString(useWebSocket()));
		result = result.replace("$PORT", "");
		result = result.replace("$WS", "ws");
		result = result.replace("$DISABLED_SEARCH", Application.getInstance().hasSearchPages() ? "" : "disabled");
		result = result.replace("$SEARCH", Resources.getString("SearchAction"));
		return result;
	}

}
