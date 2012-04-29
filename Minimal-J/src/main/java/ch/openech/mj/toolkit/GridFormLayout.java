package ch.openech.mj.toolkit;


public interface GridFormLayout extends ILayout {
	
	public void add(IComponent field);
	
	public void add(IComponent field, int span);

	public void addArea(IComponent field, int span);
	
}
