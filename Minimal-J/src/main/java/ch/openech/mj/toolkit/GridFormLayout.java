package ch.openech.mj.toolkit;


public interface GridFormLayout extends AbstractComponentContainer {
	
	public void add(String caption, IComponent field);
	
	public void add(String caption, IComponent field, int span);

	public void addArea(String caption, IComponent field, int span);
	
}
