package ch.openech.mj.vaadin.toolkit;


import javax.swing.Action;
import javax.swing.event.ChangeListener;

import ch.openech.mj.page.PageContext;
import ch.openech.mj.toolkit.CheckBox;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.ComboBox;
import ch.openech.mj.toolkit.ConfirmDialogListener;
import ch.openech.mj.toolkit.FlowField;
import ch.openech.mj.toolkit.GridFormLayout;
import ch.openech.mj.toolkit.HorizontalLayout;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.IComponentDelegate;
import ch.openech.mj.toolkit.SwitchLayout;
import ch.openech.mj.toolkit.TextField;
import ch.openech.mj.toolkit.TextField.TextFieldFilter;
import ch.openech.mj.toolkit.VisualDialog;
import ch.openech.mj.toolkit.VisualTable;
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

	public static Component getComponent(IComponent component) {
		if (component instanceof IComponentDelegate) {
			IComponentDelegate delegate = (IComponentDelegate) component;
			return (Component) delegate.getComponent();
		} else {
			return (Component) component;
		}
	}
	
	@Override
	public IComponent createEmptyComponent() {
		return new VaadinComponentDelegate(new Panel());
	}
	
	@Override
	public IComponent createLabel(String string) {
		return new VaadinComponentDelegate(new Label(string));
	}

	@Override
	public IComponent createTitle(String string) {
		return new VaadinComponentDelegate(new Label(string));
	}

	@Override
	public TextField createReadOnlyTextField() {
		return new VaadinReadOnlyTextField();
	}

	@Override
	public TextField createTextField(ChangeListener changeListener, int maxLength) {
		return new VaadinTextField(changeListener, maxLength);
	}
	
	@Override
	public TextField createTextField(ChangeListener changeListener, TextFieldFilter filter) {
		return new VaadinTextField(changeListener, filter);
	}

	@Override
	public FlowField createFlowField(boolean vertical) {
		return vertical ? new VaadinVerticalFlowField() : new VaadinHorizontalFlowField();
	}

	@Override
	public <T> ComboBox<T> createComboBox(ChangeListener listener) {
		return new VaadinComboBox<T>(listener);
	}

	@Override
	public CheckBox createCheckBox(ChangeListener listener, String text) {
		return new VaadinCheckBox(listener, text);
	}

	@Override
	public IComponent decorateWithCaption(IComponent component, String caption) {
		return new VaadinCaption(component, caption);
	}

	@Override
	public HorizontalLayout createHorizontalLayout(IComponent... components) {
		return new VaadinHorizontalLayout(components);
	}

	@Override
	public GridFormLayout createGridLayout(int columns, int columnWidthPercentage) {
		return new VaadinGridFormLayout(columns, columnWidthPercentage);
	}

	@Override
	public SwitchLayout createSwitchLayout() {
		return new VaadinSwitchLayout();
	}

	@Override
	public void showNotification(IComponent c, String text) {
		Component component = getComponent(c);
		Window window = component.getWindow();
		window.showNotification("Hinweis", text, Notification.TYPE_HUMANIZED_MESSAGE);
	}

	@Override
	public void focusFirstComponent(IComponent component) {
		// TODO Auto-generated method stub
	}

	@Override
	public void showMessage(IComponent c, String text) {
		// TODO Vaadin zeigt Notifikationen statt Informationsdialog
		Component component = getComponent(c);
		Window window = component.getWindow();
		window.showNotification("Information", text, Notification.TYPE_HUMANIZED_MESSAGE);
	}
	
	@Override
	public void showError(IComponent c, String text) {
		// TODO Vaadin zeigt Notifikationen statt Informationsdialog
		Component component = getComponent(c);
		Window window = component.getWindow();
		window.showNotification("Fehler", text, Notification.TYPE_ERROR_MESSAGE);
	}

	@Override
	public void showConfirmDialog(IComponent c, String message, String title, int optionType, ConfirmDialogListener listener) {
		Component component = getComponent(c);
		Window window = component.getWindow();
		while (window.getParent() != null) {
			window = window.getParent();
		}
		new VaadinConfirmDialog(window, message, title, optionType, listener);
	}

	@Override
	public <T> VisualTable<T> createVisualTable(Class<T> clazz, Object[] fields) {
		return new VaadinVisualTable<T>(clazz, fields);
	}

	@Override
	public VisualDialog openDialog(Object parent, IComponent content, String title) {
		Component component = getComponent(content);
		Component parentComponent = (Component) parent;
		Window window = parentComponent.getWindow();
		return new VaadinDialog(window, (ComponentContainer) component, title);
	}

	@Override
	public IComponent createEditorLayout(String information, IComponent content, Action[] actions) {
		return new VaadinEditorLayout(information, content, actions);
	}

	@Override
	public IComponent createSearchLayout(TextField text, Action searchAction, IComponent content, Action... actions) {
		return new VaadinEditorLayout(text, searchAction, content, actions);
	}

	@Override
	public PageContext openPageContext(PageContext parentPageContext, boolean newWindow) {
		VaadinWindow parentVaadinWindow = (VaadinWindow) parentPageContext;
		VaadinWindow vaadinWindow = new VaadinWindow();
		parentVaadinWindow.open(new ExternalResource(vaadinWindow.getURL()), "_new");
		return vaadinWindow;
	}

	@Override
	public IComponent createFormAlignLayout(IComponent content) {
		GridLayout gridLayout = new GridLayout(3, 3);
		gridLayout.addComponent(getComponent(content), 1, 1);
		gridLayout.setRowExpandRatio(0, 0.1f);
		gridLayout.setRowExpandRatio(1, 0.7f);
		gridLayout.setRowExpandRatio(2, 0.2f);
		gridLayout.setColumnExpandRatio(0, 0.1f);
		gridLayout.setColumnExpandRatio(1, 0.7f);
		gridLayout.setColumnExpandRatio(2, 0.2f);
		return new VaadinComponentDelegate(gridLayout);
	}
	
	@Override
	public PageContext findPageContext(Object source) {
		if (source instanceof IComponent) {
			source = getComponent((IComponent)source);
		}
		Component c = (Component) source;
		while (!(c instanceof PageContext) && c != null) {
			c = c.getParent();
		}
		return (PageContext) c;
	}
	
}
