package ch.openech.mj.toolkit;

import java.util.List;

import ch.openech.mj.edit.fields.Focusable;

public interface VisualTable<T> extends Focusable {

	public void setObjects(List<T> object);

	public void setSelectedObject(T object);

	public T getSelectedObject();

	public int getSelectedIndex();
	
	public void setClickListener(ClickListener clickListener);
	
	public interface ClickListener {
		
		public void clicked();
		
	}
	
}
