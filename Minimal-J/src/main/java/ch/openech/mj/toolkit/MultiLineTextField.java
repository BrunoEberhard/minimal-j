package ch.openech.mj.toolkit;

import ch.openech.mj.edit.fields.Focusable;
import ch.openech.mj.edit.validation.Indicator;

public interface MultiLineTextField extends IComponent, Indicator, Focusable {

	public void setText(String text);
	
	public void setEnabled(boolean enabled);
	
}
