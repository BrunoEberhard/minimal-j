package ch.openech.mj.swing.toolkit;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.beans.PropertyVetoException;

import javax.swing.JInternalFrame;

import ch.openech.mj.swing.component.EditablePanel;
import ch.openech.mj.toolkit.IDialog;

public class SwingInternalFrame extends JInternalFrame implements IDialog {
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
		setResizable(true);
		
		// TODO: tuts ein simples setContent(internalFrameEditorPanel) auch?
		setLayout(new BorderLayout());
		add(content, BorderLayout.CENTER);
		pack();
		
		setClosable(true);
	}
	
	@Override
	public void doDefaultCloseAction() {
		if (closeListener != null) {
			if (closeListener.close()) {
				closeDialog();
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
	public Dimension getPreferredSize() {
		Dimension preferredSize = super.getPreferredSize();
		if (isResizable()) {
			// if ScrollBar appears, the right side of the fields should not be cut
			return  new Dimension(Math.min(preferredSize.width + 30, editablePanel.getWidth()), Math.min(preferredSize.height + 70, editablePanel.getHeight()));
		} else {
			return  new Dimension(Math.min(preferredSize.width, editablePanel.getWidth()), Math.min(preferredSize.height, editablePanel.getHeight()));
		}
	}
	
	@Override
	public void closeDialog() {
		try {
			setClosed(true);
			focusAfterCloseComponent();
		} catch (PropertyVetoException e) {
			// there is no Vetoable Listener attached, should never happen
		}
	}

	private void focusAfterCloseComponent() {
		if (focusAfterClose != null) {
			focusAfterClose.requestFocus();
		}
	}

	@Override
	public void openDialog() {
		editablePanel.openModalDialog(this);
		if (getHeight() >= editablePanel.getHeight()) {
			setLocation(getLocation().x, 0);
		}
	}
	
}
