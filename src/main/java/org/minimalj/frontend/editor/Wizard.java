package org.minimalj.frontend.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.minimalj.application.Configuration;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.SwitchContent;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.page.IDialog;
import org.minimalj.model.validation.Validation;
import org.minimalj.model.validation.ValidationMessage;
import org.minimalj.util.ChangeListener;
import org.minimalj.util.ExceptionUtils;
import org.minimalj.util.mock.Mocking;

public abstract class Wizard<RESULT> extends Action {

	private static final Logger logger = Logger.getLogger(Wizard.class.getName());

	private Object stepObject;
	private WizardStep step;
	private Form form;
	private final EditorChangeListener changeListener = new EditorChangeListener();
	private final List<ValidationMessage> validationMessages = new ArrayList<>();
	private final FinishAction finishAction = new FinishAction();
	private final NextWizardStepAction nextAction = new NextWizardStepAction();
	private final PreviousWizardStepAction previousAction = new PreviousWizardStepAction();
	private final CancelAction cancelAction = new CancelAction();
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

		stepIndex = 0;
		step = getFirstStep();
		switchStep();
		
		dialog = Frontend.showDialog(getTitle(), switchContent, nextAction, cancelAction, createActions());
	}

	private Action[] createActions() {
		List<Action> additionalActions = createAdditionalActions();
		Action[] actions = new Action[additionalActions.size() + 4];
		int index;
		for (index = 0; index<additionalActions.size(); index++) {
			actions[index] = additionalActions.get(index);
		}
		actions[index++] = cancelAction;
		actions[index++] = previousAction;
		actions[index++] = nextAction;
		actions[index++] = finishAction;
		return actions;
	}
		
	protected List<Action> createAdditionalActions() {
		List<Action> actions = new ArrayList<Action>();
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
		
		validate(stepObject);
		
		switchContent.show(form.getContent());
		previousAction.setEnabled(stepIndex > 0);
	}
	
	protected abstract WizardStep<?> getFirstStep();
	
	private void validate(Object stepObject) {
		validationMessages.clear();
		if (stepObject instanceof Validation) {
			validationMessages.addAll(((Validation) stepObject).validateNullSafe());
		}
		Validator.validate(stepObject, validationMessages, form.getProperties());
		if (step instanceof Validation) {
			validationMessages.addAll(((Validation) step).validateNullSafe());
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
		try {
			RESULT result = save();
			dialog.closeDialog();
			finished(result);
		} catch (Exception x) {
			ExceptionUtils.logReducedStackTrace(logger, x);
			Frontend.showError(x.getLocalizedMessage());
			return;
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
			return valid && step.hasNext();
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
			form.setObject(stepObject);
		} else {
			form.mock();
		}
	}

}
