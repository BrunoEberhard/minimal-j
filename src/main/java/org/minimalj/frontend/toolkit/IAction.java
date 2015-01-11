package org.minimalj.frontend.toolkit;



public interface IAction {

	public String getName();

	public String getDescription();
	
	public void action();
	
	public boolean isEnabled();
	
	public void setChangeListener(ActionChangeListener changeListener);
	
	public interface ActionChangeListener {
		
		public void change();
		
	}
}
