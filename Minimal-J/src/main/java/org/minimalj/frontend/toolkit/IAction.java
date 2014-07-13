package org.minimalj.frontend.toolkit;

import org.minimalj.frontend.toolkit.ClientToolkit.IContext;



public interface IAction {

	public String getName();

	public String getDescription();
	
	public void action(IContext context2);
	
	public boolean isEnabled();
	
	public void setChangeListener(ActionChangeListener changeListener);
	
	public interface ActionChangeListener {
		
		public void change();
		
	}
}
