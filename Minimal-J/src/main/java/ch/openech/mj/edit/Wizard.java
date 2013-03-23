package ch.openech.mj.edit;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;

import ch.openech.mj.edit.form.IForm;
import ch.openech.mj.edit.form.SwitchForm;
import ch.openech.mj.edit.validation.Indicator;
import ch.openech.mj.edit.validation.ValidationMessage;
import ch.openech.mj.resources.ResourceHelper;
import ch.openech.mj.resources.Resources;

public abstract class Wizard<T> extends Editor<T> {

	private WizardStep<?> currentStep;
	private int currentStepIndex = 0;
	
	protected final Action prevAction;
	protected final Action nextAction;
	protected final Action finishAction;
	private SwitchForm<T> switchForm;
	private final Indicator indicator;
	
	protected Wizard() {
		nextAction = createNextAction();
		prevAction = createPrevAction();
		finishAction = createFinishAction();
		indicator = new WizardIndicator();
	}

	protected abstract WizardStep<?> getFirstStep();
	
	@Override
	public Action[] getActions() {
		return new Action[]{demoAction(), cancelAction(), prevAction, nextAction, finishAction};
	}

	protected int getCurrentStepIndex() {
		return currentStepIndex;
	}
	
	protected Action createNextAction() {
		Action action = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentStep.isSaveable()) {
					currentStep.save();
					currentStepIndex++;
					setCurrentStep(currentStep.getNextStep());
				}
			}
		};
		ResourceHelper.initProperties(action, Resources.getResourceBundle(), "NextWizardStepAction");
		return action;
	}

	protected Action createPrevAction() {
		Action action = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentStep.finish();
				currentStepIndex--;
				setCurrentStep(currentStep.getPreviousStep());
			}
		};
		ResourceHelper.initProperties(action, Resources.getResourceBundle(), "PreviousWizardStepAction");
		return action;
	}

	protected final Action createFinishAction() {
		Action action = new SaveAction("OkAction") {
			@Override
			public void actionPerformed(ActionEvent e) {
				currentStep.save();
				save();
			}
		};
		return action;
	}
	
	@Override
	protected final IForm<T> createForm() {
		switchForm = new SwitchForm<T>();
		return switchForm;
	}
	
	@Override
	public IForm<T> startEditor() {
		IForm<T> formVisual = super.startEditor();
		setCurrentStep(getFirstStep());
		return formVisual;
	}

	@Override
	public void finish() {
		currentStep.finish();
		super.finish();
	}

	private void setCurrentStep(WizardStep<?> step) {
		currentStep = step;
		currentStep.setIndicator(indicator);
		switchForm.setForm(currentStep.startEditor());
		prevAction.setEnabled(currentStepIndex > 0);
	}

	@Override
	protected abstract boolean save(T object) throws Exception;
	
	private class WizardIndicator implements Indicator {

		@Override
		public void setValidationMessages(List<ValidationMessage> validationResult) {
			nextAction.setEnabled(validationResult.isEmpty());
			finishAction.setEnabled(validationResult.isEmpty() && currentStep.canFinish());
		}
	}

	@Override
	public void fillWithDemoData() {
		currentStep.fillWithDemoData();
	}
	 
}
