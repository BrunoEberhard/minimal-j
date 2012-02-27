package ch.openech.mj.swing.toolkit;

import java.awt.BorderLayout;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;

import javax.swing.JInternalFrame;

import ch.openech.mj.application.EditablePanel;
import ch.openech.mj.toolkit.VisualDialog;

// TODO make async (implement executeAsync wie in AsyncPage)
public class SwingInternalFrame extends JInternalFrame implements VisualDialog {
	// private static final Logger logger = Logger.getLogger(EditorInternalFrameDecorator.class.getName());
	
	private final EditablePanel editablePanel;
	private final Component focusAfterClose;
	private CloseListener closeListener;
	
	//
	
	public SwingInternalFrame(EditablePanel editablePanel, Component content, String title) {
		this(editablePanel, content, title, null);
	}

	public SwingInternalFrame(EditablePanel editablePanel, Component content, String title, Component focusAfterClose) {
		this.editablePanel = editablePanel;
		this.focusAfterClose = focusAfterClose;
		
		setTitle(title);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		// TODO: tuts ein simples setContent(internalFrameEditorPanel) auch?
		setLayout(new BorderLayout());
		add(content, BorderLayout.CENTER);
		pack();
		
		addVetoableChangeListener(new EditorDialogInternalFrameListener());
	}
	
	private class EditorDialogInternalFrameListener implements VetoableChangeListener {

		@Override
		public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
			if (IS_CLOSED_PROPERTY.equals(evt.getPropertyName())) {
				if (closeListener != null && !closeListener.close()) {
					PropertyChangeEvent event = new PropertyChangeEvent(evt.getSource(), evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
					throw new PropertyVetoException("Closing aborted", event);
				}
			}
		}
	}

	@Override
	public void setCloseListener(CloseListener closeListener) {
		this.closeListener = closeListener;
	}

	@Override
	public void setResizable(boolean resizable) {
		super.setResizable(resizable);
	}

	@Override
	public void closeDialog() {
		try {
			setClosed(true);
			if (focusAfterClose != null) {
				focusAfterClose.requestFocus();
			}
			if (closeListener != null) {
				closeListener.close();
			}
		} catch (PropertyVetoException e) {
			// nothing to do, simply dont close
		}
	}

	@Override
	public void openDialog() {
		editablePanel.openModalDialog(this);
	}
	
}
