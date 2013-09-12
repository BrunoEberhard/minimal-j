package ch.openech.mj.edit;

import java.util.List;

import ch.openech.mj.application.DevMode;
import ch.openech.mj.edit.form.IForm;
import ch.openech.mj.edit.form.SwitchForm;
import ch.openech.mj.edit.validation.Indicator;
import ch.openech.mj.edit.validation.ValidationMessage;
import ch.openech.mj.toolkit.IAction;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.ResourceActionEnabled;

public abstract class Wizard<T> extends Editor<T> {

	private WizardStep<?> currentStep;
	private int currentStepIndex = 0;
	
	protected final PreviousWizardStepAction prevAction;
	protected final NextWizardStepAction nextAction;
	private SwitchForm<T> switchForm;
	private final Indicator indicator;
	private final EditorListener stepFinishedListener;
	
	protected Wizard() {
		nextAction = new NextWizardStepAction();
		prevAction = new PreviousWizardStepAction();
		indicator = new WizardIndicator();
		stepFinishedListener = new WizardStepFinishedListener();
	}

	protected abstract WizardStep<?> getFirstStep();
	
	@Override
	public IAction[] getActions() {
		if (DevMode.isActive()) {
			return new IAction[]{demoAction, cancelAction, prevAction, nextAction, saveAction};
		} else {
			return new IAction[]{cancelAction, prevAction, nextAction, saveAction};
		}
	}

	@Override
	public void save() {
		// WizardFinishedListener will trigger save of wizard object
		currentStep.setEditorListener(new WizardFinishedListener());
		currentStep.save();
	}

	protected int getCurrentStepIndex() {
		return currentStepIndex;
	}
	
	private class NextWizardStepAction extends ResourceActionEnabled {
		@Override
		public void action(IComponent context) {
			currentStep.save();
		}
	}

	private class PreviousWizardStepAction extends ResourceActionEnabled {
		@Override
		public void action(IComponent context) {
			currentStep.cancel();
		}
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

	private void setCurrentStep(WizardStep<?> step) {
		currentStep = step;
		currentStep.setIndicator(indicator);
		currentStep.setEditorListener(stepFinishedListener);
 
		IForm<?> form = currentStep.startEditor();
		switchForm.setForm(form);
		// ClientToolkit.getToolkit().focusFirstComponent(form.getComponent());
		
		prevAction.setEnabled(currentStepIndex > 0);
	}
	
	protected void commit() {
		if (currentStep.isSaveable()) {
			currentStep.save();
		}
	}
	
	@Override
	protected void finish() {
		currentStep.finish();
		super.finish();
	}

	@Override
	protected abstract Object save(T object) throws Exception;
	
	private class WizardIndicator implements Indicator {

		@Override
		public void setValidationMessages(List<ValidationMessage> validationResult) {
			nextAction.setEnabled(validationResult.isEmpty() && currentStep.getNextStep() != null);
			saveAction.setEnabled(validationResult.isEmpty() && currentStep.canFinish());
		}
	}
	
	private class WizardStepFinishedListener implements EditorListener {

		@Override
		public void saved(Object saveResult) {
			WizardStep<?> nextStep = currentStep.getNextStep();
			currentStepIndex++;
			setCurrentStep(nextStep);
		}

		@Override
		public void canceled() {
			currentStepIndex--;
			setCurrentStep(currentStep.getPreviousStep());
		}
	}
	
	private class WizardFinishedListener implements EditorListener {

		@Override
		public void saved(Object saveResult) {
			Wizard.super.save();
		}

		@Override
		public void canceled() {
			// not used
		}
	}
	
	@Override
	public void fillWithDemoData() {
		currentStep.fillWithDemoData();
	}
	 
}
