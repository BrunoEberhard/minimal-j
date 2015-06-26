package org.minimalj.frontend.lanterna.toolkit;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.minimalj.application.ApplicationContext;
import org.minimalj.frontend.lanterna.LanternaGUIScreen;
import org.minimalj.frontend.lanterna.component.LanternaForm;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.toolkit.Action;
import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.frontend.toolkit.FormContent;
import org.minimalj.frontend.toolkit.IDialog;
import org.minimalj.frontend.toolkit.IList;

import com.googlecode.lanterna.gui.component.Button;
import com.googlecode.lanterna.gui.dialog.MessageBox;

public class LanternaClientToolkit extends ClientToolkit {
	private static final ThreadLocal<LanternaGUIScreen> gui = new ThreadLocal<>();
	
	public LanternaClientToolkit() {
	}
	
	public static void setGui(LanternaGUIScreen value) {
		if (value == null && gui.get() != null) {
			gui.get().actionComplete();
		}
		gui.set(value);
	}
	
	public static LanternaGUIScreen getGui() {
		return gui.get();
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
	public IDialog showDialog(String title, IContent content, Action closeAction, Action saveAction, Action... actions) {
		// TODO use saveAction (Enter in TextFields should save the dialog)
		return new LanternaDialog(getGui(), content, title, closeAction, actions);
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
		private final LanternaGUIScreen guiScreen;
		private final Action action;
		
		public LanternaActionAdapter(Action action) {
			this.action = action;
			this.guiScreen = getGui();
		}
		
		public void doAction() {
			setGui(guiScreen);
			action.action();
			setGui(null);
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
	public Input<String> createTextField(int maxLength, String allowedCharacters, InputType inputType, Search<String> autocomplete,
			InputComponentListener changeListener) {
		return new LanternaTextField(changeListener);
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
	public OutputStream store(String buttonText) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InputStream load(String buttonText) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void showConfirmDialog(String message,
			String title, ConfirmDialogType type, DialogListener listener) {
		// TODO Auto-generated method stub
	}

	@Override
	public void showError(String text) {
		MessageBox.showMessageBox(getGui(), "Error", text);
	}

	@Override
	public void showMessage(String text) {
		MessageBox.showMessageBox(getGui(), "Message", text);

	}

	@Override
	public <T> IDialog showSearchDialog(Search<T> index, Object[] keys, TableActionListener<T> listener) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Input<T> createLookup(InputComponentListener changeListener, Search<T> index, Object[] keys) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void show(Page page) {
		getGui().show(page);
	}

	@Override
	public void refresh() {
		throw new RuntimeException("refresh on lanterna not yet implemented");
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

}
