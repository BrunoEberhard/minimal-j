package ch.openech.mj.toolkit;

public abstract class ResourceActionEnabled extends ResourceAction {

	private boolean enabled = true;
	private ActionChangeListener changeListener;

	public ResourceActionEnabled() {
		super(null);
	}
	
	public ResourceActionEnabled(String string) {
		super(string);
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		fireChange();
	}

	protected void fireChange() {
		if (changeListener != null) {
			changeListener.change();
		}
	}
	
	@Override
	public void setChangeListener(ActionChangeListener changeListener) {
		this.changeListener = changeListener;
	}

}
