package org.minimalj.frontend.toolkit;

import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;


public interface SwitchComponent extends IComponent {

	public void show(IComponent component);

	public IComponent getShownComponent();
	
}
