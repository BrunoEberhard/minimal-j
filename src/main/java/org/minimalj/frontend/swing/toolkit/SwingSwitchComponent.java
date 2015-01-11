package org.minimalj.frontend.swing.toolkit;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;
import org.minimalj.frontend.toolkit.SwitchComponent;

public class SwingSwitchComponent extends JPanel implements SwitchComponent {
	private static final long serialVersionUID = 1L;
	
	private final List<IComponent> components;
	private IComponent shownComponent;
	
	public SwingSwitchComponent(IComponent... components) {
		super(new BorderLayout());
		setInheritsPopupMenu(true);
		this.components = Arrays.asList(components);
	}

	@Override
	public void show(IComponent c) {		
		if (!components.contains(c)) throw new IllegalArgumentException("Component not specified at constructor");
		if (shownComponent != c) {
			JComponent component = (JComponent) c;
			removeAll();
			if (component != null) {
				// TODO the updateComponentTreeUI uses 4-5ms. This should only be done if necessary (if l&f has changed)
				SwingUtilities.updateComponentTreeUI(component);
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
	public IComponent getShownComponent() {
		return shownComponent;
	}

	@Override
	public void requestFocus() {
		if (shownComponent != null) {
			Component component = (Component) shownComponent;
			component.requestFocus();
		}
	}
	
}
