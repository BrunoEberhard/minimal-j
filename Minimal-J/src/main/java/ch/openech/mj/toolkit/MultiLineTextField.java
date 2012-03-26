package ch.openech.mj.toolkit;

import javax.swing.Action;

import ch.openech.mj.edit.fields.Focusable;
import ch.openech.mj.edit.validation.Indicator;

public interface MultiLineTextField extends IComponent, Indicator, Focusable {

	public void clear();
	
	public void addObject(Object object);

	public void addAction(Action action);
	
	public void addGap();

	public void setEnabled(boolean enabled);
	
}
