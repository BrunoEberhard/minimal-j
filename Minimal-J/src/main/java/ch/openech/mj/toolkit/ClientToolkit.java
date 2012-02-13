package ch.openech.mj.toolkit;

import javax.swing.Action;

import ch.openech.mj.application.WindowConfig;
import ch.openech.mj.page.PageContext;
import ch.openech.mj.toolkit.TextField.TextFieldFilter;



/**
 * 
 * @author Bruno
 *
 */
public abstract class ClientToolkit {

	private static ClientToolkit toolkit;
	
	public static ClientToolkit getToolkit() {
		if (toolkit == null) {
			throw new IllegalStateException("CientToolkit has to be initialized");
		}
		return toolkit;
	}

	public static synchronized void setToolkit(ClientToolkit toolkit) {
		if (ClientToolkit.toolkit != null) {
			throw new IllegalStateException("CientToolkit cannot be changed");
		}		
		if (toolkit == null) {
			throw new IllegalArgumentException("CientToolkit cannot be null");
		}
		ClientToolkit.toolkit = toolkit;
	}

	// Widgets
	
	public abstract Object createEmptyComponent();
	
	public abstract Object createLabel(String string);
	
	public abstract Object createTitle(String string);

	public abstract TextField createTextField();

	public abstract TextField createTextField(int maxLength);
	
	public abstract TextField createTextField(TextFieldFilter filter);

	public abstract MultiLineTextField createMultiLineTextField();

	public abstract ComboBox createComboBox();
	
	public abstract CheckBox createCheckBox(String text);

	public abstract VisualList createVisualList();
	
	public abstract <T> VisualTable<T> createVisualTable(Class<T> clazz, Object[] fields);

	// Layouts
	
	public abstract HorizontalLayout createHorizontalLayout(Object... components);

	public abstract ContextLayout createContextLayout(Object content);
	
	public abstract VisibilityLayout createVisibilityLayout(Object content);
	
	public abstract SwitchLayout createSwitchLayout();
	
	public abstract GridFormLayout createGridLayout(int columns, int defaultSpan);

	public abstract Object createFormAlignLayout(Object content);

	public abstract Object createEditorLayout(String information, Object content, Action[] actions);

	// Notification
	
	public abstract void showMessage(Object component, String text);
	
	public abstract void showNotification(Object component, String text);

	public abstract void showError(Object component, String text);
	
	public abstract int showConfirmDialog(Object component, Object message, String title, int optionType);
	
	public abstract VisualDialog openDialog(Object parent, Object content, String title);
	        
	// Focus
	
	public abstract void focusFirstComponent(Object component);
	
	// TODO openPageContext ist ev in ClientToolkit am falschen Ort
	public abstract PageContext openPageContext(PageContext parentPageContext, WindowConfig windowConfig);
	
	public abstract PageContext openPageContext(PageContext parentPageContext);
}
