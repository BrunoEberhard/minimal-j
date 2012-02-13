package ch.openech.mj.toolkit;

import java.util.List;

import javax.swing.event.ChangeListener;

import ch.openech.mj.edit.fields.Focusable;
import ch.openech.mj.edit.validation.Indicator;

public interface ComboBox extends Indicator, Focusable {

	public void setObjects(List<?> object);
	
	public void setSelectedObject(Object object) throws IllegalArgumentException;

	public Object getSelectedObject();

	public void setChangeListener(ChangeListener changeListener);
	
	public void setEnabled(boolean enabled);

}
