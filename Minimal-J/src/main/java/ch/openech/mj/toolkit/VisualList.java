package ch.openech.mj.toolkit;

import java.util.List;

import ch.openech.mj.edit.fields.Focusable;
import ch.openech.mj.edit.validation.Indicator;

public interface VisualList extends IComponent, Indicator, Focusable {

	public void setObjects(List<?> object);

	public void setSelectedObject(Object object);

	public Object getSelectedObject();

	public int getSelectedIndex();
	
	// public void setChangeListener(ChangeListener changeListener);
	
	public void setEnabled(boolean enabled);

	public void setClickListener(ClickListener clickListener);
	
	public interface ClickListener {
		
		public void clicked();
		
	}
}
