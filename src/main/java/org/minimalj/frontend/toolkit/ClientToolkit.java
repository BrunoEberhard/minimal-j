package org.minimalj.frontend.toolkit;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

import org.minimalj.application.ApplicationContext;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.page.PageWithDetail;

/**
 * To provide a new kind of client you have to implement two things:
 * <OL>
 * <LI>This class</LI>
 * <LI>Some kind of Frontend. The Frontend should take an instance of Application and 
 * start the client. Take a look at the existing SwingFrontend, VaadinFrontend or LanternaFrontend. The trickiest part will be to implement
 * the PageContext.</LI>
 * </OL>
 *
 */
public abstract class ClientToolkit {

	private static ClientToolkit toolkit;
	
	public static ClientToolkit getToolkit() {
		if (toolkit == null) {
			throw new IllegalStateException("ClientToolkit has to be initialized");
		}
		return toolkit;
	}

	public static synchronized void setToolkit(ClientToolkit toolkit) {
		if (ClientToolkit.toolkit != null) {
			throw new IllegalStateException("ClientToolkit cannot be changed");
		}		
		if (toolkit == null) {
			throw new IllegalArgumentException("ClientToolkit cannot be null");
		}
		ClientToolkit.toolkit = toolkit;
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

	 // http://www.w3schools.com/html/html_form_input_types.asp 
	public enum InputType { FREE, EMAIL, URL, TEL, NUMBER; }

	public abstract IComponent createLabel(String string);
	public abstract IComponent createLabel(Action action);
	public abstract IComponent createTitle(String string);
	public abstract TextField createReadOnlyTextField();
	public abstract TextField createTextField(int maxLength, String allowedCharacters, InputType inputType, Search<String> autocomplete, InputComponentListener changeListener);
	public abstract TextField createAreaField(int maxLength, String allowedCharacters, InputComponentListener changeListener);
	public abstract IList createList(Action... actions);
	public abstract <T> Input<T> createComboBox(List<T> object, InputComponentListener changeListener);
	public abstract Input<Boolean> createCheckBox(InputComponentListener changeListener, String text);

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
	
	//
	
	public void show(Page detail, PageWithDetail ownerPage) {
		show(detail);
	}
	
	public abstract void show(Page page);

	public abstract void refresh();

	public abstract ApplicationContext getApplicationContext();
	
	//
	
	public abstract IDialog showDialog(String title, IContent content, Action closeAction, Action... actions);

	public abstract <T> IDialog showSearchDialog(Search<T> index, Object[] keys, TableActionListener<T> listener);

	//
	
	public abstract void showMessage(String text);
	
	public abstract void showError(String text);
	
	// Don't change enum orders. Needed by SwingClientToolkit
	public static enum ConfirmDialogType { YES_NO, YES_NO_CANCEL }
	public static enum ConfirmDialogResult { YES, NO, CANCEL }
	
	public abstract void showConfirmDialog(String message, String title, ConfirmDialogType type, DialogListener listener);
	
	public static interface DialogListener {
		
		void close(ConfirmDialogResult result);
	}

	// Up / Dowload
	
	/**
	 * Store the output of a stream locally on a place to select
	 * 
	 * @param buttonText the text displayed probably in a file browser
	 * @return the stream provided through which the local resource can be filled
	 */
	public abstract OutputStream store(String buttonText);

	/**
	 * Select a stream from a locally source
	 * 
	 * @param buttonText the text displayed probably in a file browser
	 * @return the stream provided by the selected local source
	 */
	public abstract InputStream load(String buttonText);
	
}
