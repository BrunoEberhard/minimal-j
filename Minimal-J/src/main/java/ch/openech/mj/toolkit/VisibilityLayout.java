package ch.openech.mj.toolkit;

import ch.openech.mj.edit.fields.Focusable;

public interface VisibilityLayout extends AbstractComponentContainer, Focusable {

	// must have a constructor wiht Object component to set the content
	
	public void setVisible(boolean visible);

	public boolean isVisible();
	
}
