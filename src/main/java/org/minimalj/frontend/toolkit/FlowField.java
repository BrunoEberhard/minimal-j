package org.minimalj.frontend.toolkit;

import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;

public interface FlowField extends IComponent {

	public void clear();
	
	public void add(IComponent component);

	public void addGap();

	
}