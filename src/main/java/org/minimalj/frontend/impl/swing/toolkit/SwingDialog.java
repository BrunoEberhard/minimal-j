package org.minimalj.frontend.impl.swing.toolkit;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.impl.swing.SwingFrame;
import org.minimalj.frontend.page.Page.Dialog;

import com.formdev.flatlaf.util.UIScale;

public class SwingDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	
	private final Dialog dialog;
	private final Component focusAfterClose;
	private final Action saveAction;
	
	public SwingDialog(SwingFrame frame, Dialog dialog, Component content, Action saveAction, Action closeAction) {
		this(frame, dialog, content, saveAction, closeAction, null);
	}

	public SwingDialog(SwingFrame frame, Dialog dialog, Component content, Action saveAction, Action closeAction, Component focusAfterClose) {
		super(frame);
		setModalityType(ModalityType.DOCUMENT_MODAL);

		this.dialog = dialog;
		this.focusAfterClose = focusAfterClose;
		this.saveAction = saveAction;
		
		setTitle(dialog.getTitle());
		setResizable(true);
		
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		WindowListener listener = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (closeAction != null) {
					closeAction.run();
				} else {
					setVisible(false);
				}
			}
		};
		addWindowListener(listener);

		setContentPane((JComponent) content);
		pack();
		if (dialog.getWidth() != Dialog.FIT_CONTENT) {
			setSize(new Dimension(dialog.getWidth(), getSize().height));
		}
		
		setLocationRelativeTo(frame);

		if (frame != null) {
			Dimension size = getSize();
			Rectangle bounds = frame.getGraphicsConfiguration().getDevice().getDefaultConfiguration().getBounds();
			size.height = Math.min(UIScale.scale(bounds.height) - 50, size.height);
			size.width = Math.min(UIScale.scale(bounds.width) - 100, size.width);
			setMaximumSize(size);
		}

		SwingFrontend.focusFirstComponent((JComponent) this.getContentPane());
		SwingUtilities.invokeLater(() -> {
			setVisible(true);	
		});
	}

	public Dialog getDialog() {
		return dialog;
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
