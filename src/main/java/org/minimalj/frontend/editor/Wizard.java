package org.minimalj.frontend.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.minimalj.application.DevMode;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.SwitchContent;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.page.IDialog;
import org.minimalj.model.validation.ValidationMessage;
import org.minimalj.util.ChangeListener;
import org.minimalj.util.ExceptionUtils;
import org.minimalj.util.mock.Mocking;

public abstract class Wizard<RESULT> extends Action {

	private static final Logger logger = Logger.getLogger(Wizard.class.getName());

	private Object stepObject;
	private WizardStep<?> step;
	private final StepChangeListener changeListener = new StepChangeListener();
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
		if (DevMode.isActive()) {
			actions.add(new FillWithDemoDataAction());
		}
		return actions;
	}
	
	private void switchStep() {
		step.setChangeListener(changeListener);
		
		switchContent.show(step.getContent());
		previousAction.setEnabled(stepIndex > 0);
	}
	
	protected abstract WizardStep<?> getFirstStep();
	
	private void next(Object object) {
		stepIndex++;
		step = step.createNextStep();
		switchStep();
	}

	private void previous() {
		stepIndex--;
		step = step.createPreviousStep();
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

	private class StepChangeListener implements ChangeListener<WizardStep<?>> {

		@Override
		public void changed(WizardStep<?> wizardStep) {
			List<ValidationMessage> validationMessages = wizardStep.getValidationMessages();
			nextAction.setValidationMessages(validationMessages);
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
	
	protected WizardStep<?> getStep() {
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
			if (step instanceof Mocking) {
				((Mocking) step).mock();
			}
		}
	}

}
