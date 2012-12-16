package ch.openech.mj.edit.form;

import java.util.Collection;
import java.util.List;

import javax.swing.Action;
import javax.swing.event.ChangeListener;

import ch.openech.mj.db.model.PropertyInterface;
import ch.openech.mj.toolkit.IComponent;

public interface IForm<T> {

	public IComponent getComponent();
	
	public abstract boolean isResizable();
	
	public void setSaveAction(Action saveAction);

	public Collection<PropertyInterface> getProperties();
	
	public void setObject(T value);

	public void setValidationMessage(PropertyInterface property, List<String> validationMessages);

	/**
	 * Set the ChangeListener<br>
	 * note: There can only be set <i>one</i> ChangeListener (strict dependency)
	 * 
	 * @param changeListener
	 */
	public void setChangeListener(ChangeListener changeListener);

}