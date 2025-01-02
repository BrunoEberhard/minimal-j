package org.minimalj.frontend.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.minimalj.application.Configuration;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.Frontend.SwitchContent;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.page.Page.Dialog;
import org.minimalj.model.validation.Validation;
import org.minimalj.model.validation.ValidationMessage;
import org.minimalj.util.ChangeListener;
import org.minimalj.util.ExceptionUtils;
import org.minimalj.util.mock.Mocking;

public abstract class Wizard<RESULT> extends Action implements Dialog {

	private static final Logger logger = Logger.getLogger(Wizard.class.getName());

	private final boolean oneDialog;
	private boolean dialogShown = false;
	private Object stepObject;
	private WizardStep step;
	private Form form;
	private final EditorChangeListener changeListener = new EditorChangeListener();
	private final List<ValidationMessage> validationMessages = new ArrayList<>();
	private final FinishAction finishAction = new FinishAction();
	private final NextWizardStepAction nextAction = new NextWizardStepAction();
	private final PreviousWizardStepAction previousAction = new PreviousWizardStepAction();
	private final CancelAction cancelAction = new CancelAction();
	private SwitchContent switchContent;
	private int stepIndex;
	
	public Wizard() {
		this(true);
	}
	
	public Wizard(boolean oneDialog) {
		super();
		this.oneDialog = oneDialog;
	}

	public String getTitle() {
		return getName();
	}
	
	@Override
	public IContent getContent() {
		return switchContent;
	}

	@Override
	public void run() {
		switchContent = Frontend.getInstance().createSwitchContent();

		stepIndex = 0;
		step = getFirstStep();
		switchStep();
	}
	
	@Override
	public List<Action> getActions() {
		List<Action> actions = new ArrayList<>();
		actions.addAll(createAdditionalActions());
		actions.add(cancelAction);
		actions.add(previousAction);
		actions.add(nextAction);
		actions.add(finishAction);
		return actions;
	}
	
	@Override
	public Action getSaveAction() {
		return finishAction;
	}
	
	@Override
	public Action getCancelAction() {
		return cancelAction;
	}
		
	protected List<Action> createAdditionalActions() {
		List<Action> actions = new ArrayList<>();
		if (Configuration.isDevModeActive()) {
			actions.add(new FillWithDemoDataAction());
		}
		return actions;
	}
	
	private void switchStep() {
		stepObject = step.createObject();

		form = step.createForm();
		form.setChangeListener(changeListener);
		form.setObject(stepObject);
		nextAction.setForm(form);
		
		validate(stepObject);
		
		switchContent.show(form.getContent());
		previousAction.setEnabled(stepIndex > 0);
		
		if (dialogShown) {
			if (!oneDialog) {
				Frontend.closeDialog(this);
				Frontend.showDialog(this);
			}
		} else {
			Frontend.showDialog(this);
			dialogShown = true;
		}
		
	}
	
	protected abstract WizardStep<?> getFirstStep();
	
	private void validate(Object stepObject) {
		validationMessages.clear();
		if (stepObject instanceof Validation) {
			validationMessages.addAll(((Validation) stepObject).validateNullSafe());
		}
		validationMessages.addAll(Validator.validate(stepObject));
		if (step instanceof Validation) {
			validationMessages.addAll(((Validation) step).validateNullSafe());
		}
				
		boolean relevantValidationMessage = form.indicate(validationMessages);
		nextAction.setInvalidFormElement(relevantValidationMessage);
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
		try {
			RESULT result = save();
			Frontend.closeDialog(this);
			finished(result);
		} catch (Exception x) {
			ExceptionUtils.logReducedStackTrace(logger, x);
			Frontend.showError(x.getLocalizedMessage());
		}
	}
	
	protected abstract RESULT save();
	
	protected void finished(RESULT result) {
		//
	}

	private class EditorChangeListener implements ChangeListener<Form<?>> {

		@Override
		public void changed(Form<?> form) {
			validate(stepObject);
			finishAction.setValidationMessages(validationMessages);
		}
	}	

	protected final class NextWizardStepAction extends ValidationAwareAction {
		private boolean invalidFormElement = false;
		
		@Override
		public void run() {
			next(stepObject);
		}

		public void setInvalidFormElement(boolean invalidFormElement) {
			this.invalidFormElement = invalidFormElement;
		}
		
		@Override
		public boolean isEnabled() {
			return !invalidFormElement && step.hasNext();
		}
	}
	
	protected final class PreviousWizardStepAction extends Action {
		@Override
		public void run() {
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
		private boolean enabled = false;
		
		@Override
		public void run() {
			finish();
		}
		
		public void setValidationMessages(List<ValidationMessage> validationMessages) {
			boolean valid = validationMessages == null || validationMessages.isEmpty();
			enabled = valid & canFinish();
			fireChange();
		}

		@Override
		public boolean isEnabled() {
			return enabled;
		}
	}
	
	private class CancelAction extends Action {
		@Override
		public void run() {
			cancel();
		}
	}
	
	public void cancel() {
		Frontend.closeDialog(this);
	}

	private class FillWithDemoDataAction extends Action {
		@Override
		public void run() {
			fillWithDemoData();
			validate(stepObject);
		}
	}
	
	protected void fillWithDemoData() {
		if (stepObject instanceof Mocking) {
			((Mocking) stepObject).mock();
			form.setObject(stepObject);
		} else {
			form.mock();
		}
	}

}
