package org.minimalj.frontend.impl.json;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.impl.servlet.MjServlet;
import org.minimalj.security.Authorization;
import org.minimalj.util.LocaleContext;
import org.minimalj.util.resources.Resources;

public class JsonFrontend extends Frontend {

	private static String htmlTemplate;
	
	public static JsonClientSession getClientSession() {
		return (JsonClientSession) Frontend.getBrowser();
	}
	
	@Override
	public IComponent createLabel(String string) {
		JsonTextField component = new JsonTextField("Label");
		component.setValue(string);
		return component;
	}
	
	@Override
	public IComponent createLabel(Action action) {
		return new JsonAction(action);
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
		// TODO Auto-generated method stub
		return null;
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

	//
	
	static {
		htmlTemplate = readStream(MjServlet.class.getClassLoader().getResourceAsStream("index.html"));
	}
	
	public static String readStream(InputStream inputStream) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		return reader.lines().collect(Collectors.joining(System.getProperty("line.separator")));
	}

	public static String getHtmlTemplate() {
		return htmlTemplate;
	}
	
	public static String fillPlaceHolder(String html, Locale locale) {
		LocaleContext.setLocale(locale);
		String result = html.replace("$LOCALE", locale.getLanguage());
		result = result.replace("$AUTHORIZATION", Boolean.toString(Authorization.isAvailable()));
		result = result.replace("$FORCE_WSS", "false");
		result = result.replace("$PORT", "");
		result = result.replace("$WS", "ws");
		result = result.replace("$SEARCH", Resources.getString("SearchAction"));
		return result;
	}
}
