package org.minimalj.frontend.toolkit;

import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;
import org.minimalj.frontend.toolkit.ClientToolkit.IContent;


public interface GridContent extends IContent {
	
	public void add(IComponent field, int span);
	
}
