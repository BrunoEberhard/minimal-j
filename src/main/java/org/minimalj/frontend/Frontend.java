package org.minimalj.frontend;

import java.net.URL;
import java.util.List;
import java.util.Optional;

import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.action.ActionGroup;
import org.minimalj.frontend.form.element.FormElementConstraint;
import org.minimalj.frontend.page.IDialog;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.page.PageManager;
import org.minimalj.model.Rendering;
import org.minimalj.util.StringUtils;

/**
 * To provide a new kind (Xy) of client you have to implement two things:
 * <OL>
 * <LI>This class, like XyFrontend</LI>
 * <LI>Some kind of XyApplication with a main. The XyApplication should take an instance of Application and 
 * start the client. Take a look at the existing SwingFrontend, JsonFrontend or LanternaFrontend. The trickiest part will be to implement
 * the PageManager.</LI>
 * </OL>
 *
 */
public abstract class Frontend {

	private static Frontend instance;
	
	public static Frontend getInstance() {
		if (instance == null) {
			throw new IllegalStateException("Frontend has to be initialized");
		}
		return instance;
	}

	public static void setInstance(Frontend frontend) {
		if (Frontend.instance != null) {
			throw new IllegalStateException("Frontend cannot be changed");
		}		
		if (frontend == null) {
			throw new IllegalArgumentException("Frontend cannot be null");
		}
		Frontend.instance = frontend;
	}

	public static boolean isAvailable() {
		return Frontend.instance != null;
	}

	/**
	 * Components are the smallest part of the gui. Things like textfields
	 * and comboboxes. A form is filled with components.
	 */
	public interface IComponent {
	}
	
	public interface Input<T> extends IComponent {
		
		public void setValue(T value);

		public T getValue();

		public void setEditable(boolean editable);
	}

	public interface PasswordField extends Input<char[]> {

	}
	
	 // http://www.w3schools.com/html/html_form_input_types.asp 
	public enum InputType { TEXT, EMAIL, URL, TEL, NUMBER, DATE, TIME, DATETIME; }
	
	/**
	 * Frontends may or may not provide special Inputs. Even a single Frontend
	 * can sometimes support those Inputs and sometimes not. For example a HTML
	 * Frontend depends on the used Browser. Firefox and Microsoft didn't
	 * support date and time Inputs for a long time. Also the used device or the
	 * user preferences can influence whether the returned Optional is empty or
	 * contains an Input.
	 * 
	 * @param maxLength maximum input length
	 * @param inputType TEXT, EMAIL, ...
	 * @param changeListener listener attached to the Input
	 * @return optional
	 */
	public Optional<Input<String>> createInput(int maxLength, InputType inputType, InputComponentListener changeListener) {
		return Optional.empty();
	}
	
	public abstract IComponent createText(String string);
	public abstract IComponent createText(Rendering rendering);
	public abstract IComponent createText(Action action);
	public abstract IComponent createTitle(String string);
	public abstract Input<String> createReadOnlyTextField();
	public abstract Input<String> createTextField(int maxLength, String allowedCharacters, Search<String> suggestionSearch, InputComponentListener changeListener);
	public abstract Input<String> createAreaField(int maxLength, String allowedCharacters, InputComponentListener changeListener);
	public abstract PasswordField createPasswordField(InputComponentListener changeListener, int maxLength);
	public abstract <T> Input<T> createComboBox(List<T> items, InputComponentListener changeListener);
	public abstract Input<Boolean> createCheckBox(InputComponentListener changeListener, String text);

	public abstract Input<byte[]> createImage(InputComponentListener changeListener);

	public interface SwitchComponent extends IComponent {
		public void show(IComponent component);
	}
	
	public abstract SwitchComponent createSwitchComponent();
	
	public interface InputComponentListener {
	    void changed(IComponent source);
	}
	
	public interface Search<S> {

		public List<S> search(String query);
	}

	// decorate?
	public abstract Input<String> createLookup(Input<String> stringInput, Runnable lookup);

	public abstract Input<String> createLookup(Input<String> stringInput, ActionGroup actions);

	public abstract IComponent createHorizontalGroup(IComponent... components);

	public abstract IComponent createVerticalGroup(IComponent... components);

	/**
	 * Content means the content of a dialog or of a page
	 */
	public interface IContent {
	}

	public interface FormContent extends IContent {
		public void add(String caption, IComponent component, FormElementConstraint constraint, int span);
		public void setValidationMessages(IComponent component, List<String> validationMessages);
	}
	
	public abstract FormContent createFormContent(int columns, int columnWidth);

	public interface SwitchContent extends IContent {
		public void show(IContent content);
	}
	
	public abstract SwitchContent createSwitchContent();
	
	public interface ITable<T> extends IContent {
		
		public void setObjects(List<T> objects);
	}

	public interface TableActionListener<U> {

		public default void selectionChanged(List<U> selectedObjects) {
		}
		
		public default void action(U selectedObject) {
		}
	}
	
	// experimental. Signature may change. Idea is to have a header or filter above
	// a table. But the header/filter must not be a growing content
	public abstract IContent createFormTableContent(FormContent form, ITable<?> table);

	public abstract <T> ITable<T> createTable(Object[] keys, boolean multiSelect, TableActionListener<T> listener);

	/**
	 * <p>
	 * <strong>note:</strong> If any user input is used as html content the input
	 * should be considered dangerous as some Frontends could execute injected code.
	 * </p>
	 * 
	 * @see StringUtils#escapeHTML(String)
	 * 
	 * @param html valid html
	 * @return html content
	 */
	public abstract IContent createHtmlContent(String html);

	public abstract IContent createHtmlContent(URL url);

	public void showBrowser(URL url) {
		showBrowser(url.toExternalForm());
	}

	public void showBrowser(String url) {
		throw new RuntimeException("Not yet implemented");
	}

	/**
	 * Create a content with a caption and a large search field. Something like
	 * what a really big search engine show.
	 * <p>
	 * 
	 * The caption text is defined by the Resource QueryPage or if that is not
	 * available by the Application.name .
	 * 
	 * @return query content
	 */
	public abstract IContent createQueryContent();
	
	//
	
	public abstract PageManager getPageManager();
	
	// delegating shortcuts
	
	public static void show(Page page) {
		getInstance().getPageManager().show(page);
	}

	public static void showDetail(Page mainPage, Page detail) {
		getInstance().getPageManager().showDetail(mainPage, detail);
	}
	
	public static void hideDetail(Page page) {
		getInstance().getPageManager().hideDetail(page);
	}
	
	public static boolean isDetailShown(Page page) {
		return getInstance().getPageManager().isDetailShown(page);
	}

	public static IDialog showDialog(String title, IContent content, Action saveAction, Action closeAction, Action... actions) {
		return getInstance().getPageManager().showDialog(title, content, saveAction, closeAction, actions);
	}

	public abstract <T> IContent createTable(Search<T> search, Object[] keys, boolean multiSelect, TableActionListener<T> listener);

	public static void showMessage(String text) {
		getInstance().getPageManager().showMessage(text);
	}
	
	public static void showError(String text) {
		getInstance().getPageManager().showError(text);
	}
}
