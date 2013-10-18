package ch.openech.mj.vaadin.toolkit;


import java.io.InputStream;
import java.io.PipedInputStream;
import java.util.Iterator;
import java.util.List;

import ch.openech.mj.search.Search;
import ch.openech.mj.toolkit.Caption;
import ch.openech.mj.toolkit.CheckBox;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.ComboBox;
import ch.openech.mj.toolkit.ExportHandler;
import ch.openech.mj.toolkit.FlowField;
import ch.openech.mj.toolkit.GridFormLayout;
import ch.openech.mj.toolkit.HorizontalLayout;
import ch.openech.mj.toolkit.IAction;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.IDialog;
import ch.openech.mj.toolkit.ILink;
import ch.openech.mj.toolkit.ITable;
import ch.openech.mj.toolkit.ITable.TableActionListener;
import ch.openech.mj.toolkit.ProgressListener;
import ch.openech.mj.toolkit.SwitchLayout;
import ch.openech.mj.toolkit.TextField;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Link;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import com.vaadin.ui.themes.BaseTheme;

public class VaadinClientToolkit extends ClientToolkit {

	@Override
	public IComponent createLabel(String string) {
		return new VaadinLabel(string);
	}
	
	@Override
	public IComponent createLabel(IAction action) {
		return new VaadinActionLabel(action);
	}

	private static class VaadinActionLabel extends Button implements IComponent {

		private static final long serialVersionUID = 1L;

		public VaadinActionLabel(final IAction action) {
			super(action.getName());
//			button.setDescription((String) action.getValue(Action.LONG_DESCRIPTION));
			setStyleName(BaseTheme.BUTTON_LINK);
			addListener(new ClickListener() {
				private static final long serialVersionUID = 1L;

				@Override
				public void buttonClick(ClickEvent event) {
					action.action(VaadinActionLabel.this);
				}
			});
		}
	}
	
	@Override
	public IComponent createTitle(String string) {
		return new VaadinTitle(string);
	}

	@Override
	public TextField createReadOnlyTextField() {
		return new VaadinReadOnlyTextField();
	}

	@Override
	public TextField createTextField(InputComponentListener changeListener, int maxLength) {
		return new VaadinTextField(changeListener, maxLength);
	}
	
	@Override
	public TextField createTextField(InputComponentListener changeListener, int maxLength, String allowedCharacters) {
		return new VaadinTextField(changeListener, maxLength, allowedCharacters);
	}

	@Override
	public FlowField createFlowField() {
		return new VaadinVerticalFlowField();
	}

	@Override
	public <T> ComboBox<T> createComboBox(InputComponentListener listener) {
		return new VaadinComboBox<T>(listener);
	}

	@Override
	public CheckBox createCheckBox(InputComponentListener listener, String text) {
		return new VaadinCheckBox(listener, text);
	}

	@Override
	public Caption decorateWithCaption(final IComponent component, String text) {
		final AbstractComponent vaadinComponent = (AbstractComponent) component;
		vaadinComponent.setCaption(text);
		Caption caption = new Caption() {
			@Override
			public void setValidationMessages(List<String> validationMessages) {
				VaadinIndication.setValidationMessages(validationMessages, vaadinComponent);
			}

			@Override
			public IComponent getComponent() {
				return component;
			}
		};
		return caption;
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

	public static void focusFirstComponent(Component component) {
		AbstractField field = findAbstractField(component);
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
		Component parentComponent = (Component) parent;
		Window window = parentComponent.getWindow();
		window.showNotification("Information", text, Notification.TYPE_HUMANIZED_MESSAGE);
	}
	
	@Override
	public void showError(Object parent, String text) {
		// TODO Vaadin zeigt Notifikationen statt Informationsdialog
		Component parentComponent = (Component) parent;
		Window window = parentComponent.getWindow();
		window.showNotification("Fehler", text, Notification.TYPE_ERROR_MESSAGE);
	}

	@Override
	public void showConfirmDialog(IComponent c, String message, String title, ConfirmDialogType type, DialogListener listener) {
		Component component = (Component) c;
		Window window = component.getWindow();
		while (window.getParent() != null) {
			window = window.getParent();
		}
		new VaadinConfirmDialog(window, message, title, type, listener);
	}

	@Override
	public <T> ITable<T> createTable(Class<T> clazz, Object[] fields) {
		return new VaadinTable<T>(clazz, fields);
	}
	
	@Override
	public IDialog createDialog(IComponent parent, String title, IComponent content, IAction... actions) {
		Component component = new VaadinEditorLayout(content, actions);
		component.setSizeFull();

		return createDialog(parent, title, component);
	}

	private IDialog createDialog(IComponent parent, String title, Component component) {
		Component parentComponent = (Component) parent;
		Window window = parentComponent.getWindow();
		// need to find application-level window
		while (window.getParent() != null) {
			window = window.getParent();
		}
		return new VaadinDialog(window, (ComponentContainer) component, title);
	}

	public static ProgressListener showProgress(Object parent, String text) {
		Component parentComponent = (Component) parent;
		Window window = parentComponent.getWindow();
		VaadinProgressDialog progressDialog = new VaadinProgressDialog(window, text);
		return progressDialog;
	}
	
	@Override
	public <T> IDialog createSearchDialog(IComponent parent, Search<T> search, TableActionListener<T> listener) {
		VaadinSearchPanel<T> panel = new VaadinSearchPanel<>(search, listener);
		return createDialog(parent, null, panel);
	}

	@Override
	public IComponent createFormAlignLayout(IComponent content) {
		VaadinGridLayout gridLayout = new VaadinGridLayout(3, 3);
		gridLayout.setMargin(false);
		gridLayout.setStyleName("gridForm");
		gridLayout.setSizeFull();
		gridLayout.addComponent((Component) content, 1, 1);
		gridLayout.setRowExpandRatio(0, 0.1f);
		gridLayout.setRowExpandRatio(1, 0.7f);
		gridLayout.setRowExpandRatio(2, 0.2f);
		gridLayout.setColumnExpandRatio(0, 0.3f);
		gridLayout.setColumnExpandRatio(1, 0.0f);
		gridLayout.setColumnExpandRatio(2, 0.7f);
		return gridLayout;
	}
	
	private class VaadinGridLayout extends GridLayout implements IComponent {
		private static final long serialVersionUID = 1L;

		public VaadinGridLayout(int columns, int rows) {
			super(columns, rows);
		}
	}
	
	@Override
	public void export(IComponent parent, String buttonText, ExportHandler exportHandler) {
		Component parentComponent = (Component) parent;
		Window window = parentComponent.getWindow();

		new VaadinExportDialog(window, "Export", exportHandler);
	}

	@Override
	public InputStream imprt(IComponent parent, String buttonText) {
		Component parentComponent = (Component) parent;
		Window window = parentComponent.getWindow();

		VaadinImportDialog importDialog = new VaadinImportDialog(window, "Import");
		PipedInputStream inputStream = importDialog.getInputStream();
		return inputStream;
	}

	@Override
	public ILink createLink(String text, String address) {
		final VaadinActionLink button = new VaadinActionLink(text, address);
		return button;
	}
	
	public static class VaadinActionLink extends Link implements ILink {
		private static final long serialVersionUID = 1L;
		private final String address;
		
		public VaadinActionLink(String text, String address) {
			super(text, new ExternalResource("#" + address));
			this.address = address;
		}

		public String getAddress() {
			return address;
		}
		
	}
	
}
