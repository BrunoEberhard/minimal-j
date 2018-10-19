package org.minimalj.frontend.impl.vaadin.toolkit;


import java.util.Iterator;
import java.util.List;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.page.PageManager;
import org.minimalj.model.Rendering;

import com.vaadin.ui.AbstractField;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;

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
			setStyleName(ValoTheme.BUTTON_LINK);
			addClickListener(new ClickListener() {
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
	public Input<String> createTextField(int maxLength, String allowedCharacters, Search<String> suggestionSearch, InputComponentListener changeListener) {
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
		return new VaadinSwitch();
	}

	@Override
	public SwitchComponent createSwitchComponent() {
		return new VaadinSwitch();
	}

	@Override
	public IContent createQueryContent() {
		return new VaadinQueryContent();
	}

	public static void focusFirstComponent(Component component) {
		AbstractField<?> field = findAbstractField(component);
		if (field != null) {
			field.focus();
		}
	}
	
	private static AbstractField<?> findAbstractField(Component c) {
		if (c instanceof AbstractField) {
			return ((AbstractField<?>) c);
		} else if (c instanceof ComponentContainer) {
			ComponentContainer container = (ComponentContainer) c;
			Iterator<Component> components = container.iterator();
			while (components.hasNext()) {
				AbstractField<?> field = findAbstractField(components.next());
				if (field != null) {
					return field;
				}
			}
		}
		return null;
	}

	@Override
	public <T> ITable<T> createTable(Object[] keys, boolean multiSelect, TableActionListener<T> listener) {
		return new VaadinTable<T>(keys, multiSelect, listener);
	}

	@Override
	public <T> IContent createTable(Search<T> search, Object[] keys, boolean multiSelect, TableActionListener<T> listener) {
		return new VaadinSearchPanel<T>(search, keys, multiSelect, listener);
	}

	@Override
	public Input<String> createLookup(Input<String> stringInput, Runnable lookup) {
		return new VaadinLookup(stringInput, lookup);
	}
	
	private static class VaadinLookup extends GridLayout implements Input<String> {
		private static final long serialVersionUID = 1L;
		
		private final Input<String> stringInput;
		private final Button lookupButton;
		
		public VaadinLookup(Input<String> stringInput, Runnable lookup) {
			super(2, 1);
			this.stringInput = stringInput;
			
			((Component) stringInput).setSizeFull();
			addComponent((Component) stringInput);

			this.lookupButton = new Button("...", event -> lookup.run());
			addComponent(lookupButton);
			
			setColumnExpandRatio(0, 100.0f);
			setColumnExpandRatio(1, 0.0f);
		}

		@Override
		public void setValue(String value) {
			stringInput.setValue(value);
		}
		
		@Override
		public String getValue() {
			return stringInput.getValue();
		}
		
		@Override
		public void setEditable(boolean editable) {
			stringInput.setEditable(editable);
			lookupButton.setVisible(editable);
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
	public Input<byte[]> createImage(int size, InputComponentListener changeListener) {
		return new VaadinImage(size, changeListener);
	}

	@Override
	public IContent createHtmlContent(String htmlOrUrl) {
		return new VaadinHtmlContent(htmlOrUrl);
	}

	@Override
	public PageManager getPageManager() {
		return (PageManager) UI.getCurrent();
	}
	
	
}
