package ch.openech.mj.swing.toolkit;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import ch.openech.mj.application.EditablePanel;
import ch.openech.mj.toolkit.VisualDialog;

// TODO make async (implement executeAsync wie in AsyncPage)
public class SwingInternalFrame extends JInternalFrame implements VisualDialog {
	// private static final Logger logger = Logger.getLogger(EditorInternalFrameDecorator.class.getName());
	
	private final Component focusAfterClose;
	private CloseListener closeListener;
	
	//
	
	public SwingInternalFrame(EditablePanel editablePanel, Component content, String title) {
		this(editablePanel, content, title, null);
	}

	public SwingInternalFrame(EditablePanel editablePanel, Component content, String title, Component focusAfterClose) {
		this.focusAfterClose = focusAfterClose;
		
		setTitle(title);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		// TODO: tuts ein simples setContent(internalFrameEditorPanel) auch?
		setLayout(new BorderLayout());
		add(content, BorderLayout.CENTER);
		pack();
		
		addInternalFrameListener(new EditorDialogInternalFrameListener());
		editablePanel.openModalDialog(this);
	}
	
	private class EditorDialogInternalFrameListener extends InternalFrameAdapter {

		@Override
		public void internalFrameClosed(InternalFrameEvent e) {
			if (focusAfterClose != null) {
				focusAfterClose.requestFocus();
			}
		}
		
		@Override
		public void internalFrameClosing(InternalFrameEvent e) {
			if (closeListener == null || closeListener.close()) {
				SwingInternalFrame.this.setVisible(false);
				SwingInternalFrame.this.dispose();
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
	
}
