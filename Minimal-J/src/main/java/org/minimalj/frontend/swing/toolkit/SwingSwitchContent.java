package org.minimalj.frontend.swing.toolkit;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.minimalj.frontend.toolkit.ClientToolkit.IContent;
import org.minimalj.frontend.toolkit.ClientToolkit.SwitchContent;

public class SwingSwitchContent extends JPanel implements SwitchContent {
	private static final long serialVersionUID = 1L;
	
	private IContent shownComponent;
	
	public SwingSwitchContent() {
		super(new BorderLayout());
		setInheritsPopupMenu(true);
	}

	@Override
	public void show(IContent c) {
		if (shownComponent != c) {
			JComponent component = (JComponent) c;
			removeAll();
			if (component != null) {
				add(component, BorderLayout.CENTER);
			}
			refresh(this);
		}
		shownComponent = c;
	}
	
	private void refresh(Component component) {
		repaint();
		revalidate();
	}

	@Override
	public void requestFocus() {
		if (shownComponent != null) {
			Component component = (Component) shownComponent;
			component.requestFocus();
		}
	}

}
