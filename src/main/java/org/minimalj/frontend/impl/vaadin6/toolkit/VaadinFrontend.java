package org.minimalj.frontend.impl.vaadin6.toolkit;


import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.impl.vaadin6.VaadinWindow;
import org.minimalj.frontend.impl.vaadin6.toolkit.VaadinPasswordField.VaadinPasswordDelegate;
import org.minimalj.frontend.impl.vaadin6.toolkit.VaadinTextAreaField.VaadinTextAreaDelegate;
import org.minimalj.frontend.impl.vaadin6.toolkit.VaadinTextField.VaadinTextDelegate;
import org.minimalj.frontend.impl.vaadin6.toolkit.VaadinTextFieldAutocomplete.VaadinTextAutocompleteDelegate;
import org.minimalj.frontend.page.IDialog;
import org.minimalj.model.Rendering;
import org.minimalj.model.Rendering.RenderType;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Link;
import com.vaadin.ui.themes.BaseTheme;

public class VaadinFrontend extends Frontend {

	@Override
	public IComponent createLabel(String string) {
		return new VaadinLabel(string);
	}
	
	@Override
	public IComponent createLabel(Action action) {
		return new VaadinActionLabel(action);
	}

	public static class VaadinActionLabel extends Button implements IComponent {

		private static final long serialVersionUID = 1L;

		public VaadinActionLabel(final Action action) {
			super(action.getName());
//			button.setDescription((String) action.getValue(Action.LONG_DESCRIPTION));
			setStyleName(BaseTheme.BUTTON_LINK);
			addListener(new ClickListener() {
				private static final long serialVersionUID = 1L;

				@Override
				public void buttonClick(ClickEvent event) {
					Frontend.setBrowser((VaadinWindow) event.getComponent().getWindow());
					action.action();
					VaadinFrontend.setBrowser(null);
				}
			});
		}
	}
	
	@Override
	public IComponent createTitle(String string) {
		return new VaadinTitle(string);
	}

	@Override
	public Input<String> createReadOnlyTextField() {
		return new VaadinReadOnlyTextField();
	}

	@Override
	public Input<String> createTextField(int maxLength, String allowedCharacters, InputType inputType, List<String> choice,
			InputComponentListener changeListener) {
		if (choice == null) {
			return new VaadinTextDelegate(changeListener, maxLength, allowedCharacters);
		} else {
			return new VaadinTextAutocompleteDelegate(choice, changeListener);
		}
	}

	@Override
	public PasswordField createPasswordField(InputComponentListener changeListener, int maxLength) {
		return new VaadinPasswordDelegate(changeListener, maxLength);
	}
	
	@Override
	public Input<String> createAreaField(int maxLength, String allowedCharacters, InputComponentListener changeListener) {
		return new VaadinTextAreaDelegate(changeListener, maxLength, allowedCharacters);
	}

	@Override
	public IList createList(Action... actions) {
		return new VaadinList(actions);
	}

	@Override
	public <T> Input<T> createComboBox(List<T> objects, InputComponentListener listener) {
		return new VaadinComboBox<T>(objects, listener);
	}

	@Override
	public Input<Boolean> createCheckBox(InputComponentListener listener, String text) {
		return new VaadinCheckBox(listener, text);
	}

	@Override
	public IComponent createComponentGroup(IComponent... components) {
		return new VaadinHorizontalLayout(components);
	}

	@Override
	public FormContent createFormContent(int columns, int columnWidthPercentage) {
		return new VaadinGridFormLayout(columns, columnWidthPercentage);
	}

	@Override
	public SwitchContent createSwitchContent() {
		return new VaadinSwitchContent();
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
	public <T> ITable<T> createTable(Object[] keys, TableActionListener<T> listener) {
		return new VaadinTable<T>(keys, listener);
	}
	

	@Override
	public <T> Input<T> createLookup(InputComponentListener changeListener, Search<T> index, Object[] keys) {
		return new VaadinLookup<T>(changeListener, index, keys);
	}
	
	private static class VaadinLookup<T> extends GridLayout implements Input<T> {
		private static final long serialVersionUID = 1L;
		
		private final InputComponentListener changeListener;
		private final Search<T> search;
		private final Object[] keys;
		private final VaadinLookupLabel actionLabel;
		private final VaadinRemoveLabel removeLabel;
		private IDialog dialog;
		private T selectedObject;
		
		public VaadinLookup(InputComponentListener changeListener, Search<T> search, Object[] keys) {
			super(2, 1);
			
			this.changeListener = changeListener;
			this.search = search;
			this.keys = keys;
			
			this.actionLabel = new VaadinLookupLabel();
			this.removeLabel = new VaadinRemoveLabel();
			addComponent(actionLabel);
			addComponent(removeLabel);
			setColumnExpandRatio(0, 1.0f);
			setColumnExpandRatio(1, 0.0f);
		}

		@Override
		public void setValue(T value) {
			this.selectedObject = value;
			display();
		}

		protected void display() {
			if (selectedObject instanceof Rendering) {
				Rendering rendering = (Rendering) selectedObject;
				actionLabel.setValue(rendering.render(RenderType.PLAIN_TEXT, Locale.getDefault()));
			} else if (selectedObject != null) {
				actionLabel.setValue(selectedObject.toString());
			} else {
				actionLabel.setValue("[+]");
			}
		}
		
		@Override
		public T getValue() {
			return selectedObject;
		}
		
		@Override
		public void setEditable(boolean editable) {
			actionLabel.setEnabled(editable);
			removeLabel.setEnabled(editable);
		}

		private class VaadinLookupLabel extends Button {
			private static final long serialVersionUID = 1L;

			public VaadinLookupLabel() {
				setStyleName(BaseTheme.BUTTON_LINK);
				addListener(new ClickListener() {
					private static final long serialVersionUID = 1L;

					@Override
					public void buttonClick(ClickEvent event) {
						Frontend.setBrowser((VaadinWindow) event.getComponent().getWindow());
						dialog = ((VaadinWindow) Frontend.getBrowser()).showSearchDialog(search, keys, new LookupClickListener());
						Frontend.setBrowser(null);
					}
				});
			}
		}
		
		private class VaadinRemoveLabel extends Button {
			private static final long serialVersionUID = 1L;

			public VaadinRemoveLabel() {
				super("[x]");
				setStyleName(BaseTheme.BUTTON_LINK);
				addListener(new ClickListener() {
					private static final long serialVersionUID = 1L;

					@Override
					public void buttonClick(ClickEvent event) {
						Frontend.setBrowser((VaadinWindow) event.getComponent().getWindow());
						VaadinLookup.this.selectedObject = null;
						changeListener.changed(VaadinLookup.this);
						Frontend.setBrowser(null);
					}
				});
			}
		}
		
		private class LookupClickListener implements TableActionListener<T> {
			@Override
			public void action(T selectedObject) {
				VaadinLookup.this.selectedObject = selectedObject;
				dialog.closeDialog();
				changeListener.changed(VaadinLookup.this);
			}
		}

	}

	public IComponent createLink(String text, String address) {
		final VaadinActionLink button = new VaadinActionLink(text, address);
		return button;
	}
	
	public static class VaadinActionLink extends Link implements IComponent {
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
