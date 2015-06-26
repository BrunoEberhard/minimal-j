package org.minimalj.frontend.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.minimalj.application.DevMode;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.toolkit.Action;
import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.frontend.toolkit.ClientToolkit.ConfirmDialogResult;
import org.minimalj.frontend.toolkit.ClientToolkit.ConfirmDialogType;
import org.minimalj.frontend.toolkit.ClientToolkit.DialogListener;
import org.minimalj.frontend.toolkit.ClientToolkit.SwitchContent;
import org.minimalj.frontend.toolkit.IDialog;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.model.validation.Validatable;
import org.minimalj.model.validation.Validation;
import org.minimalj.model.validation.ValidationMessage;
import org.minimalj.util.GenericUtils;
import org.minimalj.util.mock.Mocking;
import org.minimalj.util.resources.Resources;

public abstract class Wizard<RESULT> extends Action {

	private static final Logger logger = Logger.getLogger(Wizard.class.getName());

	private Object stepObject;
	private WizardStep step;
	private Form form;
	private final EditorChangeListener changeListener = new EditorChangeListener();
	private final List<ValidationMessage> validationMessages = new ArrayList<>();
	private FinishAction finishAction;
	private NextAction nextAction;
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
		// specific name of editor
		if (Resources.isAvailable(getClass().getName())) {
			return Resources.getString(getClass().getName());
		} 

		// specific name of edited class
		Class<?> clazz = GenericUtils.getGenericClass(getClass());
		if (clazz != null && Resources.isAvailable(clazz.getName())) {
			return Resources.getString(getClass().getName());
		}
		
		// simple name of editor
		if (clazz == null || Resources.isAvailable(getClass().getSimpleName())) {
			return Resources.getString(getClass().getSimpleName());
		}
		
		// simple name of edited class or default
		return Resources.getString(clazz);
	}

	@Override
	public void action() {
		switchContent = ClientToolkit.getToolkit().createSwitchContent();

		CancelAction cancelAction = new CancelAction();
		PreviousAction previousAction = new PreviousAction();
		nextAction = new NextAction();
		finishAction = new FinishAction();

		stepIndex = 0;
		step = getFirstStep();
		switchStep();
		
		if (DevMode.isActive()) {
			FillWithDemoDataAction demoAction = new FillWithDemoDataAction();
			dialog = ClientToolkit.getToolkit().showDialog(getTitle(), switchContent, nextAction, cancelAction, demoAction, cancelAction, previousAction, nextAction, finishAction);
		} else {
			dialog = ClientToolkit.getToolkit().showDialog(getTitle(), switchContent, nextAction, cancelAction, cancelAction, previousAction, nextAction, finishAction);
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
	
	private void validate(Object object) {
		validationMessages.clear();
		if (object instanceof Validation) {
			((Validation) object).validate(validationMessages);
		}
		ObjectValidator.validateForEmpty(object, validationMessages, form.getProperties());
		ObjectValidator.validateForInvalid(object, validationMessages, form.getProperties());
		ObjectValidator.validatePropertyValues(object, validationMessages, form.getProperties());
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

	protected final class NextAction extends Action {
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
	
	protected final class PreviousAction extends Action {
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
		DialogListener listener = new DialogListener() {
			@Override
			public void close(ConfirmDialogResult answer) {
				if (answer == ConfirmDialogResult.YES) {
					dialog.closeDialog();
				} 
			}
		};
		ClientToolkit.getToolkit().showConfirmDialog("Soll der Wizard abgebrochen und alle Eingaben verworfen werden?", "Schliessen",
				ConfirmDialogType.YES_NO, listener);
	}

	
	private class FillWithDemoDataAction extends Action {
		public void action() {
			fillWithDemoData();
		}
	}
	
	protected void fillWithDemoData() {
		if (stepObject instanceof Mocking) {
			((Mocking) stepObject).mock();
			// re-set the object to update the FormFields
			form.setObject(stepObject);
		} else if (form instanceof Mocking) {
			((Mocking) form).mock();
		}
	}

}
