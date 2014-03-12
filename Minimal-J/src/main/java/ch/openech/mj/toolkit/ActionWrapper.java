package ch.openech.mj.toolkit;


public abstract class ActionWrapper implements IAction {

	private final IAction action;

	public ActionWrapper(IAction action) {
		this.action = action;
	}
	
	protected IAction getAction() {
		return action;
	}

	@Override
	public String getName() {
		return action.getName();
	}

	@Override
	public String getDescription() {
		return action.getDescription();
	}

	@Override
	public boolean isEnabled() {
		return action.isEnabled();
	}

	@Override
	public void setChangeListener(ActionChangeListener changeListener) {
		action.setChangeListener(changeListener);
	}
}