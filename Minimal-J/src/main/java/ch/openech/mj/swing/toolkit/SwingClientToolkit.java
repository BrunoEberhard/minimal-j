package ch.openech.mj.swing.toolkit;

import java.awt.Color;
import java.awt.Component;
import java.awt.FocusTraversalPolicy;
import java.awt.Window;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.FocusManager;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.ChangeListener;
import javax.swing.text.JTextComponent;

import ch.openech.mj.model.annotation.LimitedString;
import ch.openech.mj.swing.component.BubbleMessageSupport;
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
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.ImportHandler;
import ch.openech.mj.toolkit.ProgressListener;
import ch.openech.mj.toolkit.SwitchLayout;
import ch.openech.mj.toolkit.TextField;
import ch.openech.mj.toolkit.VisualDialog;
import ch.openech.mj.toolkit.VisualTable;

public class SwingClientToolkit extends ClientToolkit {

	@Override
	public IComponent createLabel(String string) {
		return new SwingLabel(string);
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
	public TextField createTextField(ChangeListener changeListener, int maxLength) {
		return new SwingTextField(changeListener, maxLength);
	}

	@Override
	public TextField createTextField(ChangeListener changeListener, int maxLength, String allowedCharacters) {
		return new SwingTextField(changeListener, maxLength, allowedCharacters);
	}

	@Override
	public FlowField createFlowField() {
		return new SwingFlowField();
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

	@Override
	public void showNotification(IComponent c, String text) {
		try {
			JComponent component = (JComponent) c;
			BubbleMessageSupport.showBubble(component, text);
		} catch (Exception x) {
			// TODO
			x.printStackTrace();
		}
	}

	@Override
	public void focusFirstComponent(IComponent object) {
		if (object instanceof JComponent) {
			JComponent jComponent = (JComponent) object;
			if (jComponent.isShowing()) {
				focusFirstComponentNow(jComponent);
			} else {
				focusFirstComponentLater(jComponent, object);
			}
		}
	}

	private void focusFirstComponentNow(JComponent component) {
		FocusTraversalPolicy focusPolicy = component.getFocusTraversalPolicy();
		if (component instanceof JTextComponent || component instanceof JComboBox || component instanceof JCheckBox) {
			component.requestFocus();
		} else if (focusPolicy != null && focusPolicy.getFirstComponent(component) != null) {
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
	public <T> VisualTable<T> createVisualTable(Class<T> clazz, Object[] fields) {
		return new SwingVisualTable<T>(clazz, fields);
	}

	@Override
	public ProgressListener showProgress(Object parent, String text) {
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
	public VisualDialog openDialog(Object parent, IComponent content, String title) {
		Window window = findWindow((Component)parent);
		Component contentComponent = (Component)content;
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
		while (parentComponent != null && !(parentComponent instanceof Window)) {
			if (parentComponent instanceof JPopupMenu) {
				parentComponent = ((JPopupMenu) parentComponent).getInvoker();
			} else {
				parentComponent = parentComponent.getParent();
			}
		}
		return (Window) parentComponent;
	}

	@Override
	public IComponent createEditorLayout(IComponent content, Action[] actions) {
		return new SwingEditorLayout(content, actions);
	}

	@Override
	public IComponent createSearchLayout(TextField text, Action searchAction, IComponent content, Action... actions) {
		return new SwingSearchLayout(text, searchAction, content, actions);
	}

	@Override
	public Object getParent(Object c) {
		if (c instanceof JPopupMenu) {
			JPopupMenu popupMenu = (JPopupMenu) c;
			return popupMenu.getInvoker();
		} else if (c instanceof Component) {
			return ((Component) c).getParent();
		} else {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public IComponent importField(ImportHandler importHandler, String buttonText) {
		return null;
	}

	@Override
	public IComponent exportLabel(ExportHandler exportHandler, String label) {
		return new SwingExportLabel(exportHandler, label);
	}
	
	@Override
	public void export(Object parent, String buttonText, ExportHandler exportHandler) {
		Window window = findWindow((Component) parent);
		JFileChooser chooser = new JFileChooser();
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		if (JFileChooser.APPROVE_OPTION == chooser.showDialog(window, buttonText)) {
			File outputFile = chooser.getSelectedFile();
			try {
				exportHandler.export(new FileOutputStream(outputFile));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public InputStream imprt(Object parent, String buttonText) {
		JFileChooser chooser = new JFileChooser();
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		if (JFileChooser.APPROVE_OPTION == chooser.showDialog(null, buttonText)) {
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
