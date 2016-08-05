package org.minimalj.model.validation;

/**
 * The validatable interface is only meant for inline classes (e.g. money).
 * Complete business classes should implement validation interface.
 * 
 */
public interface Validatable {

	/**
	 * The returned String should already be localized. You get the current Locale
	 * from the LocaleContext class.
	 * 
	 * @return Localized String or <code>null</code> if valid
	 */
	String validate();
	
}