package ch.openech.mj.edit.validation;

public interface Validatable {

	/**
	 * 
	 * @return ValidationMessage or <code>null</code> if valid
	 */
	String validate();
	
}
