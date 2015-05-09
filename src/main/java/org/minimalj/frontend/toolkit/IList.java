package org.minimalj.frontend.toolkit;

import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;

public interface IList extends IComponent {

	/**
	 * @param enabled if false no content should be shown (or
	 * only in gray) and all actions must get disabled
	 */
	public void setEnabled(boolean enabled);
	
	public void clear();
	
	public void add(Object object, Action... actions);
	
}