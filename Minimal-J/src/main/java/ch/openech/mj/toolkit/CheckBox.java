package ch.openech.mj.toolkit;

import ch.openech.mj.edit.fields.Focusable;

public interface CheckBox extends IComponent, Focusable {
	
	public void setSelected(boolean selected);

	public boolean isSelected();

	public void setEnabled(boolean enabled);

}
