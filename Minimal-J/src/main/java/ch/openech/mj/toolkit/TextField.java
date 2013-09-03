package ch.openech.mj.toolkit;

import java.awt.event.FocusListener;


public interface TextField extends IComponent {
	
	public void setText(String text);

	public String getText();

	public void setEditable(boolean editable);
	
	public void setFocusListener(FocusListener focusListener);
	
	public void setCommitListener(Runnable runnable);

}
