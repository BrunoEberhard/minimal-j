package ch.openech.mj.edit.validation;




public interface Validatable {

	/**
	 * 
	 * @param key Key of the field to validate or <code>null</code> to validate entier object
	 * @return ValidationMessage or <code>null</code> if valid
	 */
	String validate();
	
}
