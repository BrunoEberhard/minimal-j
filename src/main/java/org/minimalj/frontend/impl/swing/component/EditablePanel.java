package org.minimalj.frontend.impl.swing.component;

import java.awt.AWTEvent;
import java.awt.ActiveEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.MenuComponent;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultDesktopManager;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import javax.swing.plaf.basic.BasicInternalFrameUI;

// Must be a JDesktopPane (not only a JPanel) in order to
// work as a parent of a JInternalFrame or JOptionPane.showInternal
public class EditablePanel extends JDesktopPane {
	private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(EditablePanel.class.getName());
	private List<JInternalFrame> openFrames = new ArrayList<JInternalFrame>();
	
	public EditablePanel() {
		setOpaque(false);
		setLayout(null);
		
		// The panel must not behave special
		setFocusCycleRoot(false);
		setDesktopManager(new BoundedDesktopManager());
	}

	public void setContent(JComponent content) {
		JInternalFrame frame = new JInternalFrame("");
		BasicInternalFrameUI bi = (BasicInternalFrameUI) frame.getUI();
		bi.setNorthPane(null);
		frame.setBorder(null);
		frame.setLayout(new BorderLayout());
		frame.add(content, BorderLayout.CENTER);
		add(frame);
		frame.setVisible(true);
		try {
			frame.setMaximum(true);
		} catch (PropertyVetoException e) {
			LOG.log(Level.WARNING, e.getLocalizedMessage(), e);
		}
	}

	public void openModalDialog(JInternalFrame internalFrame) {
		if (openFrames.contains(internalFrame)) {
			throw new IllegalArgumentException("Dialog already open");
		}

		openFrames.add(internalFrame);
		
		add(internalFrame);
		
		internalFrame.addInternalFrameListener(listener);

		arrangeFrames();
		internalFrame.setVisible(true);
		internalFrame.requestFocus();
	}
	
	private int lockCount = 0;
	
	public void lock() {
		lockCount++;
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		try {
			if (SwingUtilities.isEventDispatchThread()) {
				EventQueue theQueue = getToolkit().getSystemEventQueue();
				while (lockCount > 0) {
					AWTEvent event = theQueue.getNextEvent();
					Object source = event.getSource();

					if (event instanceof MouseEvent) {
						continue;
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
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			} else {
				while (lockCount > 0) {
					wait();
				}
			}
		} catch (InterruptedException x) {
			LOG.warning(x.getLocalizedMessage());
		}
	}

	public void unlock() {
		lockCount--;
	}
	
	public boolean tryToCloseDialogs() {
		for (int i = openFrames.size()-1; i>= 0; i--) {
			JInternalFrame frame = openFrames.get(i);
			frame.doDefaultCloseAction();
			if (frame.isVisible()) return false;
		}
		return true;
	}
	
	private void closeModalDialog(JInternalFrame internalFrame) {
		if (!openFrames.contains(internalFrame)) {
			throw new IllegalArgumentException("Dialog to close not open");
		}
		openFrames.remove(internalFrame);
		internalFrame.setVisible(false);
		remove(internalFrame);
	}

	private void arrangeFrames() {
		JInternalFrame newFrame = openFrames.get(openFrames.size() - 1);
		if (openFrames.size() == 1) {
			if (shouldCompleteFill(newFrame)) {
				try {
					newFrame.setMaximum(true);
					newFrame.setBorder(null);
					return;
				} catch (PropertyVetoException e) {
				}
			}
			openFrames.get(0).setLocation(getWidth() / 2 - openFrames.get(0).getWidth() / 2 - 40, 50);
		} else {
			JInternalFrame lastFrame = openFrames.get(openFrames.size() - 2);
			newFrame.setLocation(lastFrame.getX() + 50, lastFrame.getY() + 40);
			if (newFrame.getX() + newFrame.getWidth() > getWidth()) {
				newFrame.setSize(getWidth() - newFrame.getX(), newFrame.getHeight());
			}
		}
	}
	
	private boolean shouldCompleteFill(JInternalFrame frame) {
		return frame.getWidth() * 2 > getWidth() && frame.getHeight() * 2 > getHeight();
	}

	private InternalFrameListener listener = new InternalFrameAdapter() {
		
		@Override
		public void internalFrameClosed(InternalFrameEvent e) {
			closeModalDialog(e.getInternalFrame());
			e.getInternalFrame().removeInternalFrameListener(this);
		}
	};

	// see http://stackoverflow.com/questions/8136944/preventing-jinternalframe-from-being-moved-out-of-a-jdesktoppane
	public class BoundedDesktopManager extends DefaultDesktopManager {

		private static final long serialVersionUID = 1L;

		@Override
		public void beginDraggingFrame(JComponent f) {
			// Don't do anything. Needed to prevent the DefaultDesktopManager
			// setting the dragMode
		}

		@Override
		public void beginResizingFrame(JComponent f, int direction) {
			// Don't do anything. Needed to prevent the DefaultDesktopManager
			// setting the dragMode
		}

		@Override
		public void setBoundsForFrame(JComponent f, int newX, int newY, int newWidth, int newHeight) {
			boolean didResize = (f.getWidth() != newWidth || f.getHeight() != newHeight);
			if (!inBounds((JInternalFrame) f, newX, newY, newWidth, newHeight)) {
				Container parent = f.getParent();
				Dimension parentSize = parent.getSize();
				int boundedX = (int) Math.min(Math.max(0, newX), parentSize.getWidth() - newWidth);
				int boundedY = (int) Math.min(Math.max(0, newY), parentSize.getHeight() - newHeight);
				f.setBounds(boundedX, boundedY, newWidth, newHeight);
			} else {
				f.setBounds(newX, newY, newWidth, newHeight);
			}
			if (didResize) {
				f.validate();
			}
		}

		protected boolean inBounds(JInternalFrame f, int newX, int newY, int newWidth, int newHeight) {
			if (newX < 0 || newY < 0)
				return false;
			if (newX + newWidth > f.getDesktopPane().getWidth())
				return false;
			if (newY + newHeight > f.getDesktopPane().getHeight())
				return false;
			return true;
		}
	}
	
}