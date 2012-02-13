package ch.openech.mj.toolkit;


public interface GridFormLayout extends AbstractComponentContainer {
	
	public void add(String caption, Object field);
	
	public void add(String caption, Object field, int span);

	public void addArea(String caption, Object field, int span);
	
}
