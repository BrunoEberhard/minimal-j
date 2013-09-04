package ch.openech.mj.lanterna.toolkit;

import java.io.InputStream;

import ch.openech.mj.lanterna.component.LanternaForm;
import ch.openech.mj.toolkit.Caption;
import ch.openech.mj.toolkit.CheckBox;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.ComboBox;
import ch.openech.mj.toolkit.ConfirmDialogListener;
import ch.openech.mj.toolkit.ExportHandler;
import ch.openech.mj.toolkit.FlowField;
import ch.openech.mj.toolkit.GridFormLayout;
import ch.openech.mj.toolkit.HorizontalLayout;
import ch.openech.mj.toolkit.IAction;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.IDialog;
import ch.openech.mj.toolkit.ITable;
import ch.openech.mj.toolkit.SwitchLayout;
import ch.openech.mj.toolkit.TextField;

import com.googlecode.lanterna.gui.Component;
import com.googlecode.lanterna.gui.GUIScreen;
import com.googlecode.lanterna.gui.dialog.MessageBox;

public class LanternaClientToolkit extends ClientToolkit {
	private final GUIScreen gui;
	
	public LanternaClientToolkit(GUIScreen gui) {
		this.gui = gui;
	}
	
	@Override
	public CheckBox createCheckBox(InputComponentListener changeListener, String text) {
		return new LanternaCheckBox(changeListener, text);
	}

	@Override
	public <T> ComboBox<T> createComboBox(InputComponentListener changeListener) {
		return new LanternaComboBox<T>(changeListener);
	}

	@Override
	public IDialog createDialog(IComponent parent, String title, IComponent content, IAction... actions) {
		return new LanternaDialog(gui, content, title, actions);
	}

	@Override
	public FlowField createFlowField() {
		return new LanternaFlowField();
	}

	@Override
	public IComponent createFormAlignLayout(IComponent content) {
		return content;
	}

	@Override
	public GridFormLayout createGridLayout(int columns, int columnWidth) {
		return new LanternaForm(columns);
	}

	@Override
	public HorizontalLayout createHorizontalLayout(IComponent... components) {
		return new LanternaHorizontalLayout(components);
	}

	@Override
	public IComponent createLabel(IAction action) {
		TextField textField = createReadOnlyTextField();
		textField.setText("TODO: Action " + action.getName());
		return textField;
	}

	@Override
	public IComponent createLabel(String text) {
		return new LanternaLabel(text);
	}

	@Override
	public IComponent createLink(String text, String address) {
		TextField textField = createReadOnlyTextField();
		textField.setText("TODO: Link " + text);
		return textField;
	}

	@Override
	public TextField createReadOnlyTextField() {
		return new LanternaReadOnlyTextField();
	}

	@Override
	public SwitchLayout createSwitchLayout() {
		return new LanternaSwitchLayout();
	}

	@Override
	public <T> ITable<T> createTable(Class<T> clazz, Object[] fields) {
		return new LanternaTable<T>(clazz, fields);
	}

	@Override
	public TextField createTextField(InputComponentListener changeListener, int maxLength) {
		return new LanternaTextField(changeListener);
	}

	@Override
	public TextField createTextField(InputComponentListener changeListener, int maxLength, String allowedCharacters) {
		return new LanternaTextField(changeListener);
	}

	@Override
	public IComponent createTitle(String text) {
		return new LanternaLabel(text);
	}

	@Override
	public Caption decorateWithCaption(IComponent component, String caption) {
		return new LanternaCaption((Component) component, caption);
	}

	@Override
	public void export(IComponent parent, String buttonText,
			ExportHandler exportHandler) {
		// TODO Auto-generated method stub

	}

	@Override
	public InputStream imprt(IComponent parent, String buttonText) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void showConfirmDialog(IComponent component, String message,
			String title, int type, ConfirmDialogListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void showError(Object parent, String text) {
		MessageBox.showMessageBox(gui, "Error", text);
	}

	@Override
	public void showMessage(Object parent, String text) {
		MessageBox.showMessageBox(gui, "Message", text);

	}

}
