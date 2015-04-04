package org.minimalj.frontend.json;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.minimalj.application.ApplicationContext;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.toolkit.Action;
import org.minimalj.frontend.toolkit.CheckBox;
import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.frontend.toolkit.ComboBox;
import org.minimalj.frontend.toolkit.FlowField;
import org.minimalj.frontend.toolkit.FormContent;
import org.minimalj.frontend.toolkit.IDialog;
import org.minimalj.frontend.toolkit.TextField;

public class JsonClientToolkit extends ClientToolkit {

	
	@Override
	public IComponent createLabel(String string) {
		JsonComponent component = new JsonComponent("label");
		component.put("value", string);
		return component;
	}

	public static class JsonActionLabel extends JsonComponent {
		private final Action action;
		
		public JsonActionLabel(Action action) {
			super("actionLabel");
			put(VALUE, action.getName());
			this.action = action;
		}
	}
	
	@Override
	public IComponent createLabel(Action action) {
		return new JsonActionLabel(action);
	}

	@Override
	public IComponent createTitle(String string) {
		JsonComponent component = new JsonComponent("title");
		component.put("value", string);
		return component;
	}

	@Override
	public TextField createReadOnlyTextField() {
		return new JsonTextField("readOnlyTextField");
	}

	@Override
	public TextField createTextField(int maxLength, String allowedCharacters, InputType inputType, Search<String> autocomplete,
			InputComponentListener changeListener) {
		return new JsonTextField("textField", maxLength, allowedCharacters, inputType, autocomplete, changeListener);
	}

	@Override
	public TextField createAreaField(int maxLength, String allowedCharacters, InputComponentListener changeListener) {
		return new JsonTextField("AreaField", maxLength, allowedCharacters, null, null, changeListener);
	}

	@Override
	public FlowField createFlowField() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> ComboBox<T> createComboBox(List<T> objects, InputComponentListener changeListener) {
		return new JsonCombobox<T>(objects, changeListener);
	}

	@Override
	public CheckBox createCheckBox(InputComponentListener changeListener, String text) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> ITable<T> createTable(Object[] keys, TableActionListener<T> listener) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void show(Page page) {
		JsonComponent content = (JsonComponent) page.getContent();
		System.out.println(content.toString());

		// 1. page.getContent() should provide content of page
		// -> send it to browser togehter with ObjectActions etc
		// serialize the page and store it
	}

	@Override
	public void show(List<Page> pages, int startIndex) {
		// TODO Auto-generated method stub
	}

	@Override
	public void refresh() {
		// TODO Auto-generated method stub
	}

	@Override
	public <T> ILookup<T> createLookup(InputComponentListener changeListener, Search<T> index, Object[] keys) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IComponent createComponentGroup(IComponent... components) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FormContent createFormContent(int columns, int columnWidth) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SwitchContent createSwitchContent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ApplicationContext getApplicationContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IDialog createDialog(String title, IContent content, Action... actions) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> IDialog createSearchDialog(Search<T> index, Object[] keys, TableActionListener<T> listener) {
		// TODO Auto-generated method stub
		return null;
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
