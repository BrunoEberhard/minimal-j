package ch.openech.mj.toolkit;

import javax.swing.event.ChangeListener;

import ch.openech.mj.edit.fields.Focusable;

public interface CheckBox extends IComponent, Focusable {
	
	public void setSelected(boolean selected);

	public boolean isSelected();

	public void setChangeListener(ChangeListener changeListener);
	
	public void setEnabled(boolean enabled);

}
