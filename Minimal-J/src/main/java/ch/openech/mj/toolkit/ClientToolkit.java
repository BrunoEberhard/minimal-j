package ch.openech.mj.toolkit;

import java.io.InputStream;

import ch.openech.mj.search.Search;
import ch.openech.mj.toolkit.ITable.TableActionListener;

/**
 * 
 * @author Bruno
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

	// Widgets
	
	public abstract IComponent createLabel(String string);
	
	public abstract IComponent createLabel(IAction action);
	
	public abstract IComponent createTitle(String string);

	public abstract TextField createReadOnlyTextField();

	public abstract TextField createTextField(InputComponentListener changeListener, int maxLength);
	
	public abstract TextField createTextField(InputComponentListener changeListener, int maxLength, String allowedCharacters);

	public abstract FlowField createFlowField();

	public abstract <T> ComboBox<T> createComboBox(InputComponentListener changeListener);
	
	public abstract CheckBox createCheckBox(InputComponentListener changeListener, String text);

	public abstract <T> ITable<T> createTable(Class<T> clazz, Object[] fields);

	public abstract IComponent createLink(String text, String address);
	
	public interface InputComponentListener {
		
	    void changed(IComponent source);

	}
	
	// Layouts
	
	public abstract Caption decorateWithCaption(IComponent component, String caption);
	
	public abstract HorizontalLayout createHorizontalLayout(IComponent... components);

	public abstract SwitchLayout createSwitchLayout();
	
	public abstract GridFormLayout createGridLayout(int columns, int columnWidth);

	public abstract IComponent createFormAlignLayout(IComponent content);

	public abstract <T> IDialog createSearchDialog(IComponent parent, Search<T> search, TableActionListener<T> listener);

	// Dialogs / Notification

	public abstract IDialog createDialog(IComponent parent, String title, IComponent content, IAction... actions);
	
	public abstract void showMessage(Object parent, String text);
	
	public abstract void showError(Object parent, String text);
	
	// Don't change order, is used in SwingClientToolkit
	public static enum ConfirmDialogType { YES_NO, YES_NO_CANCEL }
	
	public abstract void showConfirmDialog(IComponent component, String message, String title, ConfirmDialogType type, DialogListener listener);
	
	public static interface DialogListener {
		
		// Don't change order, is used in SwingClientToolkit
		public enum DialogResult { YES, NO, CANCEL }
		
		void close(Object result);
	}
	
	// Focus
	
//	public abstract void focusFirstComponent(IComponent component);
	
	// Up / Dowload
	
	public abstract void export(IComponent parent, String buttonText, ExportHandler exportHandler);

	public abstract InputStream imprt(IComponent parent, String buttonText);
	
}
