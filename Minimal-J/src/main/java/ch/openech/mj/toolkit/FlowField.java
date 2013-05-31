package ch.openech.mj.toolkit;

import javax.swing.Action;


public interface FlowField extends IComponent {

	public void clear();
	
	public void addObject(Object object);

	public void addHtml(String html);

	public void addAction(Action action);
	
	public void addGap();

	public void setEnabled(boolean enabled);
	
}