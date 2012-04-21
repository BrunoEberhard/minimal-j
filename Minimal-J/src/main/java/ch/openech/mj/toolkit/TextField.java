package ch.openech.mj.toolkit;

import java.awt.event.FocusListener;

import ch.openech.mj.edit.fields.Focusable;

public interface TextField extends IComponent, Focusable {
	
	public void setText(String text);

	public String getText();

	public void setEnabled(boolean editable);
	
	public static interface TextFieldFilter {
		
		public int getLimit();
		
		public String getAllowedCharacters();
		
	}
	
	public void setFocusListener(FocusListener focusListener);

}
