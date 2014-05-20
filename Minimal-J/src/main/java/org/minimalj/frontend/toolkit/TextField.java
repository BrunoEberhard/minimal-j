package org.minimalj.frontend.toolkit;



public interface TextField extends IComponent {
	
	public void setText(String text);

	public String getText();

	public void setEditable(boolean editable);
	
	public void setFocusListener(IFocusListener focusListener);
	
	public void setCommitListener(Runnable runnable);

}
