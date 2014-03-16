package ch.openech.mj.toolkit;

import java.io.InputStream;

import ch.openech.mj.model.Search;
import ch.openech.mj.toolkit.ITable.TableActionListener;

/**
 * To provide a new kind of client you have to implement two things:
 * <OL>
 * <LI>This class</LI>
 * <LI>Some kind of launcher. The launcher should take an instance of MjApplication and 
 * start the client. Take a look at the existing SwingLauncher, VaadinLauncher oder LanternaLauncher. The trickiest part will be to implement
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

	public abstract <T> ITable<T> createTable(Object[] fields);

	public abstract IComponent createLink(String text, String address);
	
	public interface InputComponentListener {
		
	    void changed(IComponent source);

	}
	
	public abstract <T> ILookup<T> createLookup(InputComponentListener changeListener, Search<T> index, Object[] keys);
	
	public interface ILookup<S> extends IComponent {
		
		void setText(String string);
		
		S getSelectedObject();
		
	}
	
	// Layouts
	
	public abstract Caption decorateWithCaption(IComponent component, String caption);
	
	public abstract HorizontalLayout createHorizontalLayout(IComponent... components);

	public abstract SwitchLayout createSwitchLayout();
	
	public abstract GridFormLayout createGridLayout(int columns, int columnWidth);

	public abstract IComponent createFormAlignLayout(IComponent content);

	public abstract <T> IDialog createSearchDialog(IComponent parent, Search<T> index, Object[] keys, TableActionListener<T> listener);

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

	// Up / Dowload
	
	public abstract void export(IComponent parent, String buttonText, ExportHandler exportHandler);

	public abstract InputStream imprt(IComponent parent, String buttonText);
	
}
