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
import ch.openech.mj.toolkit.MultiLineTextField;
import ch.openech.mj.toolkit.SwitchLayout;
import ch.openech.mj.toolkit.TextField;
import ch.openech.mj.toolkit.VisibilityLayout;
import ch.openech.mj.toolkit.VisualDialog;
import ch.openech.mj.toolkit.VisualList;
import ch.openech.mj.toolkit.VisualTable;
import ch.openech.mj.toolkit.TextField.TextFieldFilter;

public class SwingClientToolkit extends ClientToolkit {

	@Override
	public Object createEmptyComponent() {
		return new JPanel();
	}
	
	@Override
	public Object createLabel(String string) {
		return new JLabel(string);
	}

	@Override
	public Object createTitle(String string) {
		JLabel label = new JLabel(string);
		label.setFont(label.getFont().deriveFont(Font.BOLD));
		return label;
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
	public HorizontalLayout createHorizontalLayout(Object... components) {
		return new SwingHorizontalLayout(components);
	}

	@Override
	public SwitchLayout createSwitchLayout() {
		return new SwingSwitchLayout();
	}


	@Override
	public ContextLayout createContextLayout(Object content) {
		return new SwingContextLayout(content);
	}
	
	@Override
	public VisibilityLayout createVisibilityLayout(Object content) {
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
	public Object createFormAlignLayout(Object content) {
		JPanel panel = new JPanel(new SwingFormAlignLayoutManager());
		panel.add((Component) content);
		return panel;
	}

	@Override
	public void showNotification(Object component, String text) {
		try {
			BubbleMessageSupport.showBubble((JComponent) component, text);
		} catch (Exception x) {
			// TODO
			x.printStackTrace();
		}
	}

	@Override
	public void focusFirstComponent(Object object) {
		JComponent component = (JComponent) object;
		if (component.isShowing()) {
			focusFirstComponentNow(component);
		} else {
			focusFirstComponentLater(component);
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

	private void focusFirstComponentLater(final JComponent component) {
		component.addHierarchyListener(new HierarchyListener() {
			@Override
			public void hierarchyChanged(HierarchyEvent e) {
				component.removeHierarchyListener(this);
				focusFirstComponent(component);
			}
		});
	}

	@Override
	public void showMessage(Object component, String text) {
		JOptionPane.showMessageDialog((Component) component, text, "Information", JOptionPane.INFORMATION_MESSAGE); 
	}
	
	@Override
	public void showError(Object component, String text) {
		JOptionPane.showMessageDialog((Component) component, text, "Fehler", JOptionPane.ERROR_MESSAGE); 
	}

	@Override
	public int showConfirmDialog(Object c, Object message, String title, int optionType) {
		JComponent parentComponent = (JComponent) c;
		return JOptionPane.showConfirmDialog(parentComponent, message, title, optionType);
	}

	@Override
	public <T> VisualTable<T> createVisualTable(Class<T> clazz, Object[] fields) {
		return new SwingVisualTable<T>(clazz, fields);
	}

	@Override
	public VisualDialog openDialog(Object parent, Object content, String title) {
		Component parentComponent = (Component) parent;
		Component contentComponent = (Component) content;
		
		EditablePanel editablePanel = EditablePanel.getEditablePanel((Component) parentComponent);
		
		if (editablePanel != null) {
			SwingInternalFrame internalFrame = new SwingInternalFrame(editablePanel, contentComponent, title);
			editablePanel.openModalDialog(internalFrame);
			return internalFrame;
		} else {
			Window window = SwingUtilities.getWindowAncestor((Component) parentComponent);
			return new SwingEditorDialog(window, contentComponent, title);
		}		
	}

	@Override
	public Object createEditorLayout(String information, Object content, Action[] actions) {
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
