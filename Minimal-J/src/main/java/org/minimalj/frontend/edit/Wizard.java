package org.minimalj.frontend.edit;

import java.util.List;

import org.minimalj.application.DevMode;
import org.minimalj.frontend.edit.form.Form;
import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.frontend.toolkit.ClientToolkit.IContent;
import org.minimalj.frontend.toolkit.ClientToolkit.SwitchContent;
import org.minimalj.frontend.toolkit.FormContent;
import org.minimalj.frontend.toolkit.IAction;
import org.minimalj.frontend.toolkit.ResourceActionEnabled;
import org.minimalj.model.validation.ValidationMessage;

public abstract class Wizard<T> extends Editor<T> {

	private WizardStep<?> currentStep;
	private int currentStepIndex = 0;
	
	protected final PreviousWizardStepAction prevAction;
	protected final NextWizardStepAction nextAction;
	private SwitchContent wizardContent;
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
		public void action() {
			currentStep.save();
		}
	}

	private class PreviousWizardStepAction extends ResourceActionEnabled {
		@Override
		public void action() {
			currentStep.cancel();
		}
	}

	@Override
	protected final Form<T> createForm() {
		return null;
	}
	
	@Override
	public void startEditor() {
		super.startEditor();
		wizardContent = ClientToolkit.getToolkit().createSwitchContent();
		setCurrentStep(getFirstStep());
	}

	@Override
	public IContent getContent() {
		return wizardContent;
	}
	
	private void setCurrentStep(WizardStep<?> step) {
		currentStep = step;
		currentStep.setIndicator(indicator);
		currentStep.setEditorListener(stepFinishedListener);
 
		currentStep.startEditor();
		wizardContent.show((FormContent) currentStep.getContent());
		
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
