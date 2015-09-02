package org.minimalj.frontend.impl.swing.toolkit;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.beans.PropertyVetoException;

import javax.swing.JInternalFrame;

import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.impl.swing.component.EditablePanel;
import org.minimalj.frontend.page.IDialog;

public class SwingInternalFrame extends JInternalFrame implements IDialog {
	// private static final Logger logger = Logger.getLogger(EditorInternalFrameDecorator.class.getName());
	private static final long serialVersionUID = 1L;
	
	private final EditablePanel editablePanel;
	private final Component focusAfterClose;
	private final Action saveAction, closeAction;
	
	public SwingInternalFrame(EditablePanel editablePanel, String title, Component content, Action saveAction, Action closeAction) {
		this(editablePanel, title, content, saveAction, closeAction, null);
	}

	public SwingInternalFrame(EditablePanel editablePanel, String title, Component content, Action saveAction, Action closeAction, Component focusAfterClose) {
		this.editablePanel = editablePanel;
		this.focusAfterClose = focusAfterClose;
		this.saveAction = saveAction;
		this.closeAction = closeAction;
		
		setTitle(title);
		setResizable(true);
		
		// TODO: tuts ein simples setContent(internalFrameEditorPanel) auch?
		setLayout(new BorderLayout());
		add(content, BorderLayout.CENTER);
		pack();
		
		setClosable(true);
		
		editablePanel.openModalDialog(this);
		if (getHeight() >= editablePanel.getHeight()) {
			setLocation(getLocation().x, 0);
		}
		SwingFrontend.focusFirstComponent(this);
	}
	
	@Override
	public void doDefaultCloseAction() {
		if (closeAction != null) {
			closeAction.action();
		} else {
			closeDialog();
		}
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

	public Action getSaveAction() {
		return saveAction;
	}
}
