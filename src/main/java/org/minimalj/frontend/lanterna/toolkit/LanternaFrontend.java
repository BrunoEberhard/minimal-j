package org.minimalj.frontend.lanterna.toolkit;

import java.util.List;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.lanterna.LanternaGUIScreen;
import org.minimalj.frontend.lanterna.component.LanternaForm;
import org.minimalj.frontend.page.PageBrowser;

import com.googlecode.lanterna.gui.component.Button;

public class LanternaFrontend extends Frontend {
	
	public LanternaFrontend() {
	}
	
	public static void setGui(LanternaGUIScreen value) {
		if (value == null && Frontend.getBrowser() != null) {
			((LanternaGUIScreen) Frontend.getBrowser()).actionComplete();
		}
		Frontend.setBrowser(value);
	}
	
	@Override
	public Input<Boolean> createCheckBox(InputComponentListener changeListener, String text) {
		return new LanternaCheckBox(changeListener, text);
	}

	@Override
	public <T> Input<T> createComboBox(List<T> objects, InputComponentListener changeListener) {
		return new LanternaComboBox<T>(objects, changeListener);
	}

	@Override
	public IList createList(Action... actions) {
		return new LanternaList(actions);
	}

	@Override
	public FormContent createFormContent(int columns, int columnWidth) {
		return new LanternaForm(columns);
	}

	@Override
	public IComponent createComponentGroup(IComponent... components) {
		return new LanternaHorizontalLayout(components);
	}

	@Override
	public IComponent createLabel(final Action action) {
		LanternaActionAdapter lanternaAction = new LanternaActionAdapter(action);
		LanternaActionLabel button = new LanternaActionLabel(action.getName(), lanternaAction);
		return button;
	}

	public static class LanternaActionLabel extends Button implements IComponent {
		public LanternaActionLabel(String name, com.googlecode.lanterna.gui.Action action) {
			super(name, action);
		}
	}

	/*
	 * Cannot be done as inner class because lanterna action has to be provided
	 * to the button constructor. And the minimal-j action needs the component for
	 * the action method.
	 */
	private static class LanternaActionAdapter implements com.googlecode.lanterna.gui.Action {
		private final PageBrowser browser;
		private final Action action;
		
		public LanternaActionAdapter(Action action) {
			this.action = action;
			this.browser = Frontend.getBrowser();
		}
		
		@Override
		public void doAction() {
			Frontend.setBrowser(browser);
			action.action();
			Frontend.setBrowser(null);
		}
	}
	
	@Override
	public IComponent createLabel(String text) {
		return new LanternaLabel(text);
	}

	@Override
	public Input<String> createReadOnlyTextField() {
		return new LanternaReadOnlyTextField();
	}

	@Override
	public SwitchContent createSwitchContent() {
		return new LanternaSwitchContent();
	}

	@Override
	public <T> ITable<T> createTable(Object[] keys, TableActionListener<T> listener) {
		return new LanternaTable<T>(keys, listener);
	}

	@Override
	public Input<String> createTextField(int maxLength, String allowedCharacters, InputType inputType, List<String> choice,
			InputComponentListener changeListener) {
		return new LanternaTextField(changeListener);
	}
	
	@Override
	public PasswordField createPasswordField(InputComponentListener changeListener, int maxLength) {
		return new LanternaPasswordField(changeListener);
	}

	@Override
	public Input<String> createAreaField(int maxLength, String allowedCharacters, InputComponentListener changeListener) {
		return new LanternaTextField(changeListener);
	}

	@Override
	public IComponent createTitle(String text) {
		return new LanternaLabel(text);
	}

	@Override
	public <T> Input<T> createLookup(InputComponentListener changeListener, Search<T> index, Object[] keys) {
		// TODO Auto-generated method stub
		return null;
	}

}
