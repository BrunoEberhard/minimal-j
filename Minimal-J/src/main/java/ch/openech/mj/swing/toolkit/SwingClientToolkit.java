package ch.openech.mj.swing.toolkit;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.FocusTraversalPolicy;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.FocusManager;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.text.JTextComponent;

import ch.openech.mj.swing.component.EditablePanel;
import ch.openech.mj.swing.component.SwingCaption;
import ch.openech.mj.toolkit.Caption;
import ch.openech.mj.toolkit.CheckBox;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.ComboBox;
import ch.openech.mj.toolkit.ConfirmDialogListener;
import ch.openech.mj.toolkit.ExportHandler;
import ch.openech.mj.toolkit.FlowField;
import ch.openech.mj.toolkit.GridFormLayout;
import ch.openech.mj.toolkit.HorizontalLayout;
import ch.openech.mj.toolkit.IAction;
import ch.openech.mj.toolkit.IAction.ActionChangeListener;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.IDialog;
import ch.openech.mj.toolkit.ILink;
import ch.openech.mj.toolkit.ITable;
import ch.openech.mj.toolkit.ProgressListener;
import ch.openech.mj.toolkit.SwitchLayout;
import ch.openech.mj.toolkit.TextField;

public class SwingClientToolkit extends ClientToolkit {

	@Override
	public IComponent createLabel(String string) {
		return new SwingLabel(string);
	}
	
	@Override
	public IComponent createLabel(IAction action) {
		return new SwingActionLabel(action);
	}

	private static class SwingActionLabel extends JLabel implements IComponent {
		private static final long serialVersionUID = 1L;

		public SwingActionLabel(final IAction action) {
//			setIcon((Icon) action.getValue(Action.SMALL_ICON));
			setText(action.getName());
//			label.setToolTipText(Resources.getResourceBundle().getString(runnable.getClass().getSimpleName() + ".description"));
			
			setForeground(Color.BLUE);
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					action.action(SwingActionLabel.this);
				}
			});
		}
		
	}
	
	@Override
	public IComponent createTitle(String string) {
		return new SwingTitle(string);

	}

	@Override
	public TextField createReadOnlyTextField() {
		return new SwingReadOnlyTextField();
	}

	@Override
	public TextField createTextField(InputComponentListener changeListener, int maxLength) {
		return new SwingTextField(changeListener, maxLength);
	}

	@Override
	public TextField createTextField(InputComponentListener changeListener, int maxLength, String allowedCharacters) {
		return new SwingTextField(changeListener, maxLength, allowedCharacters);
	}

	@Override
	public FlowField createFlowField() {
		return new SwingFlowField();
	}

	@Override
	public <T> ComboBox<T> createComboBox(InputComponentListener changeListener) {
		return new SwingComboBox<T>(changeListener);
	}

	@Override
	public CheckBox createCheckBox(InputComponentListener changeListener, String text) {
		return new SwingCheckBox(changeListener, text);
	}

	@Override
	public Caption decorateWithCaption(IComponent component, String caption) {
		return new SwingCaption((Component)component, caption);
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
		Component component = (Component)content;
		panel.add(component);

		return new SwingScrollPane(new ScrollablePanel(panel));
	}

	public static void focusFirstComponent(IComponent object) {
		if (object instanceof JComponent) {
			JComponent jComponent = (JComponent) object;
			if (jComponent.isShowing()) {
				focusFirstComponentNow(jComponent);
			} else {
				focusFirstComponentLater(jComponent, object);
			}
		}
	}

	private static void focusFirstComponentNow(JComponent component) {
		FocusTraversalPolicy focusPolicy = component.getFocusTraversalPolicy();
		if (component instanceof JTextComponent || component instanceof JComboBox || component instanceof JCheckBox) {
			component.requestFocus();
		} else if (focusPolicy != null && focusPolicy.getFirstComponent(component) != null) {
			focusPolicy.getFirstComponent(component).requestFocus();
		} else {
			FocusManager.getCurrentManager().focusNextComponent(component);
		}
	}

	private static void focusFirstComponentLater(final JComponent component, final IComponent object) {
		component.addHierarchyListener(new HierarchyListener() {
			@Override
			public void hierarchyChanged(HierarchyEvent e) {
				component.removeHierarchyListener(this);
				focusFirstComponent(object);
			}
		});
	}

	@Override
	public void showMessage(Object parent, String text) {
		Window window = findWindow((Component)parent);
		JOptionPane.showMessageDialog(window, text, "Information", JOptionPane.INFORMATION_MESSAGE);
	}

	@Override
	public void showError(Object parent, String text) {
		Window window = findWindow((Component)parent);
		JOptionPane.showMessageDialog(window, text, "Fehler", JOptionPane.ERROR_MESSAGE);
	}

	@Override
	public void showConfirmDialog(IComponent c, String message, String title, int optionType,
			ConfirmDialogListener listener) {
		Component parentComponent = (Component)c;
		int result = JOptionPane.showConfirmDialog(parentComponent, message, title, optionType);
		listener.onClose(result);
	}

	@Override
	public <T> ITable<T> createTable(Class<T> clazz, Object[] fields) {
		return new SwingTable<T>(clazz, fields);
	}

	public static ProgressListener showProgress(Object parent, String text) {
		EditablePanel editablePanel = EditablePanel.getEditablePanel((Component)parent);
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
	public IDialog createDialog(IComponent parent, String title, IComponent content, IAction... actions) {
		Window window = findWindow((Component) parent);
		// TODO check for OS or move this to UI
		JComponent contentComponent = new SwingEditorLayout(content, actions);
		contentComponent.setBorder(BorderFactory.createLineBorder(Color.BLACK));

		EditablePanel editablePanel = EditablePanel.getEditablePanel((Component) parent);

		if (editablePanel != null) {
			return new SwingInternalFrame(editablePanel, contentComponent, title);
		} else {
			return new SwingEditorDialog(window, contentComponent, title);
		}
	}

	public static Window findWindow(Component parentComponent) {
		while (parentComponent != null && !(parentComponent instanceof Window)) {
			if (parentComponent instanceof JPopupMenu) {
				parentComponent = ((JPopupMenu) parentComponent).getInvoker();
			} else {
				parentComponent = parentComponent.getParent();
			}
		}
		return (Window) parentComponent;
	}

	public static Component createSearchLayout(TextField text, Action searchAction, IComponent content, Action... actions) {
		return new SwingSearchLayout(text, searchAction, content, actions);
	}

	@Override
	public void export(IComponent parent, String buttonText, ExportHandler exportHandler) {
		JFileChooser chooser = new JFileChooser();
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		if (JFileChooser.APPROVE_OPTION == chooser.showDialog((Component) parent, buttonText)) {
			File outputFile = chooser.getSelectedFile();
			try {
				exportHandler.export(new FileOutputStream(outputFile));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public InputStream imprt(IComponent parent, String buttonText) {
		JFileChooser chooser = new JFileChooser();
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		if (JFileChooser.APPROVE_OPTION == chooser.showDialog((Component) parent, buttonText)) {
			File inputFile = chooser.getSelectedFile();
			try {
				return new FileInputStream(inputFile);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return null;
			}
		} else {
			return null;
		}
	}

	public static boolean verticallyGrowing(Component component) {
		if (component instanceof SwingFlowField || component instanceof JTable) {
			return true;
		}
		if (component instanceof Container) {
			Container container = (Container) component;
			for (Component c : container.getComponents()) {
				if (verticallyGrowing(c)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public ILink createLink(String text, String address) {
        return new SwingLink(text, address);
	}
	
	public static class SwingLink extends JLabel implements ILink {
		private static final long serialVersionUID = 1L;
		private final String address;
		private MouseListener mouseListener;
		
		public SwingLink(String text, String address) {
			super(text);
			this.address = address;
			setForeground(Color.BLUE);
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					mouseListener.mouseClicked(e);
				}
			});
		}

		public String getAddress() {
			return address;
		}

		public void setMouseListener(MouseListener mouseListener) {
			this.mouseListener = mouseListener;
		}
	}

	public static Action[] adaptActions(IAction[] actions, IComponent context) {
		Action[] swingActions = new Action[actions.length];
		for (int i = 0; i<actions.length; i++) {
			swingActions[i] = adaptAction(actions[i], context);
		}
		return swingActions;
	}

	public static Action adaptAction(final IAction action, final IComponent context) {
		final Action swingAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				action.action(context);
			}
		};
		action.setChangeListener(new ActionChangeListener() {
			{
				update();
			}
			
			@Override
			public void change() {
				update();
			}

			protected void update() {
				swingAction.putValue(Action.NAME, action.getName());
				swingAction.putValue(Action.LONG_DESCRIPTION, action.getDescription());
				swingAction.setEnabled(action.isEnabled());
			}
		});
		return swingAction;
	}
	
	// @Override
	// public InputStream importField(Object parent, String buttonText) {
	// JFileChooser chooser = new JFileChooser();
	// chooser.setMultiSelectionEnabled(false);
	// chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
	// if (JFileChooser.APPROVE_OPTION == chooser.showDialog(null, buttonText))
	// {
	// File inputFile = chooser.getSelectedFile();
	// try {
	// return new FileInputStream(inputFile);
	// } catch (FileNotFoundException e) {
	// e.printStackTrace();
	// return null;
	// }
	// } else {
	// return null;
	// }
	// }

}
