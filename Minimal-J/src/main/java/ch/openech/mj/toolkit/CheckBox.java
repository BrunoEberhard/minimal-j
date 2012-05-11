package ch.openech.mj.toolkit;


public interface CheckBox extends IComponent, Focusable {
	
	public void setSelected(boolean selected);

	public boolean isSelected();

	public void setEnabled(boolean enabled);

}
