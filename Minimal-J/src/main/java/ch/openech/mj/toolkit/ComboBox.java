package ch.openech.mj.toolkit;

import java.util.List;

import ch.openech.mj.edit.fields.Focusable;
import ch.openech.mj.edit.validation.Indicator;

public interface ComboBox extends IComponent, Indicator, Focusable {

	public void setObjects(List<?> object);
	
	public void setSelectedObject(Object object) throws IllegalArgumentException;

	public Object getSelectedObject();

	public void setEnabled(boolean enabled);

}
