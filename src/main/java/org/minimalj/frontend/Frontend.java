package org.minimalj.frontend;

import java.util.List;
import java.util.Optional;

import org.minimalj.application.Application;
import org.minimalj.application.Configuration;
import org.minimalj.backend.Backend;
import org.minimalj.frontend.action.Action;
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

	// just a helper method for all frontends. Is it at the right place here?
	public static boolean loginAtStart() {
		boolean loginAtStart = Application.getInstance().isLoginRequired() || Configuration.get("MjLoginAtStart", "false").equals("true");
		if (loginAtStart && !Backend.getInstance().isAuthenticationActive()) {
			throw new IllegalStateException("Login required but authorization is not configured!");
		}
		return loginAtStart;
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
	
	/**
	 * In text and titles html is supported (text has to start with &lt;html&gt; and
	 * end with &lt;/html&gt; but only a limited set of tags to prevent code
	 * injection. This can be overriden by configuration MjAllowedHtmlTags.
	 */
	public static final String[] ALLOWED_HTML_TAGS = { "b", "i", "u", "sub", "sup" };
	
	public abstract IComponent createText(String string);
	public abstract IComponent createText(Action action);
	public abstract IComponent createText(Rendering rendering);
	public abstract IComponent createTitle(String string);
	public abstract Input<String> createReadOnlyTextField();
	public abstract Input<String> createTextField(int maxLength, String allowedCharacters, Search<String> suggestionSearch, InputComponentListener changeListener);
	public abstract Input<String> createAreaField(int maxLength, String allowedCharacters, InputComponentListener changeListener);
	public abstract PasswordField createPasswordField(InputComponentListener changeListener, int maxLength);
	public abstract IList createList(Action... actions);
	public abstract <T> Input<T> createComboBox(List<T> object, InputComponentListener changeListener);
	public abstract Input<Boolean> createCheckBox(InputComponentListener changeListener, String text);

	public abstract Input<byte[]> createImage(int size, InputComponentListener changeListener);

	public interface SwitchComponent extends IComponent {
		public void show(IComponent component);
	}
	
	public abstract SwitchComponent createSwitchComponent();
	
	public interface IList extends IComponent {
		/**
		 * @param enabled if false no content should be shown (or
		 * only in gray) and all actions must get disabled
		 */
		public void setEnabled(boolean enabled);
		
		public void clear();
		
		public void add(IComponent component, Action... actions);
	}
	
	public interface InputComponentListener {
	    void changed(IComponent source);
	}
	
	public interface Search<S> {

		public List<S> search(String query);
	}
	
	public abstract <T> Input<T> createLookup(InputComponentListener changeListener, Search<T> search, Object[] keys);
	
	public abstract IComponent createComponentGroup(IComponent... components);

	/**
	 * Content means the content of a dialog or of a page
	 */
	public interface IContent {
	}

	public interface FormContent extends IContent {
		public void add(IComponent component);
		public void add(String caption, IComponent component, int span);
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

	public static interface TableActionListener<U> {

		public default void selectionChanged(List<U> selectedObjects) {
		}
		
		public default void action(U selectedObject) {
		}
	}
	
	public abstract <T> ITable<T> createTable(Object[] keys, boolean multiSelect, TableActionListener<T> listener);

	/**
	 * Allows several types of input:
	 * <UL>
	 * <LI>if it starts with a '&lt;html&gt;' and ends with &lt;/html&gt; it's supposed to be a html document
	 * <LI>if it is a valid url the content of that url is loaded
	 * <LI>if it ends with '.html' the content is loaded from the classpath
	 * <LI>if none of the above the input is used as plain String
	 * </UL>
	 * <strong>note:</strong> If any user input is used as html content the input should be considered
	 * dangerous as some Frontends could execute injected code.<p>
	 * 
	 * @see StringUtils#sanitizeHtml(String)
	 * 
	 * @param htmlOrUrl html, url, classpath location or string
	 * @return html content
	 */
	public abstract IContent createHtmlContent(String htmlOrUrl);
		
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

	public static <T> IDialog showSearchDialog(Search<T> index, Object[] keys, TableActionListener<T> listener) {
		return getInstance().getPageManager().showSearchDialog(index, keys, listener);
	}

	public static void showMessage(String text) {
		getInstance().getPageManager().showMessage(text);
	}
	
	public static void showError(String text) {
		getInstance().getPageManager().showError(text);
	}
}
