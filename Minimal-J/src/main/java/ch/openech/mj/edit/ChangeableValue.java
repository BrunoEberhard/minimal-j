package ch.openech.mj.edit;

import javax.swing.event.ChangeListener;


/**
 * "Change" means the value can be changed from something else than
 * setObject for example by the user. A setObject doesn't inform the
 * ChangeListener.<p>
 * 
 * A changeable must send exactly one ChangeEvent every time
 * the object of Changeabe changes.<p>
 * 
 */
public interface ChangeableValue<T> extends Value<T> {

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
