package org.minimalj.frontend.editor;

import java.util.List;

import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.model.validation.ValidationMessage;
import org.minimalj.util.ChangeListener;

/*
 * Could extend Page and then in a very complicate way TablePage could be
 * used as Page and as WizardPage. Would be very OO but not understandable anymore.
 * 
 */
public interface WizardStep<T> {

	public String getTitle();

	public IContent getContent();

	public void setChangeListener(ChangeListener<WizardStep<?>> changeListener);
	
	public List<ValidationMessage> getValidationMessages();
	
	public WizardStep<?> createNextStep();

	public WizardStep<?> createPreviousStep();
}