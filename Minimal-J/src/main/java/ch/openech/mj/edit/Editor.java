package ch.openech.mj.edit;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.JOptionPane;

import ch.openech.mj.autofill.DemoEnabled;
import ch.openech.mj.edit.form.IForm;
import ch.openech.mj.edit.validation.Indicator;
import ch.openech.mj.edit.validation.Validatable;
import ch.openech.mj.edit.validation.Validation;
import ch.openech.mj.edit.validation.ValidationMessage;
import ch.openech.mj.edit.value.CloneHelper;
import ch.openech.mj.model.EmptyValidator;
import ch.openech.mj.model.InvalidValues;
import ch.openech.mj.model.PropertyInterface;
import ch.openech.mj.model.annotation.Required;
import ch.openech.mj.resources.ResourceAction;
import ch.openech.mj.resources.Resources;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.ConfirmDialogListener;

/**
 * An <code>Editor</code> knows
 * <UL>
 * <LI>How to build the FormPanel containing FormField s
 * <LI>How to load an Object
 * <LI>How to validate the object
 * <LI>How to save the object
 * <LI>What additional Actions the Editor provides
 * <LI>Its name (for Window Title oder Tab Title)
 * </UL>

 * @startuml 
 * [*] --> Started: start
 * Started: Form is created
 * CheckClose: User is asked if he\nbreally wants to cancel
 * Finished: Form is null again
 * Started --> CheckClose : cancel or\nclose Window
 * CheckClose --> Started : cancel\naborted
 * CheckClose --> Finished : cancel confirmed
 * Finished --> Started : restart
 * Started --> Save : save
 * Save --> Finished : save
 * 
 * @enduml
 * 
 * @startuml sequence_editor.png
 * == Starting ==
 * EditorPage -> Editor: start
 * Editor -> SpecificEditor: create Form
 * Editor -> SpecificEditor: load or create Object
 * Editor -> Form: set Object to Form
 * == Editing ==
 * Form -> Editor: fire change
 * Editor -> Object: set value of Field
 * Editor -> SpecificEditor: validate
 * Editor -> Form: validate
 * Editor -> Object: validate
 * Editor -> Form: indicate
 * Editor -> EditorPage: indicate
 * == Finishing ==
 * Editor -> SpecificEditor: save
 * Editor -> EditorPage: fire finished
 * @enduml

 * @author Bruno
 * 
 * @param <T>
 *            Class of the edited Object
 */
public abstract class Editor<T> {

	private static final Logger logger = Logger.getLogger(Editor.class.getName());

	private T original, editedObject;
	private IForm<T> form;
	private SaveAction saveAction;
	private EditorFinishedListener editorFinishedListener;
	private final Map<PropertyInterface, String> propertyValidations = new HashMap<>();
	private Indicator indicator;
	private boolean userEdited;
	private String followLink;
	
	// what to implement

	protected abstract IForm<T> createForm();

	/**
	 * Should load the object to be edited. Note: The object will be copied before
	 * changed by the editor
	 * 
	 * @return null if newInstance() should be used
	 */
	protected T load() {
		return null;
	}

	protected abstract boolean save(T object) throws Exception;

	protected boolean isSaveSynchron() {
		return true;
	}
	
	public String getTitle() {
		String resourceName = getClass().getSimpleName() + ".text";
		return Resources.getString(resourceName);
	}

	public Action[] getActions() {
		if (System.getProperty("MjDevMode", "false").equals("true")) {
			return new Action[] { demoAction(), cancelAction(), saveAction() };
		} else {
			return new Action[] { cancelAction(), saveAction() };
		}
	}

	// /////

	protected Editor() {
	}
	
	public IForm<T> startEditor() {
		if (form != null) {
			throw new IllegalStateException();
		}
		form = createForm();
		
		original = load();
		if (original != null) {
			editedObject = CloneHelper.clone(original);
		} else {
			editedObject = newInstance();
		}
		form.setObject(editedObject);
		updateValidation();

		userEdited = false;
		form.setChangeListener(new EditorChangeListener());
		
		return form;
	}
	
	/**
	 * Override this method to preset values for the editor
	 * 
	 * @return The object this editor should edit.
	 */
	protected T newInstance() {
		@SuppressWarnings("unchecked")
		Class<T> clazz = (Class<T>) ch.openech.mj.util.GenericUtils.getGenericClass(Editor.this.getClass());
		T newInstance = CloneHelper.newInstance(clazz);
		return newInstance;
	}
	
	public final void setEditorFinishedListener(EditorFinishedListener editorFinishedListener) {
		this.editorFinishedListener = editorFinishedListener;
	}
	
	public final void setIndicator(Indicator indicator) {
		this.indicator = indicator;
	}

	/**
	 * 
	 * Disposes the editor and calls the editorFinished Listener
	 */
	protected void finish() {
		fireEditorFinished();
		form = null;
	}
	
	public final boolean isFinished() {
		return form == null;
	}
	
	private void fireEditorFinished() {
		if (editorFinishedListener != null) {
			editorFinishedListener.finished(followLink);
		}
	}
	
	protected final void setFollowLink(String followLink) {
		this.followLink = followLink;
	}
	
	protected final T getObject() {
		return editedObject;
	}
	
	protected final void save() {
		if (isSaveable()) {
			if (isSaveSynchron()) {
				doSave();
			} else {
				progress(0, 100);
				new Thread(new Runnable() {
					@Override
					public void run() {
						doSave();
					}
				}).start();
			}
		} else {
			ClientToolkit.getToolkit().showNotification(form.getComponent(), "Abschluss nicht möglich.\n\nBitte Eingaben überprüfen.");
		}
	}

	private void doSave() {
		try {
			T objectToSave;
			if (original != null) {
				objectToSave = original;
				CloneHelper.deepCopy(getObject(), original);
			} else {
				objectToSave = getObject();
			}
			if (save(objectToSave)) {
				finish();
			}
		} catch (Exception x) {
			x.printStackTrace();
			ClientToolkit.getToolkit().showNotification(form.getComponent(), "Abschluss fehlgeschlagen: " + x.getLocalizedMessage());
		}
	}
	
	public final void progress(int value, int maximum) {
		if (editorFinishedListener != null) {
			editorFinishedListener.progress(value, maximum);
		}
	}
	
	private class EditorChangeListener implements IForm.FormChangeListener {

		public void stateChanged(PropertyInterface property, Object newValue) {

			if (logger.isLoggable(Level.FINE)) {
				logger.fine(property.getFieldPath() + " changed to " + newValue);
			}
			
			property.setValue(editedObject, newValue);
			
			propertyValidations.remove(property);
			if (newValue instanceof Validatable) {
				String validationMessage = ((Validatable) newValue).validate();
				if (validationMessage != null) {
					propertyValidations.put(property, validationMessage);
				}
			}
			updateValidation();
			
			userEdited = true;
		}

		@Override
		public void commit() {
			if (isSaveable()) {
				save();
			}
		}
	}
	
	private void updateValidation() {
		List<ValidationMessage> validationMessages = new ArrayList<>();
		if (editedObject instanceof Validation) {
			((Validation) editedObject).validate(validationMessages);
		}
		for (Map.Entry<PropertyInterface, String> entry : propertyValidations.entrySet()) {
			validationMessages.add(new ValidationMessage(entry.getKey(), entry.getValue()));
		}
		validateForEmpty(validationMessages);
		validateForInvalid(validationMessages);
		validate(editedObject, validationMessages);
		indicate(validationMessages);
	}
	
	private void validateForEmpty(List<ValidationMessage> validationMessages) {
		for (PropertyInterface property : form.getProperties()) {
			if (property.getAnnotation(Required.class) != null) {
				EmptyValidator.validate(validationMessages, editedObject, property);
			}
		}
	}

	private void validateForInvalid(List<ValidationMessage> validationMessages) {
		for (PropertyInterface property : form.getProperties()) {
			Object value = property.getValue(editedObject);
			if (InvalidValues.isInvalid(value)) {
				String caption = Resources.getObjectFieldName(Resources.getResourceBundle(), property);
				validationMessages.add(new ValidationMessage(property, caption + " ungültig"));
			}
		}
	}

	
	protected void validate(T object, List<ValidationMessage> validationMessages) {
		// overwrite this method to add Editor specific validation
	}
	
	final void indicate(List<ValidationMessage> validationMessages) {
		for (PropertyInterface property : form.getProperties()) {
			List<String> filteredValidationMessages = ValidationMessage.filterValidationMessage(validationMessages, property);
			form.setValidationMessage(property, filteredValidationMessages);
		}
		
		saveAction().setEnabled(allUsedFieldsValid(validationMessages));
		saveAction.setValidationMessages(validationMessages);
		
		if (indicator != null) {
			indicator.setValidationMessages(validationMessages);
		}
	}
	
	private boolean allUsedFieldsValid(List<ValidationMessage> validationMessages) {
		for (ValidationMessage validationMessage : validationMessages) {
			if (form.getProperties().contains(validationMessage.getProperty())) {
				return false;
			} else {
				if (showWarningIfValidationForUnsuedField()) {
					logger.warning("There is a validation message for " + validationMessage.getProperty().getFieldName() + " but the field is not used in the form");
					logger.warning("The message is: " + validationMessage.getFormattedText());
					logger.fine("This can be ok if at some point not all validations in a object have to be ok");
					logger.fine("But you have to make sure to get valid data in database");
					logger.fine("You can avoid these warnings if you override showWarningIfValidationForUnsuedField()");
				}
			}
		}
		return true;
	}

	protected boolean showWarningIfValidationForUnsuedField() {
		return true;
	}
	
	protected final boolean isSaveable() {
		return saveAction().isEnabled();
	}
	
	public final void checkedClose() {
		if (!userEdited) {
			finish();
		} else if (isSaveable()) {
			ConfirmDialogListener listener = new ConfirmDialogListener() {
				@Override
				public void onClose(int answer) {
					if (answer == JOptionPane.YES_OPTION) {
						// finish will be called at the end of save
						save();
					} else if (answer == JOptionPane.NO_OPTION) {
						finish();
					} else { // Cancel or Close
						// do nothing
					}
				}
			};
			ClientToolkit.getToolkit().showConfirmDialog(form.getComponent(), "Sollen die aktuellen Eingaben gespeichert werden?", "Schliessen",
					JOptionPane.YES_NO_CANCEL_OPTION, listener);

		} else {
			ConfirmDialogListener listener = new ConfirmDialogListener() {
				@Override
				public void onClose(int answer) {
					if (answer == JOptionPane.YES_OPTION) {
						finish();
					} else { // No or Close
						// do nothing
					}
				}
			};
			
			ClientToolkit.getToolkit().showConfirmDialog(form.getComponent(), "Die momentanen Eingaben sind nicht gültig\nund können daher nicht gespeichert werden.\n\nSollen sie verworfen werden?",
					"Schliessen", JOptionPane.YES_NO_OPTION, listener);
		}
	}
	
	protected final Action saveAction() {
		if (saveAction == null) {
			saveAction = new SaveAction("OkAction") {
				@Override
				public void actionPerformed(ActionEvent e) {
					save();
				}
			};
		}
		return saveAction;
	}
	
	protected final Action cancelAction() {
		return new ResourceAction("CancelAction") {
			@Override
			public void actionPerformed(ActionEvent e) {
				finish();
			}
		};
	}
	
	protected final Action demoAction() {
		return new ResourceAction("FillWithDemoDataAction") {
			@Override
			public void actionPerformed(ActionEvent e) {
				fillWithDemoData();
			}
		};
	}
	
	//
	
	public interface EditorFinishedListener {
		
		public void progress(int value, int maximum);
		
		public void finished(String followLink);
		
	}
	
	public void fillWithDemoData() {
		if (editedObject instanceof DemoEnabled) {
			((DemoEnabled) editedObject).fillWithDemoData();
			// re-set the object to update the FormFields
			form.setObject(editedObject);
		} else if (form instanceof DemoEnabled) {
			((DemoEnabled) form).fillWithDemoData();
		}
	}
	
}
