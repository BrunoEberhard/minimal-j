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
	
	public abstract IComponent createEmptyComponent();
	
	public abstract IComponent createLabel(String string);
	
	public abstract IComponent createTitle(String string);

	public abstract TextField createTextField();

	public abstract TextField createTextField(int maxLength);
	
	public abstract TextField createTextField(TextFieldFilter filter);

	public abstract MultiLineTextField createMultiLineTextField();

	public abstract ComboBox createComboBox();
	
	public abstract CheckBox createCheckBox(String text);

	public abstract VisualList createVisualList();
	
	public abstract <T> VisualTable<T> createVisualTable(Class<T> clazz, Object[] fields);

	// Layouts
	
	public abstract HorizontalLayout createHorizontalLayout(IComponent... components);

	public abstract ContextLayout createContextLayout(IComponent content);
	
	public abstract VisibilityLayout createVisibilityLayout(IComponent content);
	
	public abstract SwitchLayout createSwitchLayout();
	
	public abstract GridFormLayout createGridLayout(int columns, int defaultSpan);

	public abstract IComponent createFormAlignLayout(IComponent content);

	public abstract IComponent createEditorLayout(String information, IComponent content, Action[] actions);

	// Notification
	
	public abstract void showMessage(IComponent component, String text);
	
	public abstract void showNotification(IComponent component, String text);

	public abstract void showError(IComponent component, String text);
	
	public abstract int showConfirmDialog(IComponent component, Object message, String title, int optionType);
	
	public abstract VisualDialog openDialog(IComponent parent, IComponent content, String title);
	        
	// Focus
	
	public abstract void focusFirstComponent(IComponent component);
	
	// TODO openPageContext ist ev in ClientToolkit am falschen Ort
	public abstract PageContext openPageContext(PageContext parentPageContext, WindowConfig windowConfig);
	
	public abstract PageContext openPageContext(PageContext parentPageContext);
}
