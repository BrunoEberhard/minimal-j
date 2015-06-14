package org.minimalj.frontend.editor;

import org.minimalj.frontend.form.Form;

/**
 * Named <i>Step</i> so not to be confused with the concept of the pages.
 * 
 */
public interface WizardStep<T> {

	public String getTitle();

	public String getDescription();

	public T createObject();

	public Form<T> createForm();

	public WizardStep<?> getNextStep();

	public WizardStep<?> getPreviousStep();

}