package ch.openech.mj.edit.validation;

/**
 * The validatable interface is only meant for inline classes (e.g. money).
 * Complete business classes should implement validation interface.
 * 
 */
public interface Validatable {

	/**
	 * 
	 * @return ValidationMessage or <code>null</code> if valid
	 */
	String validate();
	
}
