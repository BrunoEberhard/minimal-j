package ch.openech.mj.swing.toolkit;

import java.awt.Component;
import java.awt.FocusTraversalPolicy;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

import javax.swing.Action;
import javax.swing.FocusManager;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import ch.openech.mj.application.EditablePanel;
import ch.openech.mj.application.WindowConfig;
import ch.openech.mj.page.PageContext;
import ch.openech.mj.swing.SwingFrame;
import ch.openech.mj.swing.component.BubbleMessageSupport;
import ch.openech.mj.toolkit.CheckBox;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.ComboBox;
import ch.openech.mj.toolkit.ContextLayout;
import ch.openech.mj.toolkit.GridFormLayout;
import ch.openech.mj.toolkit.HorizontalLayout;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.IComponentDelegate;
import ch.openech.mj.toolkit.MultiLineTextField;
import ch.openech.mj.toolkit.SwitchLayout;
import ch.openech.mj.toolkit.TextField;
import ch.openech.mj.toolkit.VisibilityLayout;
import ch.openech.mj.toolkit.VisualDialog;
import ch.openech.mj.toolkit.VisualList;
import ch.openech.mj.toolkit.VisualTable;
import ch.openech.mj.toolkit.TextField.TextFieldFilter;

public class SwingClientToolkit extends ClientToolkit {

	public static Component getComponent(IComponent component) {
		if (component instanceof IComponentDelegate) {
			IComponentDelegate delegate = (IComponentDelegate) component;
			return (Component) delegate.getComponent();
		} else {
			return (Component) component;
		}
	}
	
	@Override
	public IComponent createEmptyComponent() {
		return new SwingComponentDelegate(new JPanel());
	}
	
	@Override
	public IComponent createLabel(String string) {
		return new SwingComponentDelegate(new JLabel(string));
	}

	@Override
	public IComponent createTitle(String string) {
		JLabel label = new JLabel(string);
		label.setFont(label.getFont().deriveFont(Font.BOLD));
		return new SwingComponentDelegate(label);
	}
	
	@Override
	public TextField createTextField() {
		return new SwingTextField();
	}
	
	@Override
	public TextField createTextField(int maxLength) {
		return new SwingTextField(maxLength);
	}
	
	@Override
	public TextField createTextField(TextFieldFilter filter) {
		return new SwingTextField(filter);
	}

	@Override
	public MultiLineTextField createMultiLineTextField() {
		return new SwingMultiLineTextField();
	}

	@Override
	public ComboBox createComboBox() {
		return new SwingComboBox();
	}

	@Override
	public CheckBox createCheckBox(String text) {
		return new SwingCheckBox(text);
	}

	@Override
	public HorizontalLayout createHorizontalLayout(IComponent... components) {
		return new SwingHorizontalLayout(components);
	}

	@Override
	public SwitchLayout createSwitchLayout() {
		return new SwingSwitchLayout();
	}


	@Override
	public ContextLayout createContextLayout(IComponent content) {
		return new SwingContextLayout(content);
	}
	
	@Override
	public VisibilityLayout createVisibilityLayout(IComponent content) {
		return new SwingVisibilityLayout(content);
	}

	@Override
	public VisualList createVisualList() {
		return new SwingVisualList();
	}

	@Override
	public GridFormLayout createGridLayout(int columns, int defaultSpan) {
		return new SwingGridFormLayout(columns, defaultSpan);
	}

	@Override
	public IComponent createFormAlignLayout(IComponent content) {
		JPanel panel = new JPanel(new SwingFormAlignLayoutManager());
		Component component = getComponent(content);
		panel.add(component);
		return new SwingComponentDelegate(panel);
	}

	@Override
	public void showNotification(IComponent c, String text) {
		try {
			JComponent component = (JComponent) getComponent(c);
			BubbleMessageSupport.showBubble(component, text);
		} catch (Exception x) {
			// TODO
			x.printStackTrace();
		}
	}

	@Override
	public void focusFirstComponent(IComponent object) {
		Component component = SwingClientToolkit.getComponent(object);
		if (component instanceof JComponent) {
			JComponent jComponent = (JComponent) component;
			if (component.isShowing()) {
				focusFirstComponentNow(jComponent);
			} else {
				focusFirstComponentLater(jComponent, object);
			}
		}
	}

	private void focusFirstComponentNow(JComponent component) {
		FocusTraversalPolicy focusPolicy = component.getFocusTraversalPolicy();
		if (focusPolicy != null && focusPolicy.getFirstComponent(component) != null) {
			focusPolicy.getFirstComponent(component).requestFocus();
		} else {
			FocusManager.getCurrentManager().focusNextComponent(component);
		}
	}

	private void focusFirstComponentLater(final JComponent component, final IComponent object) {
		component.addHierarchyListener(new HierarchyListener() {
			@Override
			public void hierarchyChanged(HierarchyEvent e) {
				component.removeHierarchyListener(this);
				focusFirstComponent(object);
			}
		});
	}

	@Override
	public void showMessage(IComponent component, String text) {
		JOptionPane.showMessageDialog(getComponent(component), text, "Information", JOptionPane.INFORMATION_MESSAGE); 
	}
	
	@Override
	public void showError(IComponent component, String text) {
		JOptionPane.showMessageDialog(getComponent(component), text, "Fehler", JOptionPane.ERROR_MESSAGE); 
	}

	@Override
	public int showConfirmDialog(IComponent c, Object message, String title, int optionType) {
		JComponent parentComponent = (JComponent) getComponent(c);
		return JOptionPane.showConfirmDialog(parentComponent, message, title, optionType);
	}

	@Override
	public <T> VisualTable<T> createVisualTable(Class<T> clazz, Object[] fields) {
		return new SwingVisualTable<T>(clazz, fields);
	}

	@Override
	public VisualDialog openDialog(IComponent parent, IComponent content, String title) {
		Component parentComponent = getComponent(parent);
		Component contentComponent = getComponent(content);
		
		EditablePanel editablePanel = EditablePanel.getEditablePanel(parentComponent);
		
		if (editablePanel != null) {
			SwingInternalFrame internalFrame = new SwingInternalFrame(editablePanel, contentComponent, title);
			editablePanel.openModalDialog(internalFrame);
			return internalFrame;
		} else {
			Window window = SwingUtilities.getWindowAncestor(parentComponent);
			return new SwingEditorDialog(window, contentComponent, title);
		}		
	}

	@Override
	public IComponent createEditorLayout(String information, IComponent content, Action[] actions) {
		return new SwingEditorLayout(information, content, actions);
	}

	@Override
	public PageContext openPageContext(PageContext parentPageContext) {
		SwingFrame swingFrame = (SwingFrame) parentPageContext;
		return swingFrame.addTab();
	}
	
	@Override
	public PageContext openPageContext(PageContext parentPageContext, WindowConfig windowConfig) {
		SwingFrame frame = new SwingFrame(windowConfig);
		return frame.getVisiblePageContext();
	}
	
}
