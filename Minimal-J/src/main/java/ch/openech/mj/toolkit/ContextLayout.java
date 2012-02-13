package ch.openech.mj.toolkit;

import java.util.List;

import javax.swing.Action;

public interface ContextLayout extends AbstractComponentContainer {

	public void setActions(List<Action> actions);

	public void setActions(Action... action);
	
}
