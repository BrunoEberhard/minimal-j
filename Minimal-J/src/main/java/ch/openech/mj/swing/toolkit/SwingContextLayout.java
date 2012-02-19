package ch.openech.mj.swing.toolkit;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.List;

import javax.swing.Action;
import javax.swing.JPanel;

import ch.openech.mj.swing.component.SwingContextMenu;
import ch.openech.mj.toolkit.ContextLayout;
import ch.openech.mj.toolkit.IComponent;

public class SwingContextLayout extends JPanel implements ContextLayout {

	public SwingContextLayout(IComponent content) {
		super(new BorderLayout());
		Component component = SwingClientToolkit.getComponent(content);
		add(component, BorderLayout.CENTER);
	}

	@Override
	public void setActions(List<Action> actions) {
		setActions(actions.toArray(new Action[actions.size()]));
	}

	@Override
	public void setActions(Action... actions) {
		SwingContextMenu contextMenu = new SwingContextMenu(this);
		contextMenu.add(actions);
		setComponentPopupMenu(contextMenu);
//		inheritPopupMenu(this);
	}

//	private static void inheritPopupMenu(JComponent component) {
//		component.setInheritsPopupMenu(true);
//		for (Component c : component.getComponents()) {
//			if (c instanceof JComponent) {
//				JComponent j = (JComponent) c;
//				inheritPopupMenu(j);
//			}
//		}
//	}
	
}
