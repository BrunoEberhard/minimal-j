package org.minimalj.frontend.toolkit;

import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;


public interface CheckBox extends IComponent {
	
	public void setSelected(boolean selected);

	public boolean isSelected();

	public void setEditable(boolean editable);

}
