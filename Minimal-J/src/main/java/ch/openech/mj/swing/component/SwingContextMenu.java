package ch.openech.mj.swing.component;

import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.plaf.ComponentInputMapUIResource;

public class SwingContextMenu extends JPopupMenu {
	
	private final JComponent component;
	
	public SwingContextMenu(JComponent component) {
		this.component = component;
	}
		
	@Override
	public JMenuItem add(Action a) {
		installAccelerator(a);
		return super.add(a);
	}
	
	public void add(Action... actions) {
		for (Action action : actions) {
			add(action);
		}
	}

	private void installAccelerator(Action action) {
		if (action.getValue(Action.ACCELERATOR_KEY) instanceof KeyStroke) {
			KeyStroke keyStroke = (KeyStroke) action.getValue(Action.ACCELERATOR_KEY);
			
			if (KeyEvent.VK_ENTER == keyStroke.getKeyCode()) {
				// The enter action would take over every other action selected by keyboard
				action.putValue(Action.ACCELERATOR_KEY, null);
			}
			
			InputMap windowInputMap = SwingUtilities.getUIInputMap(component, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
			
			if (windowInputMap == null) {
				windowInputMap = new ComponentInputMapUIResource(component);
				SwingUtilities.replaceUIInputMap(component, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, windowInputMap);
			}
			windowInputMap.put(keyStroke, keyStroke.toString());
			component.getActionMap().put(keyStroke.toString(), action);
		}
	}

}
