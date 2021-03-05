package org.minimalj.frontend.impl.vaadin.toolkit;


import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.minimalj.application.Application;
import org.minimalj.application.Application.AuthenticatonMode;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.action.ActionGroup;
import org.minimalj.frontend.page.IDialog;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.page.PageManager;
import org.minimalj.model.Rendering;
import org.minimalj.util.LocaleContext;
import org.minimalj.util.resources.Resources;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.HasOrderedComponents;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.HasPrefixAndSuffix;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.server.VaadinSession;

public class VaadinFrontend extends Frontend {

	public VaadinFrontend() {
		LocaleContext.setLocale(() -> VaadinSession.getCurrent() != null ? VaadinSession.getCurrent().getLocale() : null);
	}

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

    public static interface HasCaption {
        public void setLabel(String label);
    }

    public static interface HasComponent {
        public Component getComponent();
    }

	public static class VaadinActionLabel extends Anchor implements IComponent {

		private static final long serialVersionUID = 1L;

		public VaadinActionLabel(final Action action) {
			super(action.getName());
			getElement().setProperty("title", action.getDescription());
			getElement().addEventListener("click", e -> action.run());
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
	
    private static class VaadinEmailField extends EmailField implements Input<String>, HasCaption {
		private static final long serialVersionUID = 1L;

		public VaadinEmailField(InputComponentListener changeListener, int maxLength) {
			setMaxLength(maxLength);
			addValueChangeListener(event -> changeListener.changed(VaadinEmailField.this));
			setValueChangeMode(ValueChangeMode.TIMEOUT);
		}
		
		@Override
		public void setEditable(boolean editable) {
			setReadOnly(!editable);
		}
	}
	
    private static class VaadinDateField implements Input<String>, HasCaption, HasComponent, HasElement {
        private static final long serialVersionUID = 1L;
        private final DatePicker picker = new DatePicker();
		
		public VaadinDateField(InputComponentListener changeListener) {
			picker.addValueChangeListener(event -> changeListener.changed(VaadinDateField.this));
		}
		
		@Override
		public void setEditable(boolean editable) {
			picker.setReadOnly(!editable);
		}

		@Override
		public String getValue() {
			LocalDate value = picker.getValue();
			return value != null ? value.toString() : null;
		}

		@Override
		public void setValue(String value) {
			picker.setValue(value != null ? LocalDate.parse(value) : null);
		}

        @Override
        public void setLabel(String label) {
            picker.setLabel(label);
        }

        @Override
        public Component getComponent() {
            return picker;
        }

        @Override
        public Element getElement() {
            return picker.getElement();
        }
	}
	
	@Override
	public Optional<Input<String>> createInput(int maxLength, InputType inputType, InputComponentListener changeListener) {
		if (inputType == InputType.DATE) {
			return Optional.of(new VaadinDateField(changeListener));
		} else if (inputType == InputType.EMAIL) {
			return Optional.of(new VaadinEmailField(changeListener, maxLength));
		} else {
			return super.createInput(maxLength, inputType, changeListener);
		}
	}
	
	@Override
	public Input<String> createAreaField(int maxLength, String allowedCharacters, InputComponentListener changeListener) {
		return new VaadinTextAreaField(changeListener, maxLength, allowedCharacters);
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
	public IComponent createHorizontalGroup(IComponent... components) {
		return new VaadinHorizontalLayout(components);
	}

	@Override
	public IComponent createVerticalGroup(IComponent... components) {
		return new VaadinVerticalLayout(components);
	}

	@Override
	public FormContent createFormContent(int columns, int columnWidthPercentage) {
        return new VaadinFormContent(columns, columnWidthPercentage);
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
		Focusable<?> field = findFocusable(component);
		if (field != null) {
			field.focus();
		}
	}
	
	private static Focusable<?> findFocusable(Component c) {
		if (c instanceof Focusable) {
			return ((Focusable<?>) c);
		} else if (c instanceof HasOrderedComponents) {
			HasOrderedComponents container = (HasOrderedComponents) c;
			return container.getChildren().map(child -> findFocusable(child)).findFirst().orElse(null);
		}
		return null;
	}

	@Override
	public <T> ITable<T> createTable(Object[] keys, boolean multiSelect, TableActionListener<T> listener) {
		return new VaadinTable<T>(keys, multiSelect, listener);
	}

	@Override
	public IContent createFormTableContent(FormContent form, ITable<?> table) {
		VaadinBorderLayoutContent content = new VaadinBorderLayoutContent();
		content.add((Component) form);
		content.addAndExpand((Component) table);
		return content;
	}

	private static class VaadinBorderLayoutContent extends VerticalLayout implements IContent {
		private static final long serialVersionUID = 1L;

		public VaadinBorderLayoutContent() {
			setMargin(false);
		}
	}

	@Override
	public <T> IContent createTable(Search<T> search, Object[] keys, boolean multiSelect, TableActionListener<T> listener) {
		return new VaadinSearchPanel<T>(search, keys, multiSelect, listener);
	}

	@Override
	public Input<String> createLookup(Input<String> stringInput, Runnable lookup) {
		return new VaadinLookup(stringInput, lookup);
	}

	@Override
	public Input<String> createLookup(Input<String> input, ActionGroup actions) {
		if (!actions.getItems().isEmpty()) {
			return new VaadinLookupWithMenu(input, actions.getItems());
		}
		return input;
	}

	private static class VaadinLookupWithMenu implements Input<String>, HasComponent {
		private final Input<String> stringInput;
		private final Button lookupButton;

		public VaadinLookupWithMenu(Input<String> stringInput, List<Action> actions) {
			this.stringInput = stringInput;

			this.lookupButton = new Button("...");
			ContextMenu menu = VaadinMenu.createMenu(lookupButton, actions);
			menu.setOpenOnClick(true);
			((HasPrefixAndSuffix) stringInput).setSuffixComponent(lookupButton);
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

		@Override
		public Component getComponent() {
			return (Component) stringInput;
		}
	}

    private static class VaadinLookup implements Input<String>, HasComponent {
		private final Input<String> stringInput;
		private final Button lookupButton;
		
		public VaadinLookup(Input<String> stringInput, Runnable lookup) {
            this.stringInput = stringInput;

			this.lookupButton = new Button("...", event -> lookup.run());
			lookupButton.setHeight("60%");
            ((HasPrefixAndSuffix) stringInput).setSuffixComponent(lookupButton);
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

        @Override
        public Component getComponent() {
            return (Component) stringInput;
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
	public Input<byte[]> createImage(InputComponentListener changeListener) {
		return new VaadinImage(changeListener);
	}

	@Override
	public IContent createHtmlContent(String html) {
		return new VaadinHtmlContent(html);
	}

	@Override
	public IContent createHtmlContent(URL url) {
		return new VaadinHtmlContent(url);
	}

	@Override
	public PageManager getPageManager() {
		return (PageManager) UI.getCurrent().getSession().getAttribute("pageManager");
	}
	
	@Override
	public Optional<IDialog> showLogin(IContent content, Action loginAction, Action... additionalActions) {
		Page page = new Page() {
			@Override
			public IContent getContent() {
				Action[] actions;
				if (Application.getInstance().getAuthenticatonMode() != AuthenticatonMode.REQUIRED) {
					SkipLoginAction skipLoginAction = new SkipLoginAction();
					actions = new org.minimalj.frontend.action.Action[] {skipLoginAction, loginAction};
				} else {
					actions = new org.minimalj.frontend.action.Action[] {loginAction};
				}
				VaadinEditorLayout editorLayout = new VaadinEditorLayout(getTitle(), (Component) content, loginAction, null, actions);
				VaadinHorizontalLayout centerLayout = new VaadinHorizontalLayout(new IComponent[] {editorLayout});
				editorLayout.setSizeUndefined(); // VaadinHorizontalLayout constructor did set the width
				centerLayout.setSizeFull();
				centerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
				centerLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
				return centerLayout;
			}
			
			@Override
			public String getTitle() {
				return Resources.getString("Login.title");
			}
		};
		show(page);
		return Optional.empty();
	}

	private class SkipLoginAction extends Action {
		
		@Override
		public void run() {
			Frontend.getInstance().login(null);
		}
	}
}
