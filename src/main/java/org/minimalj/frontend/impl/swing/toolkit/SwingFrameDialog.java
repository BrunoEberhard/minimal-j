package org.minimalj.frontend.impl.swing.toolkit;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.minimalj.application.Application;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.page.Page.Dialog;

public class SwingFrameDialog extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private final Dialog dialog;
	private final Action saveAction;
	
	public SwingFrameDialog(Dialog dialog, Component content, Action saveAction, Action closeAction) {
		this.dialog = dialog;
		this.saveAction = saveAction;
		
		setTitle(dialog.getTitle());

		InputStream inputStream = Application.getInstance().getIcon();
		if (inputStream != null) {
			try {
				setIconImage(ImageIO.read(inputStream));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

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
		if (dialog.getHeight() != Dialog.FIT_CONTENT && dialog.getWidth() != Dialog.FIT_CONTENT) {
			setSize(dialog.getWidth(), dialog.getHeight());
			setResizable(false);
		} else {
			pack();
			setResizable(true);
		}
		
		setLocationRelativeTo(null);

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
	}

	public Action getSaveAction() {
		return saveAction;
	}
}
