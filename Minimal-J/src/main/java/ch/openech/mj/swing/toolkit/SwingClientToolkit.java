package ch.openech.mj.swing.toolkit;

import java.awt.Color;
import java.awt.Component;
import java.awt.FocusTraversalPolicy;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.FocusManager;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeListener;

import ch.openech.mj.application.EditablePanel;
import ch.openech.mj.page.PageContext;
import ch.openech.mj.swing.SwingFrame;
import ch.openech.mj.swing.component.BubbleMessageSupport;
import ch.openech.mj.swing.component.SwingCaption;
import ch.openech.mj.toolkit.CheckBox;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.ComboBox;
import ch.openech.mj.toolkit.ConfirmDialogListener;
import ch.openech.mj.toolkit.FlowField;
import ch.openech.mj.toolkit.GridFormLayout;
import ch.openech.mj.toolkit.HorizontalLayout;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.IComponentDelegate;
import ch.openech.mj.toolkit.SwitchLayout;
import ch.openech.mj.toolkit.TextField;
import ch.openech.mj.toolkit.TextField.TextFieldFilter;
import ch.openech.mj.toolkit.VisualDialog;
import ch.openech.mj.toolkit.VisualTable;
import ch.openech.mj.util.ProgressListener;

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
		label.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
		label.setFont(label.getFont().deriveFont(Font.BOLD));
		return new SwingComponentDelegate(label);
	}
	
	@Override
	public TextField createReadOnlyTextField() {
		return new SwingReadOnlyTextField();
	}
	
	@Override
	public TextField createTextField(ChangeListener changeListener, int maxLength) {
		return new SwingTextField(changeListener, maxLength);
	}
	
	@Override
	public TextField createTextField(ChangeListener changeListener, TextFieldFilter filter) {
		return new SwingTextField(changeListener, filter);
	}

	@Override
	public FlowField createFlowField(boolean vertical) {
		return new SwingFlowField(vertical);
	}

	@Override
	public <T> ComboBox<T> createComboBox(ChangeListener changeListener) {
		return new SwingComboBox<T>(changeListener);
	}

	@Override
	public CheckBox createCheckBox(ChangeListener changeListener, String text) {
		return new SwingCheckBox(changeListener, text);
	}

	@Override
	public IComponent decorateWithCaption(IComponent component, String caption) {
		return new SwingCaption(getComponent(component), caption);
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
	public GridFormLayout createGridLayout(int columns, int columnWidthPercentage) {
		return new SwingGridFormLayout(columns, columnWidthPercentage);
	}

	@Override
	public IComponent createFormAlignLayout(IComponent content) {
		JPanel panel = new JPanel(new SwingFormAlignLayoutManager());
		Component component = getComponent(content);
		panel.add(component);
		
		JScrollPane scrollPane = new JScrollPane(new ScrollablePanel(panel));
		scrollPane.setBorder(null);
		return new SwingComponentDelegate(scrollPane);
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
	public void showConfirmDialog(IComponent c, String message, String title, int optionType, ConfirmDialogListener listener) {
		JComponent parentComponent = (JComponent) getComponent(c);
		int result = JOptionPane.showConfirmDialog(parentComponent, message, title, optionType);
		listener.onClose(result);
	}

	@Override
	public <T> VisualTable<T> createVisualTable(Class<T> clazz, Object[] fields) {
		return new SwingVisualTable<T>(clazz, fields);
	}
	
	@Override
	public ProgressListener showProgress(Object parent, String text) {
		EditablePanel editablePanel = EditablePanel.getEditablePanel((Component) parent);
		if (editablePanel != null) {
			SwingProgressInternalFrame frame = new SwingProgressInternalFrame(text);
			editablePanel.openModalDialog(frame);
			return frame;
		} else {
			Window window = findWindow((Component) parent);
			SwingProgressDialog dialog = new SwingProgressDialog(window, text);
			dialog.setVisible(true);
			return dialog;
		}		
	}

	@Override
	public VisualDialog openDialog(Object parent, IComponent content, String title) {
		Window window = findWindow((Component) parent);
		Component contentComponent = getComponent(content);
		// TODO check for OS or move this to UI
		((JComponent) contentComponent).setBorder(BorderFactory.createLineBorder(Color.BLACK));
		
		EditablePanel editablePanel = EditablePanel.getEditablePanel((Component) parent);
		
		if (editablePanel != null) {
			return new SwingInternalFrame(editablePanel, contentComponent, title);
		} else {
			return new SwingEditorDialog(window, contentComponent, title);
		}		
	}

	private Window findWindow(Component parentComponent) {
		while (parentComponent !=null && !(parentComponent instanceof Window)) {
			if (parentComponent instanceof JPopupMenu) {
				parentComponent = ((JPopupMenu) parentComponent).getInvoker();
			} else {
				parentComponent = parentComponent.getParent();
			}
		}
		return (Window) parentComponent;
	}

	@Override
	public IComponent createEditorLayout(String information, IComponent content, Action[] actions) {
		return new SwingEditorLayout(information, content, actions);
	}

	@Override
	public IComponent createSearchLayout(TextField text, Action searchAction, IComponent content, Action... actions) {
		return new SwingSearchLayout(text, searchAction, content, actions);
	}
	
	@Override
	public PageContext openPageContext(PageContext parentPageContext, boolean newWindow) {
		if (newWindow) {
			SwingFrame frame = new SwingFrame();
			return frame.getVisiblePageContext();
		} else {
			SwingFrame swingFrame = (SwingFrame) parentPageContext;
			return swingFrame.addTab();
		}
	}

	@Override
	public PageContext findPageContext(Object source) {
		if (source instanceof IComponent) {
			source = getComponent((IComponent)source);
		}
		Component c = (Component) source;
		while (!(c instanceof PageContext) && c != null) {
			if (c instanceof JPopupMenu) {
				JPopupMenu popupMenu = (JPopupMenu) c;
				c = popupMenu.getInvoker();
			} else if (c instanceof SwingFrame) {
				SwingFrame frame = (SwingFrame) c;
				c = frame.getVisiblePageContext();
			} else {
				c = c.getParent();
			}
		}
		return (PageContext) c;
	}
	
}
