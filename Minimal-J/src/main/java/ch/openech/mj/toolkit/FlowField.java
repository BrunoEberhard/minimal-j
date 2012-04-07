package ch.openech.mj.toolkit;

import javax.swing.Action;

import ch.openech.mj.edit.fields.Focusable;

public interface FlowField extends IComponent, Focusable {

	public void clear();
	
	public void addObject(Object object);

	public void addHtml(String html);

	public void addAction(Action action);
	
	public void addGap();

	public void setEnabled(boolean enabled);
	
}