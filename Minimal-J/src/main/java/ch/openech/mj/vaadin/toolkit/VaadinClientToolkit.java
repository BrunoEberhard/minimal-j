package ch.openech.mj.vaadin.toolkit;


import java.io.InputStream;
import java.io.PipedInputStream;
import java.util.Iterator;

import javax.swing.Action;
import javax.swing.event.ChangeListener;

import ch.openech.mj.page.PageContext;
import ch.openech.mj.toolkit.CheckBox;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.ComboBox;
import ch.openech.mj.toolkit.ConfirmDialogListener;
import ch.openech.mj.toolkit.ExportHandler;
import ch.openech.mj.toolkit.FlowField;
import ch.openech.mj.toolkit.GridFormLayout;
import ch.openech.mj.toolkit.HorizontalLayout;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.IComponentDelegate;
import ch.openech.mj.toolkit.ImportHandler;
import ch.openech.mj.toolkit.SwitchLayout;
import ch.openech.mj.toolkit.TextField;
import ch.openech.mj.toolkit.TextField.TextFieldFilter;
import ch.openech.mj.toolkit.VisualDialog;
import ch.openech.mj.toolkit.VisualTable;
import ch.openech.mj.util.ProgressListener;

import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
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
	
	private static Component getComponent(Object component) {
		if (component instanceof IComponent) {
			return getComponent((IComponent) component);
		} else {
			return (Component) component;
		}
	}
	
	@Override
	public IComponent createLabel(String string) {
		Label label = new Label(string);
		label.setContentMode(Label.CONTENT_XHTML);
		return new VaadinComponentDelegate(label);
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
	public FlowField createFlowField() {
		return new VaadinVerticalFlowField();
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
		Component c = getComponent(component);
		AbstractField field = findAbstractField(c);
		if (field != null) {
			field.focus();
		}
	}
	
	private static AbstractField findAbstractField(Component c) {
		if (c instanceof AbstractField) {
			return ((AbstractField) c);
		} else if (c instanceof ComponentContainer) {
			ComponentContainer container = (ComponentContainer) c;
			Iterator<Component> components = container.getComponentIterator();
			while (components.hasNext()) {
				AbstractField field = findAbstractField(components.next());
				if (field != null) {
					return field;
				}
			}
		}
		return null;
	}
	
	@Override
	public void showMessage(Object parent, String text) {
		// TODO Vaadin zeigt Notifikationen statt Informationsdialog
		Component parentComponent = getComponent(parent);
		Window window = parentComponent.getWindow();
		window.showNotification("Information", text, Notification.TYPE_HUMANIZED_MESSAGE);
	}
	
	@Override
	public void showError(Object parent, String text) {
		// TODO Vaadin zeigt Notifikationen statt Informationsdialog
		Component parentComponent = getComponent(parent);
		Window window = parentComponent.getWindow();
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
		Component parentComponent = getComponent(parent);
		Window window = parentComponent.getWindow();
		return new VaadinDialog(window, (ComponentContainer) component, title);
	}

	@Override
	public ProgressListener showProgress(Object parent, String text) {
		Component parentComponent = getComponent(parent);
		Window window = parentComponent.getWindow();
		VaadinProgressDialog progressDialog = new VaadinProgressDialog(window, text);
		return progressDialog;
	}

	@Override
	public IComponent createEditorLayout(IComponent content, Action[] actions) {
		return new VaadinEditorLayout(content, actions);
	}

	@Override
	public IComponent createSearchLayout(TextField text, Action searchAction, IComponent content, Action... actions) {
		VaadinEditorLayout layout = new VaadinEditorLayout(text, searchAction, content, actions);
		layout.setWidth("80em");
		return layout;
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

	@Override
	public IComponent exportLabel(ExportHandler exportHandler, String label) {
		return new VaadinExportLabel(exportHandler, label);
//		Component parentComponent = (Component) parent;
//		Window window = parentComponent.getWindow();
//
//		VaadinExportDialog exportDialog = new VaadinExportDialog(window, "Export");
//		return exportDialog.getOutputStream();
	}

	@Override
	public IComponent importField(ImportHandler importHandler, String buttonText) {
		return new VaadinImportField(importHandler, buttonText);
//		Component parentComponent = (Component) parent;
//		Window window = parentComponent.getWindow();
//
//		VaadinImportDialog importDialog = new VaadinImportDialog(window, "Import");
//		PipedInputStream inputStream = importDialog.getInputStream();
//		return inputStream;
	}

	@Override
	public void export(Object parent, String buttonText, ExportHandler exportHandler) {
		Component parentComponent = (Component) parent;
		Window window = parentComponent.getWindow();

		new VaadinExportDialog(window, "Export", exportHandler);
	}

	@Override
	public InputStream imprt(Object parent, String buttonText) {
		Component parentComponent = (Component) parent;
		Window window = parentComponent.getWindow();

		VaadinImportDialog importDialog = new VaadinImportDialog(window, "Import");
		PipedInputStream inputStream = importDialog.getInputStream();
		return inputStream;
	}
	
}
