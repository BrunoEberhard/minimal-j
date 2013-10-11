package ch.openech.mj.swing.toolkit;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JComponent;
import javax.swing.JDialog;

import ch.openech.mj.toolkit.IDialog;

public class SwingEditorDialog extends JDialog implements IDialog {
	private static final long serialVersionUID = 1L;
	
	private final Component focusAfterClose;
	private CloseListener closeListener;
	
	//
	
	public SwingEditorDialog(Window parent, Component content, String title) {
		this(parent, content, title, null);
	}

	public SwingEditorDialog(Window parent, Component content, String title, Component focusAfterClose) {
		super(parent, Dialog.ModalityType.DOCUMENT_MODAL);
		
		this.focusAfterClose = focusAfterClose;

		setTitle(title);
		setResizable(true);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		setLayout(new BorderLayout());
		add(content, BorderLayout.CENTER);
		
		pack();
		setLocationRelativeTo(parent);
		
		addWindowListener(new EditorDialogWindowListener());
	}
	
	private class EditorDialogWindowListener extends WindowAdapter {

		@Override
		public void windowClosed(WindowEvent e) {
			if (focusAfterClose != null) {
				focusAfterClose.requestFocus();
			}
		}
		
		@Override
		public void windowClosing(WindowEvent e) {
			if (closeListener == null || closeListener.close()) {
				closeDialog();
			}
		}
	}
	
	@Override
	public void setCloseListener(CloseListener closeListener) {
		this.closeListener = closeListener;
	}

	@Override
	public void closeDialog() {
		setVisible(false);
		dispose();
	}

	@Override
	public void openDialog() {
		setVisible(true);
		SwingClientToolkit.focusFirstComponent((JComponent) getContentPane());
	}

}
