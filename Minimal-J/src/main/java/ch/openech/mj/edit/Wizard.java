package ch.openech.mj.edit;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;

import ch.openech.mj.application.DevMode;
import ch.openech.mj.edit.form.IForm;
import ch.openech.mj.edit.form.SwitchForm;
import ch.openech.mj.edit.validation.Indicator;
import ch.openech.mj.edit.validation.ValidationMessage;
import ch.openech.mj.resources.ResourceHelper;
import ch.openech.mj.resources.Resources;
import ch.openech.mj.toolkit.ClientToolkit;

public abstract class Wizard<T> extends Editor<T> {

	private static final String NEXT = "+";
	private static final String PREV = "-";
	
	private WizardStep<?> currentStep;
	private int currentStepIndex = 0;
	
	protected final Action prevAction;
	protected final Action nextAction;
	protected final Action finishAction;
	private SwitchForm<T> switchForm;
	private final Indicator indicator;
	private final EditorFinishedListener stepFinishedListener;
	
	protected Wizard() {
		nextAction = createNextAction();
		prevAction = createPrevAction();
		finishAction = createFinishAction();
		indicator = new WizardIndicator();
		stepFinishedListener = new WizardStepFinishedListener();
	}

	protected abstract WizardStep<?> getFirstStep();
	
	@Override
	public Action[] getActions() {
		if (DevMode.isActive()) {
			return new Action[]{demoAction(), cancelAction(), prevAction, nextAction, finishAction};
		} else {
			return new Action[]{cancelAction(), prevAction, nextAction, finishAction};
		}
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
				currentStep.setFollowLink(PREV);
				currentStep.finish();
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

	private void setCurrentStep(WizardStep<?> step) {
		currentStep = step;
		currentStep.setIndicator(indicator);
		currentStep.setEditorFinishedListener(stepFinishedListener);
		currentStep.setFollowLink(NEXT);
 
		IForm<?> form = currentStep.startEditor();
		switchForm.setForm(form);
		ClientToolkit.getToolkit().focusFirstComponent(form.getComponent());
		
		prevAction.setEnabled(currentStepIndex > 0);
	}
	
	protected void commit() {
		if (currentStep.isSaveable()) {
			currentStep.save();
		}
	}

	@Override
	protected abstract boolean save(T object) throws Exception;
	
	private class WizardIndicator implements Indicator {

		@Override
		public void setValidationMessages(List<ValidationMessage> validationResult) {
			nextAction.setEnabled(validationResult.isEmpty() && currentStep.getNextStep() != null);
			finishAction.setEnabled(validationResult.isEmpty() && currentStep.canFinish());
		}
	}
	
	private class WizardStepFinishedListener implements EditorFinishedListener {
		@Override
		public void progress(int value, int maximum) {
			// ignored
		}

		@Override
		public void finished(String followLink) {
			// nobug: compare to this instanceof of "+" / "-"
			if (followLink == NEXT) {
				WizardStep<?> nextStep = currentStep.getNextStep();
				if (nextStep != null) {
					currentStepIndex++;
					setCurrentStep(nextStep);
				} else {
					save();
				}
			} else if (followLink == PREV) {
				currentStepIndex--;
				setCurrentStep(currentStep.getPreviousStep());
			} // else cancel
		}
	}

	@Override
	public void fillWithDemoData() {
		currentStep.fillWithDemoData();
	}
	 
}
