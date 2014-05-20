package org.minimalj.frontend.edit.fields;


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
	public void setChangeListener(EditFieldListener changeListener);

	public interface EditFieldListener {
		
	    void changed(EditField source);

	}

}
