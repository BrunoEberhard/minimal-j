package ch.openech.mj.toolkit;

import java.awt.event.FocusListener;

import javax.swing.event.ChangeListener;

import ch.openech.mj.edit.fields.Focusable;
import ch.openech.mj.edit.validation.Indicator;

public interface TextField extends IComponent, Indicator, Focusable {
	
	public void setText(String text);

	public String getText();

	public void setChangeListener(ChangeListener changeListener);
	
	public void setEditable(boolean editable);
	
	public void setEnabled(boolean editable);
	
	public static interface TextFieldFilter {
		
		public String filter(IComponent textField, String requestedString);
		
	}
	
	public void setFocusListener(FocusListener focusListener);

}
