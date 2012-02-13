package ch.openech.mj.application;

import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Window;
import java.awt.Dialog.ModalityType;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JDialog;
import javax.swing.JInternalFrame;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

// Must be a JDesktopPane (not only a JPanel) in order to
// work as a parent of a JInternalFrame or JOptionPane.showInternal
public class EditablePanel extends JDesktopPane {
	private static final boolean NORMAL_FRAMES = false;
	private JComponent content;
	private List<JInternalFrame> openFrames = new ArrayList<JInternalFrame>();

	public EditablePanel() {
		setOpaque(false);
		setLayout(null);
		
		// The panel must not behave special
		setFocusCycleRoot(false);
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
		openModalDialog(internalFrame, false);
	}

	public void openModalDialog(JInternalFrame internalFrame, boolean resizeToMax) {
		if (!NORMAL_FRAMES) {
			openFrames.add(internalFrame);
			
			removeAll();
			add(internalFrame);

			internalFrame.addInternalFrameListener(listener);

			internalFrame.pack();
			arrangeFrames(resizeToMax);
			internalFrame.setVisible(true);
			
			repaintLater();
		} else {
			JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this));
			dialog.setModalityType(ModalityType.DOCUMENT_MODAL);
//			JDesktopPane pane = new JDesktopPane();
//			pane.add(internalFrame);
//			internalFrame.setUI(new BasicInternalFrameUI(internalFrame));
//			internalFrame.pack();
			dialog.setContentPane(internalFrame.getContentPane());
			dialog.pack();
			dialog.setTitle(internalFrame.getTitle());
			dialog.setResizable(internalFrame.isResizable());
			dialog.setLocationRelativeTo(this);
			dialog.setVisible(true);
		}
	}
	
//	private static class VoidFrameUI extends BasicInternalFrameUI {
//		
//	}
	
	private void closeModalDialog(JInternalFrame internalFrame) {
		if (openFrames.get(openFrames.size()-1) != internalFrame) {
			throw new IllegalArgumentException();
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

	private void arrangeFrames(boolean resizeToMax) {
		if (openFrames.size() == 1) {
			openFrames.get(0).setLocation(getWidth() / 2 - openFrames.get(0).getWidth() / 2 - 40, 50);
		} else {
			JInternalFrame lastFrame = openFrames.get(openFrames.size() - 2);
			JInternalFrame newFrame = openFrames.get(openFrames.size() - 1);
			newFrame.setLocation(lastFrame.getX() + 50, lastFrame.getY() + 40);
		}
		if (resizeToMax) {
			JInternalFrame newFrame = openFrames.get(openFrames.size() - 1);
			int width = Math.max(getWidth() - newFrame.getLocation().x - 50, newFrame.getWidth());
			int height = Math.max(getHeight() - newFrame.getLocation().y - 40, newFrame.getHeight());
			newFrame.setSize(width, height);
		}
	}

	private InternalFrameListener listener = new InternalFrameAdapter() {
		
		@Override
		public void internalFrameClosed(InternalFrameEvent e) {
			closeModalDialog(e.getInternalFrame());
			e.getInternalFrame().removeInternalFrameListener(this);
		}
	};

	// helper
	
	public static EditablePanel getEditablePanel(final Component component) {
		EditablePanel editablePanel = findEditablePanelByParent(component);
		if (editablePanel == null) {
			editablePanel = findEditablePanelInChildren(component);
		}
		return editablePanel;
	}

	private static EditablePanel findEditablePanelByParent(final Component component) {
		if (component == null) {
			return null;
		} else if (component instanceof EditablePanel) {
			return (EditablePanel) component;
		} else {
			return findEditablePanelByParent(component.getParent());
		}
	}

	private static EditablePanel findEditablePanelInChildren(final Component component) {
		if (component instanceof EditablePanel) {
			return (EditablePanel) component;
		} else if (component instanceof Container) {
			Container container = (Container) component;
			for (Component c : container.getComponents()) {
				EditablePanel editablePanel = findEditablePanelInChildren(c);
				if (editablePanel != null)
					return editablePanel;
			}
		}
		return null;
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