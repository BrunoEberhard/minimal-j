package ch.openech.mj.edit.fields;

import javax.swing.event.ChangeListener;

public interface EditField<T> extends FormField<T> {

	/**
	 * Get the value
	 * 
	 * @return
	 */
	public T getObject();

	/**
	 * Set the ChangeListener<br>
	 * note: There can only be set <i>one</i> ChangeListener (strict dependency)
	 * 
	 * @param changeListener
	 */
	public void setChangeListener(ChangeListener changeListener);
	
}
