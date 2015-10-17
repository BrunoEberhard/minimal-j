package org.minimalj.frontend;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.page.IDialog;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.page.PageBrowser;
import org.minimalj.model.Rendering;
import org.minimalj.security.Subject;

/**
 * To provide a new kind (Xy) of client you have to implement two things:
 * <OL>
 * <LI>This class, like XyFrontend</LI>
 * <LI>Some kind of XyApplication with a main. The XyApplication should take an instance of Application and 
 * start the client. Take a look at the existing SwingFrontend, JsonFrontend or LanternaFrontend. The trickiest part will be to implement
 * the PageBrowser.</LI>
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

	public static synchronized void setInstance(Frontend frontend) {
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
	public enum InputType { FREE, EMAIL, URL, TEL, NUMBER; }

	public abstract IComponent createLabel(String string); // TODO -> createText
	public abstract IComponent createLabel(Action action); // TODO -> createText
	public abstract IComponent createText(Rendering rendering);
	public abstract IComponent createTitle(String string);
	public abstract Input<String> createReadOnlyTextField();
	public abstract Input<String> createTextField(int maxLength, String allowedCharacters, InputType inputType, Search<String> suggestionSearch, InputComponentListener changeListener);
	public abstract Input<String> createAreaField(int maxLength, String allowedCharacters, InputComponentListener changeListener);
	public abstract PasswordField createPasswordField(InputComponentListener changeListener, int maxLength);
	public abstract IList createList(Action... actions);
	public abstract <T> Input<T> createComboBox(List<T> object, InputComponentListener changeListener);
	public abstract Input<Boolean> createCheckBox(InputComponentListener changeListener, String text);

	public enum Size { SMALL, MEDIUM, LARGE	};
	public abstract Input<byte[]> createImage(Size size, InputComponentListener changeListener);

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
	
	public abstract <T> Input<T> createLookup(InputComponentListener changeListener, Search<T> index, Object[] keys);
	
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

		public default void selectionChanged(U selectedObject, List<U> selectedObjects) {
		}
		
		public default void action(U selectedObject) {
		}
		
		public default List<Action> getActions(U selectedObject, List<U> selectedObjects) {
			return Collections.emptyList();
		}
	}
	
	public abstract <T> ITable<T> createTable(Object[] keys, TableActionListener<T> listener);
	
	public abstract IContent createHtmlContent(String htmlOrUrl);
	
	//
	
	public Subject getSubject() {
		return getBrowser() != null ? getBrowser().getSubject() : null;
	}

	public void setSubject(Subject subject) {
		getBrowser().setSubject(subject);
	}
	
	//
	
	public abstract PageBrowser getBrowser();
	
	//
	
	public <INPUT, RESULT> RESULT executeSync(Function<INPUT, RESULT> function, INPUT input) {
		return function.apply(input);
	}

	// delegating shortcuts
	
	public static void show(Page page) {
		getInstance().getBrowser().show(page);
	}

	public static void showDetail(Page mainPage, Page detail) {
		getInstance().getBrowser().showDetail(mainPage, detail);
	}
	
	public static void hideDetail(Page page) {
		getInstance().getBrowser().hideDetail(page);
	}
	
	public static boolean isDetailShown(Page page) {
		return getInstance().getBrowser().isDetailShown(page);
	}

	public static IDialog showDialog(String title, IContent content, Action saveAction, Action closeAction, Action... actions) {
		return getInstance().getBrowser().showDialog(title, content, saveAction, closeAction, actions);
	}

	public static <T> IDialog showSearchDialog(Search<T> index, Object[] keys, TableActionListener<T> listener) {
		return getInstance().getBrowser().showSearchDialog(index, keys, listener);
	}

	public static void showMessage(String text) {
		getInstance().getBrowser().showMessage(text);
	}
	
	public static void showError(String text) {
		getInstance().getBrowser().showError(text);
	}
}
