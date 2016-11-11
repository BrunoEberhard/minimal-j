package org.minimalj.frontend.impl.vaadin.toolkit;


import java.util.Iterator;
import java.util.List;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.page.IDialog;
import org.minimalj.frontend.page.PageManager;
import org.minimalj.model.Rendering;
import org.minimalj.model.Rendering.RenderType;

import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.BaseTheme;

public class VaadinFrontend extends Frontend {

	@Override
	public IComponent createText(String string) {
		return new VaadinText(string);
	}
	
	@Override
	public IComponent createText(Rendering rendering) {
		return new VaadinText(rendering);
	}

	@Override
	public IComponent createText(Action action) {
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
					action.action();
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
	public Input<String> createTextField(int maxLength, String allowedCharacters, InputType inputType, Search<String> suggestionSearch,
			InputComponentListener changeListener) {
		return new VaadinTextField(changeListener, maxLength);
	}
	
	@Override
	public Input<String> createAreaField(int maxLength, String allowedCharacters, InputComponentListener changeListener) {
		return new VaadinTextAreaField(changeListener, maxLength, allowedCharacters);
	}

	@Override
	public IList createList(Action... actions) {
		return new VaadinList(actions);
	}

	@Override
	public <T> Input<T> createComboBox(List<T> objects, InputComponentListener changeListener) {
		return new VaadinComboBox<T>(objects, changeListener);
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
		private IDialog dialog;
		private T selectedObject;
		
		public VaadinLookup(InputComponentListener changeListener, Search<T> search, Object[] keys) {
			super(2, 1);
			
			this.changeListener = changeListener;
			this.search = search;
			this.keys = keys;
			
			this.actionLabel = new VaadinLookupLabel();
			addComponent(actionLabel);
			addComponent(new VaadinRemoveLabel());
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
				actionLabel.setCaption(rendering.render(RenderType.PLAIN_TEXT));
			} else if (selectedObject != null) {
				actionLabel.setCaption(selectedObject.toString());
			} else {
				actionLabel.setCaption("[+]");
			}
		}

		@Override
		public T getValue() {
			return selectedObject;
		}
		
		@Override
		public void setEditable(boolean editable) {
			throw new RuntimeException("Not yet implemented");
		}
		
		private class VaadinLookupLabel extends Button {
			private static final long serialVersionUID = 1L;

			public VaadinLookupLabel() {
				setStyleName(BaseTheme.BUTTON_LINK);
				addListener(new ClickListener() {
					private static final long serialVersionUID = 1L;

					@Override
					public void buttonClick(ClickEvent event) {
						dialog = Frontend.showSearchDialog(search, keys, new LookupClickListener());
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
						VaadinLookup.this.selectedObject = null;
						changeListener.changed(VaadinLookup.this);
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
	
//	@Override
//	public OutputStream store(String buttonText) {
//		return new VaadinExportDialog("Export").getOutputStream();
//	}
//
//	@Override
//	public InputStream load(String buttonText) {
//		VaadinImportDialog importDialog = new VaadinImportDialog("Import");
//		PipedInputStream inputStream = importDialog.getInputStream();
//		return inputStream;
//	}

//	public static class VaadinActionLink extends Link implements ILink {
//		private static final long serialVersionUID = 1L;
//		private final String address;
//		
//		public VaadinActionLink(String text, String address) {
//			super(text, new ExternalResource("#" + address));
//			this.address = address;
//		}
//
//		public String getAddress() {
//			return address;
//		}
//		
//	}

	@Override
	public PasswordField createPasswordField(InputComponentListener changeListener, int maxLength) {
		return new VaadinPasswordField(changeListener, maxLength);
	}

	@Override
	public Input<byte[]> createImage(Size size, InputComponentListener changeListener) {
		return new VaadinImage(size, changeListener);
	}

	/* (non-Javadoc)
	 * @see org.minimalj.frontend.Frontend#createHtmlContent(java.lang.String)
	 */
	@Override
	public IContent createHtmlContent(String htmlOrUrl) {
//		return new VaadinHtmlContent(htmlOrUrl);
		return new VaadinUrlContent(htmlOrUrl);
	}

	/* (non-Javadoc)
	 * @see org.minimalj.frontend.Frontend#getPageManager()
	 */
	@Override
	public PageManager getPageManager() {
		return (PageManager) UI.getCurrent();
	}
	
	
}
