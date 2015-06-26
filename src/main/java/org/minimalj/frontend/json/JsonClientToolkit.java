package org.minimalj.frontend.json;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import org.minimalj.application.ApplicationContext;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.toolkit.Action;
import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.frontend.toolkit.FormContent;
import org.minimalj.frontend.toolkit.IDialog;
import org.minimalj.frontend.toolkit.IList;

public class JsonClientToolkit extends ClientToolkit {

	private static final ThreadLocal<JsonClientSession> session = new ThreadLocal<>();
	
	static void setSession(JsonClientSession session) {
		JsonClientToolkit.session.set(session);
	}
	
	public static JsonClientSession getSession() {
		return session.get();
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
	public Input<String> createTextField(int maxLength, String allowedCharacters, InputType inputType, Search<String> autocomplete,
			InputComponentListener changeListener) {
		return new JsonTextField("TextField", maxLength, allowedCharacters, inputType, autocomplete, changeListener);
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
	public void show(Page page) {
		session.get().showPage(page);
	}

	@Override
	public void refresh() {
		session.get().refresh();
	}

	@Override
	public <T> Input<T> createLookup(InputComponentListener changeListener, Search<T> index, Object[] keys) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IComponent createComponentGroup(IComponent... components) {
		JsonComponent group = new JsonComponent("Group", false);
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
	public ApplicationContext getApplicationContext() {
		// TODO Auto-generated method stub
		return new ApplicationContext() {
			
			@Override
			public void setUser(String user) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void savePreferences(Object preferences) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void loadPreferences(Object preferences) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public String getUser() {
				// TODO Auto-generated method stub
				return null;
			}
		};
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
}
