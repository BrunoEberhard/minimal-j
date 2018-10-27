package org.minimalj.frontend.impl.javafx;

import java.util.List;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.impl.javafx.toolkit.FxCheckbox;
import org.minimalj.frontend.impl.javafx.toolkit.FxComboBox;
import org.minimalj.frontend.impl.javafx.toolkit.FxText;
import org.minimalj.frontend.impl.javafx.toolkit.FxTextField;
import org.minimalj.frontend.page.PageManager;
import org.minimalj.model.Rendering;

public class FxFrontend extends Frontend {

	@Override
	public IComponent createText(String string) {
		return new FxText(string);
	}

	@Override
	public IComponent createText(Action action) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IComponent createText(Rendering rendering) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IComponent createTitle(String string) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Input<String> createReadOnlyTextField() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Input<String> createTextField(int maxLength, String allowedCharacters, Search<String> suggestionSearch, InputComponentListener changeListener) {
		return new FxTextField(maxLength, allowedCharacters, suggestionSearch, changeListener);
	}

	@Override
	public Input<String> createAreaField(int maxLength, String allowedCharacters, InputComponentListener changeListener) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PasswordField createPasswordField(InputComponentListener changeListener, int maxLength) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IList createList(Action... actions) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Input<T> createComboBox(List<T> objects, InputComponentListener changeListener) {
		return new FxComboBox(objects, changeListener);
	}

	@Override
	public Input<Boolean> createCheckBox(InputComponentListener changeListener, String text) {
		return new FxCheckbox(text, changeListener);
	}

	@Override
	public Input<byte[]> createImage(int size, InputComponentListener changeListener) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SwitchComponent createSwitchComponent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Input<String> createLookup(Input<String> stringInput, Runnable lookup) {
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
	public <T> ITable<T> createTable(Object[] keys, boolean multiSelect, TableActionListener<T> listener) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IContent createHtmlContent(String htmlOrUrl) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IContent createQueryContent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PageManager getPageManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> IContent createTable(Search<T> search, Object[] keys, boolean multiSelect, TableActionListener<T> listener) {
		// TODO Auto-generated method stub
		return null;
	}

}
