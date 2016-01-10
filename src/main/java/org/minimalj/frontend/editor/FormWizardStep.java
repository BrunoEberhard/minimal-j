package org.minimalj.frontend.editor;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.form.Form;
import org.minimalj.model.validation.Validation;
import org.minimalj.model.validation.ValidationMessage;
import org.minimalj.util.ChangeListener;
import org.minimalj.util.mock.Mocking;

public abstract class FormWizardStep<T> implements WizardStep<T>, Mocking {
	private final Form<T> form;
	private final List<ValidationMessage> validationMessages = new ArrayList<>();
	private final T stepObject;
	private ChangeListener<WizardStep<?>> changeListener;
	
	public FormWizardStep() {
		stepObject = createObject();
		form = createForm();
		validate();
		
		form.setChangeListener(new ChangeListener<Form<?>>() {
			@Override
			public void changed(Form<?> source) {
				validate();
			}
		});
		form.setObject(stepObject);
	}
	
	public abstract Form<T> createForm();
	
	public abstract T createObject();
	
	@Override
	public IContent getContent() {
		return form.getContent();
	}
	
	@Override
	public List<ValidationMessage> getValidationMessages() {
		return validationMessages;
	}
	
	public void validate() {
		validationMessages.clear();
		if (stepObject instanceof Validation) {
			((Validation) stepObject).validate(validationMessages);
		}
		ObjectValidator.validate(stepObject, validationMessages, form.getProperties());
		validate(stepObject, validationMessages);
		form.indicate(validationMessages);
		if (changeListener != null) {
			changeListener.changed(FormWizardStep.this);
		}
	}
	
	protected void validate(T object, List<ValidationMessage> validationMessages) {
		// 
	}
	
	@Override
	public void setChangeListener(ChangeListener<WizardStep<?>> changeListener) {
		this.changeListener = changeListener;
	}
	
	@Override
	public void mock() {
		if (stepObject instanceof Mocking) {
			((Mocking) stepObject).mock();
			form.setObject(stepObject);
		} else {
			form.mock();
		}
		validate();
	}
}