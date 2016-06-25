package org.minimalj.frontend.impl.swing.toolkit;

import java.awt.AWTEvent;
import java.awt.ActiveEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.MenuComponent;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;
import java.util.logging.Logger;

import javax.swing.JInternalFrame;
import javax.swing.SwingUtilities;

import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.impl.swing.component.EditablePanel;
import org.minimalj.frontend.page.IDialog;

public class SwingInternalFrame extends JInternalFrame implements IDialog {
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = Logger.getLogger(SwingInternalFrame.class.getName());
	
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
	
	//
	
	@Override
	public void show() {
		super.show();
		SwingUtilities.invokeLater(() -> showModal());
	}

	@Override
	public void setVisible(boolean value) {
		super.setVisible(value);
		if (value) {
			SwingUtilities.invokeLater(() -> showModal());
		}
	}

	private void showModal() {
		EventQueue theQueue = getToolkit().getSystemEventQueue();
		try {
			while (isVisible()) {
				AWTEvent event = theQueue.getNextEvent();
				Object source = event.getSource();

				if (event instanceof MouseEvent) {
					MouseEvent e = (MouseEvent) event;
					MouseEvent m = SwingUtilities.convertMouseEvent((Component) e.getSource(), e, this);
					if (!this.contains(m.getPoint()) && e.getID() != MouseEvent.MOUSE_DRAGGED) {
						continue;
					}
				}

				if (event instanceof ActiveEvent) {
					((ActiveEvent) event).dispatch();
				} else if (source instanceof Component) {
					((Component) source).dispatchEvent(event);
				} else if (source instanceof MenuComponent) {
					((MenuComponent) source).dispatchEvent(event);
				} else {
					LOG.warning("Not dispatched: " + event);
				}
			}
		} catch (InterruptedException x) {
			LOG.warning(x.getLocalizedMessage());
		}
	}
}
