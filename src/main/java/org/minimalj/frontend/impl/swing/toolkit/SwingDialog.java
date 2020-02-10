package org.minimalj.frontend.impl.swing.toolkit;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.impl.swing.SwingFrame;
import org.minimalj.frontend.page.IDialog;

public class SwingDialog extends JDialog implements IDialog {
	private static final long serialVersionUID = 1L;
	
	private final Component focusAfterClose;
	private final Action saveAction;
	
	public SwingDialog(SwingFrame frame, String title, Component content, Action saveAction, Action closeAction) {
		this(frame, title, content, saveAction, closeAction, null);
	}

	public SwingDialog(SwingFrame frame, String title, Component content, Action saveAction, Action closeAction, Component focusAfterClose) {
		super(frame);
		setModalityType(ModalityType.DOCUMENT_MODAL);

		this.focusAfterClose = focusAfterClose;
		this.saveAction = saveAction;
		
		setTitle(title);
		setResizable(true);
		
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		WindowListener listener = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (closeAction != null) {
					closeAction.action();
				} else {
					setVisible(false);
				}
			}
		};
		addWindowListener(listener);

		setContentPane((JComponent) content);
		pack();

		Dimension size = getSize();
		size.height = Math.min(frame.getGraphicsConfiguration().getDevice().getDefaultConfiguration().getBounds().height - 50, size.height);
		size.width = Math.min(frame.getGraphicsConfiguration().getDevice().getDefaultConfiguration().getBounds().width - 100, size.width);
		setSize(size);

		setLocationRelativeTo(frame);

		SwingFrontend.focusFirstComponent((JComponent) this.getContentPane());
		SwingUtilities.invokeLater(() -> setVisible(true));
	}

	@Override
	public void setResizable(boolean resizable) {
		super.setResizable(resizable);
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension preferredSize = super.getPreferredSize();
		if (isResizable()) {
			// if ScrollBar appears, the right side of the fields should not be cut
			return new Dimension(preferredSize.width + 30, preferredSize.height + 70);
		} else {
			return new Dimension(preferredSize.width, preferredSize.height);
		}
	}

	@Override
	public void closeDialog() {
		setVisible(false);
		focusAfterCloseComponent();
	}

	private void focusAfterCloseComponent() {
		if (focusAfterClose != null) {
			focusAfterClose.requestFocus();
		}
	}

	public Action getSaveAction() {
		return saveAction;
	}
	
}
