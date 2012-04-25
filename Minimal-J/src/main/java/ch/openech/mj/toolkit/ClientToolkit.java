package ch.openech.mj.toolkit;

import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.Action;
import javax.swing.event.ChangeListener;

import ch.openech.mj.page.PageContext;
import ch.openech.mj.toolkit.TextField.TextFieldFilter;
import ch.openech.mj.util.ProgressListener;



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
	
	public abstract IComponent createTitle(String string);

	public abstract TextField createReadOnlyTextField();

	public abstract TextField createTextField(ChangeListener changeListener, int maxLength);
	
	public abstract TextField createTextField(ChangeListener changeListener, TextFieldFilter filter);

	public abstract FlowField createFlowField();

	public abstract <T> ComboBox<T> createComboBox(ChangeListener changeListener);
	
	public abstract CheckBox createCheckBox(ChangeListener changeListener, String text);

	public abstract <T> VisualTable<T> createVisualTable(Class<T> clazz, Object[] fields);

	// Layouts
	
	public abstract IComponent decorateWithCaption(IComponent component, String caption);
	
	public abstract HorizontalLayout createHorizontalLayout(IComponent... components);

	public abstract SwitchLayout createSwitchLayout();
	
	public abstract GridFormLayout createGridLayout(int columns, int columnWidth);

	public abstract IComponent createFormAlignLayout(IComponent content);

	public abstract IComponent createEditorLayout(String information, IComponent content, Action[] actions);

	public abstract IComponent createSearchLayout(TextField text, Action searchAction, IComponent content, Action... actions);

	// Notification
	
	public abstract void showMessage(Object parent, String text);
	
	public abstract void showNotification(IComponent component, String text);

	public abstract void showError(Object parent, String text);
	
	public abstract void showConfirmDialog(IComponent component, String message, String title, int type, ConfirmDialogListener listener);
	
	public abstract ProgressListener showProgress(Object parent, String text);
	
	public abstract VisualDialog openDialog(Object parent, IComponent content, String title);
	        
	// Focus
	
	public abstract void focusFirstComponent(IComponent component);
	
	public abstract PageContext findPageContext(Object source);
	
	// Up / Dowload
	
	public abstract OutputStream export(Object parent, String buttonText);

	public abstract InputStream imprt(Object parent, String buttonText);

}
