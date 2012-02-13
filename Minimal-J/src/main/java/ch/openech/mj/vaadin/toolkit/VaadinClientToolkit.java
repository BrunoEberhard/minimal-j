package ch.openech.mj.vaadin.toolkit;

import javax.swing.Action;

import ch.openech.mj.application.WindowConfig;
import ch.openech.mj.page.PageContext;
import ch.openech.mj.toolkit.CheckBox;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.ComboBox;
import ch.openech.mj.toolkit.ContextLayout;
import ch.openech.mj.toolkit.GridFormLayout;
import ch.openech.mj.toolkit.HorizontalLayout;
import ch.openech.mj.toolkit.MultiLineTextField;
import ch.openech.mj.toolkit.SwitchLayout;
import ch.openech.mj.toolkit.TextField;
import ch.openech.mj.toolkit.VisibilityLayout;
import ch.openech.mj.toolkit.VisualDialog;
import ch.openech.mj.toolkit.VisualList;
import ch.openech.mj.toolkit.VisualTable;
import ch.openech.mj.toolkit.TextField.TextFieldFilter;
import ch.openech.mj.vaadin.VaadinWindow;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;

public class VaadinClientToolkit extends ClientToolkit {

	@Override
	public Object createEmptyComponent() {
		return new Panel();
	}
	
	@Override
	public Object createLabel(String string) {
		return new Label(string);
	}

	@Override
	public Object createTitle(String string) {
		return new Label(string);
	}

	@Override
	public TextField createTextField() {
		return new VaadinTextField();
	}

	@Override
	public TextField createTextField(int maxLength) {
		return new VaadinTextField(maxLength);
	}
	
	@Override
	public TextField createTextField(TextFieldFilter filter) {
		return new VaadinTextField(filter);
	}

	@Override
	public MultiLineTextField createMultiLineTextField() {
		return new VaadinMultiLineTextField();
	}

	@Override
	public ComboBox createComboBox() {
		return new VaadinComboBox();
	}

	@Override
	public VisualList createVisualList() {
		return new VaadinVisualList();
	}

	@Override
	public CheckBox createCheckBox(String text) {
		return new VaadinCheckBox(text);
	}

	@Override
	public HorizontalLayout createHorizontalLayout(Object... components) {
		return new VaadinHorizontalLayout(components);
	}

	@Override
	public ContextLayout createContextLayout(Object content) {
		return new VaadinContextLayout(content);
	}

	@Override
	public VisibilityLayout createVisibilityLayout(Object content) {
		return new VaadinVisibilityLayout(content);
	}

	@Override
	public GridFormLayout createGridLayout(int columns, int defaultSpan) {
		return new VaadinGridFormLayout(columns, defaultSpan);
	}

	@Override
	public SwitchLayout createSwitchLayout() {
		return new VaadinSwitchLayout();
	}

	@Override
	public void showNotification(Object c, String text) {
		Component component = (Component) c;
		Window window = component.getWindow();
		window.showNotification("Hinweis", text, Notification.TYPE_HUMANIZED_MESSAGE);
	}

	@Override
	public void focusFirstComponent(Object component) {
		// TODO Auto-generated method stub
	}

	@Override
	public void showMessage(Object c, String text) {
		// TODO Vaadin zeigt Notifikationen statt Informationsdialog
		Component component = (Component) c;
		Window window = component.getWindow();
		window.showNotification("Information", text, Notification.TYPE_HUMANIZED_MESSAGE);
	}
	
	@Override
	public void showError(Object c, String text) {
		// TODO Vaadin zeigt Notifikationen statt Informationsdialog
		Component component = (Component) c;
		Window window = component.getWindow();
		window.showNotification("Fehler", text, Notification.TYPE_ERROR_MESSAGE);
	}

	@Override
	public int showConfirmDialog(Object component, Object message, String title, int optionType) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <T> VisualTable<T> createVisualTable(Class<T> clazz, Object[] fields) {
		return new VaadinVisualTable<T>(clazz, fields);
	}

	@Override
	public VisualDialog openDialog(Object c, Object content, String title) {
		Component component = (Component) c;
		Window window = component.getWindow();
		return new VaadinDialog(window, (ComponentContainer) content, title);
	}

	@Override
	public Object createEditorLayout(String information, Object content, Action[] actions) {
		return new VaadinEditorLayout(information, (ComponentContainer) content, actions);
	}

	@Override
	public PageContext openPageContext(PageContext parentPageContext, WindowConfig windowConfig) {
		VaadinWindow parentVaadinWindow = (VaadinWindow) parentPageContext;
		VaadinWindow vaadinWindow = new VaadinWindow(parentVaadinWindow);
		parentVaadinWindow.open(new ExternalResource(vaadinWindow.getURL()), "_new");
		return vaadinWindow;
	}

	@Override
	public PageContext openPageContext(PageContext parentPageContext) {
		return null;
	}

	@Override
	public Object createFormAlignLayout(Object content) {
		GridLayout gridLayout = new GridLayout(3, 3);
		gridLayout.addComponent((Component) content, 1, 1);
		gridLayout.setRowExpandRatio(0, 0.1f);
		gridLayout.setRowExpandRatio(1, 0.7f);
		gridLayout.setRowExpandRatio(2, 0.2f);
		gridLayout.setColumnExpandRatio(0, 0.1f);
		gridLayout.setColumnExpandRatio(1, 0.7f);
		gridLayout.setColumnExpandRatio(2, 0.2f);
		return gridLayout;
	}
}
