package org.minimalj.frontend.swing.component;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Window;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultDesktopManager;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JPopupMenu;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

// Must be a JDesktopPane (not only a JPanel) in order to
// work as a parent of a JInternalFrame or JOptionPane.showInternal
public class EditablePanel extends JDesktopPane {
	private static final long serialVersionUID = 1L;
	private JComponent content;
	private List<JInternalFrame> openFrames = new ArrayList<JInternalFrame>();

	public EditablePanel() {
		setOpaque(false);
		setLayout(null);
		
		// The panel must not behave special
		setFocusCycleRoot(false);
		setDesktopManager(new BoundedDesktopManager());
	}

	public void setContent(JComponent content) {
		removeAll();
		this.content = content;
		add(content);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void layout() {
		boolean changed = !content.getSize().equals(getSize());
		if (changed) {
			content.setSize(getSize());
			super.layout();
			if (changed && !openFrames.isEmpty()) {
				layoutTree(content);
			} 
		} else {
			super.layout();
		}
	}
	
	private void layoutTree(Component component) {
		component.doLayout();
		if (component instanceof Container) {
			Container container = (Container) component;
			for (Component child : container.getComponents()) {
				layoutTree(child);
			}
		}
	}
    
	@Override
	public void paint(Graphics g) {
		if (!openFrames.isEmpty()) {
			RepaintManager.currentManager(content).setDoubleBufferingEnabled(false);
			content.paint(g);
			RepaintManager.currentManager(content).setDoubleBufferingEnabled(true);
			for (int i = 0; i<openFrames.size()-1; i++) {
				JComponent component = openFrames.get(i);
				RepaintManager.currentManager(component).setDoubleBufferingEnabled(false);
				g.translate(component.getX(), component.getY());
				component.paint(g);
				g.translate(-component.getX(), -component.getY());
				RepaintManager.currentManager(component).setDoubleBufferingEnabled(true);
			}
		}
		super.paint(g);
	}

	public void openModalDialog(JInternalFrame internalFrame) {
		if (openFrames.contains(internalFrame)) {
			throw new IllegalArgumentException("Dialog already open");
		}

		openFrames.add(internalFrame);
		
		removeAll();
		add(internalFrame);

		internalFrame.addInternalFrameListener(listener);

		internalFrame.pack();
		arrangeFrames();
		internalFrame.setVisible(true);
		
		repaintLater();
	}
	
	private void closeModalDialog(JInternalFrame internalFrame) {
		if (!openFrames.contains(internalFrame)) {
			throw new IllegalArgumentException("Dialog to close not open");
		}
		
		openFrames.remove(internalFrame);
		
		removeAll();
		if (openFrames.isEmpty()) {
			add(content);
		} else {
			add(openFrames.get(openFrames.size()-1));
		}
		
		repaintLater();
	}
	
	private void repaintLater() {
		// Offene Frage: Aus welchem Grunde muss nochmals "later" ein repaint
		// ausgelöst werden? Wenn es nicht gemacht wird, wird beim ersten
		// öffenen eines Dialogs der Dialog nicht gezeichnet (Windows XP)
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// Offene Frage: Wenn nicht das ganze Window neu gezeichnet wird bleibt nach dem
				// Oeffnen des ersten JInternalFrame das JMenu "hängen".
				Window window = SwingUtilities.windowForComponent(EditablePanel.this);
				if (window != null) {
					window.repaint();
				} else {
					repaint();
				}
			}
		});
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

	// helper
	
	public static EditablePanel getEditablePanel(Component component) {
		while (component != null) {
			if (component instanceof EditablePanel) {
				return (EditablePanel) component;
			} else if (component instanceof JPopupMenu) {
				component = ((JPopupMenu) component).getInvoker();
			} else {
				component = component.getParent();
			}
		}
		return null;
	}

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

//	public static void main(String... args) {
//		JFrame frame = new JFrame("Test EditablePanel " + new Date());
//		frame.setSize(800, 600);
//		frame.setLocationRelativeTo(null);
//		
//		frame.setLayout(new BorderLayout());
//		EditablePanel panel = new EditablePanel();
//		frame.add(panel, BorderLayout.CENTER);
//		
//		panel.setContent(new JLabel("Hallo"));
//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		frame.setVisible(true);
//		
//		JInternalFrame internalFrame = new JInternalFrame("Test Frame 1");
//		internalFrame.setLayout(new BorderLayout());
//		internalFrame.add(new JButton("World"), BorderLayout.CENTER);
//		internalFrame.setSize(200, 100);
//		internalFrame.setResizable(true);
//		internalFrame.setClosable(true);
//		panel.openModalDialog(internalFrame);
//		
//		internalFrame = new JInternalFrame("Test Frame 2");
//		internalFrame.setLayout(new BorderLayout());
//		internalFrame.add(new JButton("Hallo Editablepanel!"), BorderLayout.CENTER);
//		internalFrame.setSize(160, 100);
//		internalFrame.setResizable(true);
//		internalFrame.setClosable(true);
//		panel.openModalDialog(internalFrame);
//	}
	
}