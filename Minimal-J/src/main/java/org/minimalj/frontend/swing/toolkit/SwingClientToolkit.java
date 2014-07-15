package org.minimalj.frontend.swing.toolkit;

import java.awt.BorderLayout;
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
import java.io.OutputStream;
import java.util.List;

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

import org.minimalj.frontend.swing.component.EditablePanel;
import org.minimalj.frontend.swing.component.SwingCaption;
import org.minimalj.frontend.toolkit.Caption;
import org.minimalj.frontend.toolkit.CheckBox;
import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.frontend.toolkit.ClientToolkit.DialogListener.DialogResult;
import org.minimalj.frontend.toolkit.ComboBox;
import org.minimalj.frontend.toolkit.FlowField;
import org.minimalj.frontend.toolkit.GridContent;
import org.minimalj.frontend.toolkit.HorizontalLayout;
import org.minimalj.frontend.toolkit.IAction;
import org.minimalj.frontend.toolkit.IAction.ActionChangeListener;
import org.minimalj.frontend.toolkit.IDialog;
import org.minimalj.frontend.toolkit.ILink;
import org.minimalj.frontend.toolkit.ITable;
import org.minimalj.frontend.toolkit.ITable.TableActionListener;
import org.minimalj.frontend.toolkit.ProgressListener;
import org.minimalj.frontend.toolkit.SwitchComponent;
import org.minimalj.frontend.toolkit.TextField;
import org.minimalj.util.StringUtils;

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
					action.action(findContext(SwingActionLabel.this));
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
	public SwitchContent createSwitchContent() {
		return new SwingSwitchContent();
	}

	@Override
	public SwitchComponent createSwitchComponent(IComponent... components) {
		return new SwingSwitchComponent(components);
	}

	@Override
	public GridContent createGridContent(int columns, int columnWidthPercentage) {
		return new SwingGridFormLayout(columns, columnWidthPercentage);
	}

	public static void focusFirstComponent(JComponent jComponent) {
		if (jComponent.isShowing()) {
			focusFirstComponentNow(jComponent);
		} else {
			focusFirstComponentLater(jComponent);
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

	private static void focusFirstComponentLater(final JComponent component) {
		component.addHierarchyListener(new HierarchyListener() {
			@Override
			public void hierarchyChanged(HierarchyEvent e) {
				component.removeHierarchyListener(this);
				focusFirstComponent(component);
			}
		});
	}

	@Override
	public void showMessage(IContext context, String text) {
		Window window = findWindow((Component)context);
		JOptionPane.showMessageDialog(window, text, "Information", JOptionPane.INFORMATION_MESSAGE);
	}

	@Override
	public void showError(IContext context, String text) {
		Window window = findWindow((Component)context);
		JOptionPane.showMessageDialog(window, text, "Fehler", JOptionPane.ERROR_MESSAGE);
	}

	@Override
	public void showConfirmDialog(IDialog c, String message, String title, ConfirmDialogType type,
			DialogListener listener) {
		Component parentComponent = (Component)c;
		int optionType = type.ordinal();
		int result = JOptionPane.showConfirmDialog(parentComponent, message, title, optionType);
		listener.close(DialogResult.values()[result]);
	}

	@Override
	public <T> ITable<T> createTable(Object[] fields) {
		return new SwingTable<T>(fields);
	}

	public static ProgressListener showProgress(Component parent, String text) {
		EditablePanel editablePanel = EditablePanel.getEditablePanel(parent);
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
	public IDialog createDialog(IContext context, String title, IContent content, IAction... actions) {
		JComponent contentComponent = new SwingEditorLayout(context, content, actions);
		// TODO check for OS or move this to UI
		contentComponent.setBorder(BorderFactory.createLineBorder(Color.BLACK));

		return createDialog(context, title, contentComponent);
	}

	private IDialog createDialog(IContext context, String title, JComponent content) {
		EditablePanel editablePanel = EditablePanel.getEditablePanel((Component) context);
		if (editablePanel != null) {
			return new SwingInternalFrame(editablePanel, content, title);
		} else {
			Window window = findWindow((Component) context);
			return new SwingEditorDialog(window, content, title);
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
	
	public static IContext findContext(Component parentComponent) {
		while (parentComponent != null && !(parentComponent instanceof IContext)) {
			if (parentComponent instanceof JPopupMenu) {
				parentComponent = ((JPopupMenu) parentComponent).getInvoker();
			} else {
				parentComponent = parentComponent.getParent();
			}
		}
		return (IContext) parentComponent;
	}
	
	@Override
	public <T> ILookup<T> createLookup(InputComponentListener changeListener, Search<T> index, Object[] keys) {
		return new SwingLookup<T>(changeListener, index, keys);
	}
	
	private static class SwingLookup<T> extends JPanel implements ILookup<T> {
		private static final long serialVersionUID = 1L;
		
		private final InputComponentListener changeListener;
		private final Search<T> search;
		private final Object[] keys;
		private final SwingLookupLabel actionLabel;
		private IDialog dialog;
		private T selectedObject;
		
		public SwingLookup(InputComponentListener changeListener, Search<T> search, Object[] keys) {
			super(new BorderLayout());
			
			this.changeListener = changeListener;
			this.search = search;
			this.keys = keys;
			
			this.actionLabel = new SwingLookupLabel();
			add(actionLabel, BorderLayout.CENTER);
			add(new SwingRemoveLabel(), BorderLayout.LINE_END);
		}

		@Override
		public void setText(String text) {
			if (!StringUtils.isBlank(text)) {
				actionLabel.setText(text);
			} else {
				actionLabel.setText("[+]");
			}
		}

		@Override
		public T getSelectedObject() {
			return selectedObject;
		}
		
		private class SwingLookupLabel extends JLabel {
			private static final long serialVersionUID = 1L;

			public SwingLookupLabel() {
				setForeground(Color.BLUE);
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						dialog = ((SwingClientToolkit) ClientToolkit.getToolkit()).createSearchDialog(findContext(SwingLookup.this), search, keys, new LookupClickListener());
						dialog.openDialog();
					}
				});
			}
		}
		
		private class SwingRemoveLabel extends JLabel {
			private static final long serialVersionUID = 1L;

			public SwingRemoveLabel() {
				super("[x]");
				setForeground(Color.BLUE);
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						SwingLookup.this.selectedObject = null;
						changeListener.changed(SwingLookup.this);
					}
				});
			}
		}
		
		private class LookupClickListener implements TableActionListener<T> {
			@Override
			public void action(T selectedObject, List<T> selectedObjects) {
				SwingLookup.this.selectedObject = selectedObject;
				dialog.closeDialog();
				changeListener.changed(SwingLookup.this);
			}
		}

	}

	public <T> IDialog createSearchDialog(IContext context, Search<T> index, Object[] keys, TableActionListener<T> listener) {
		SwingSearchPanel<T> panel = new SwingSearchPanel<T>(index, keys, listener);
		return createDialog(context, null, panel);
	}

	@Override
	public OutputStream store(IContext context, String buttonText) {
		JFileChooser chooser = new JFileChooser();
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		if (JFileChooser.APPROVE_OPTION == chooser.showDialog((Component) context, buttonText)) {
			File outputFile = chooser.getSelectedFile();
			try {
				return new FileOutputStream(outputFile);
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			}
		} else {
			return null;
		}
	}

	@Override
	public InputStream load(IContext context, String buttonText) {
		JFileChooser chooser = new JFileChooser();
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		if (JFileChooser.APPROVE_OPTION == chooser.showDialog((Component) context, buttonText)) {
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

	public static Action[] adaptActions(IAction[] actions, IContext context) {
		Action[] swingActions = new Action[actions.length];
		for (int i = 0; i<actions.length; i++) {
			swingActions[i] = adaptAction(actions[i], context);
		}
		return swingActions;
	}

	public static Action adaptAction(final IAction action, final IContext context) {
		final Action swingAction = new AbstractAction(action.getName()) {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				action.action(context);
			}
		};
		swingAction.putValue(Action.SHORT_DESCRIPTION, action.getDescription());
		action.setChangeListener(new ActionChangeListener() {
			{
				update();
			}
			
			@Override
			public void change() {
				update();
			}

			protected void update() {
				swingAction.setEnabled(action.isEnabled());
			}
		});
		return swingAction;
	}
	
}
