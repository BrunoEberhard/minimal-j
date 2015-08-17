package org.minimalj.frontend.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.minimalj.application.DevMode;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.SwitchContent;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.page.IDialog;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.model.validation.Validatable;
import org.minimalj.model.validation.Validation;
import org.minimalj.model.validation.ValidationMessage;
import org.minimalj.util.mock.Mocking;

public abstract class Wizard<RESULT> extends Action {

	private static final Logger logger = Logger.getLogger(Wizard.class.getName());

	private Object stepObject;
	private WizardStep step;
	private Form form;
	private final EditorChangeListener changeListener = new EditorChangeListener();
	private final List<ValidationMessage> validationMessages = new ArrayList<>();
	private FinishAction finishAction;
	private NextWizardStepAction nextAction;
	private IDialog dialog;
	private SwitchContent switchContent;
	private int stepIndex;
	
	public Wizard() {
		super();
	}

	public Wizard(String actionName) {
		super(actionName);
	}

	public String getTitle() {
		return getName();
	}

	@Override
	public void action() {
		switchContent = Frontend.getInstance().createSwitchContent();

		CancelAction cancelAction = new CancelAction();
		PreviousWizardStepAction previousAction = new PreviousWizardStepAction();
		nextAction = new NextWizardStepAction();
		finishAction = new FinishAction();

		stepIndex = 0;
		step = getFirstStep();
		switchStep();
		
		if (DevMode.isActive()) {
			FillWithDemoDataAction demoAction = new FillWithDemoDataAction();
			dialog = Frontend.getBrowser().showDialog(getTitle(), switchContent, nextAction, cancelAction, demoAction, cancelAction, previousAction, nextAction, finishAction);
		} else {
			dialog = Frontend.getBrowser().showDialog(getTitle(), switchContent, nextAction, cancelAction, cancelAction, previousAction, nextAction, finishAction);
		}
	}

	private void switchStep() {
		stepObject = step.createObject();

		form = step.createForm();
		form.setChangeListener(changeListener);
		form.setObject(stepObject);
		
		validate(stepObject);
		
		switchContent.show(form.getContent());
	}
	
	protected abstract WizardStep<?> getFirstStep();
	
	private void validate(Object stepObject) {
		validationMessages.clear();
		if (stepObject instanceof Validation) {
			((Validation) stepObject).validate(validationMessages);
		}
		ObjectValidator.validateForEmpty(stepObject, validationMessages, form.getProperties());
		ObjectValidator.validateForInvalid(stepObject, validationMessages, form.getProperties());
		ObjectValidator.validatePropertyValues(stepObject, validationMessages, form.getProperties());
		if (step instanceof Validatable) {
			((Validatable) step).validate();
		}
		form.indicate(validationMessages);
		nextAction.setValidationMessages(validationMessages);
		finishAction.setValidationMessages(validationMessages);
	}
	
	private void next(Object object) {
		stepIndex++;
		step = step.getNextStep();
		switchStep();
	}

	private void previous() {
		stepIndex--;
		step = step.getPreviousStep();
		switchStep();
	}
	
	protected int getStepIndex() {
		return stepIndex;
	}
	
	private void finish() {
		RESULT result = save();
		dialog.closeDialog();
		finished(result);
	}
	
	protected abstract RESULT save();
	
	protected void finished(RESULT result) {
		//
	}

	private class EditorChangeListener implements Form.FormChangeListener {

		@Override
		public void changed(PropertyInterface property, Object newValue) {
			validate(stepObject);
			finishAction.setValidationMessages(validationMessages);
		}

		@Override
		public void commit() {
			if (validationMessages.isEmpty()) {
				next(stepObject);
			}
		}
	}	

	protected final class NextWizardStepAction extends Action {
		private String description;
		private boolean valid = false;
		
		@Override
		public void action() {
			next(stepObject);
		}
		
		public void setValidationMessages(List<ValidationMessage> validationMessages) {
			valid = validationMessages == null || validationMessages.isEmpty();
			if (valid) {
				description = "Speichern und zum nächsten Schritt";
			} else {
				description = ValidationMessage.formatHtml(validationMessages);
			}
			fireChange();
		}

		@Override
		public boolean isEnabled() {
			return valid;
		}
		
		@Override
		public String getDescription() {
			return description;
		}
	}
	
	protected final class PreviousWizardStepAction extends Action {
		@Override
		public void action() {
			previous();
		}
	}
	
	protected WizardStep getStep() {
		return step;
	}
	
	protected boolean canFinish() {
		return true;
	}
	
	protected final class FinishAction extends Action {
		private String description;
		private boolean enabled = false;
		
		@Override
		public void action() {
			finish();
		}
		
		public void setValidationMessages(List<ValidationMessage> validationMessages) {
			boolean valid = validationMessages == null || validationMessages.isEmpty();
			if (valid) {
				description = "Eingaben speichern und Wizard beenden";
			} else {
				description = ValidationMessage.formatHtml(validationMessages);
			}
			enabled = valid & canFinish();
			fireChange();
		}

		@Override
		public boolean isEnabled() {
			return enabled;
		}
		
		@Override
		public String getDescription() {
			return description;
		}
	}
	
	private class CancelAction extends Action {
		@Override
		public void action() {
			cancel();
		}
	}
	
	public void cancel() {
		dialog.closeDialog();
	}

	private class FillWithDemoDataAction extends Action {
		@Override
		public void action() {
			fillWithDemoData();
			validate(stepObject);
		}
	}
	
	protected void fillWithDemoData() {
		if (stepObject instanceof Mocking) {
			((Mocking) stepObject).mock();
		} else {
			form.mock();
		}
	}

}
