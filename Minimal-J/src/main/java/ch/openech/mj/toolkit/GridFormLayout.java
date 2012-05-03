package ch.openech.mj.toolkit;


public interface GridFormLayout extends IComponent {
	
	public void add(IComponent field);
	
	public void add(IComponent field, int span);

	public void addArea(IComponent field, int span);
	
}
