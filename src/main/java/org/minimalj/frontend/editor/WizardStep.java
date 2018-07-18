package org.minimalj.frontend.editor;

import org.minimalj.frontend.form.Form;

/**
 * Named <i>Step</i> so not to be confused with the concept of the pages.
 * 
 */
public interface WizardStep<T> {

	public String getTitle();

	public T createObject();

	public Form<T> createForm();

	/**
	 * 
	 * @return true if getNextStep will not return <code>null</code>
	 */
	public boolean hasNext();

	public WizardStep<?> getNextStep();

	public WizardStep<?> getPreviousStep();
}