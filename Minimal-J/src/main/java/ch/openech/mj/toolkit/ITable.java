package ch.openech.mj.toolkit;

import java.awt.event.ActionListener;
import java.util.List;


public interface ITable<T> extends IComponent {

	public void setObjects(List<T> object);

	public List<T> getSelectedObjects();

	public T getSelectedObject();

	public void setClickListener(ActionListener listener);
	public void setDeleteListener(ActionListener listener);
	public void setInsertListener(ActionListener listener);
	public void setFunctionListener(int function, ActionListener listener);
	
}
