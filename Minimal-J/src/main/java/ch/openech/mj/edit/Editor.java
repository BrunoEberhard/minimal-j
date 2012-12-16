package ch.openech.mj.edit;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ch.openech.mj.autofill.DemoEnabled;
import ch.openech.mj.db.model.EmptyValidator;
import ch.openech.mj.db.model.PropertyInterface;
import ch.openech.mj.edit.form.Form.FormChangeEvent;
import ch.openech.mj.edit.form.IForm;
import ch.openech.mj.edit.validation.Indicator;
import ch.openech.mj.edit.validation.Validatable;
import ch.openech.mj.edit.validation.Validation;
import ch.openech.mj.edit.validation.ValidationMessage;
import ch.openech.mj.edit.value.CloneHelper;
import ch.openech.mj.edit.value.Required;
import ch.openech.mj.resources.ResourceAction;
import ch.openech.mj.resources.Resources;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.ConfirmDialogListener;

import com.google.gwt.dev.util.collect.HashMap;

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

	private T original, editedObject;
	private IForm<T> form;
	private SaveAction saveAction;
	private EditorFinishedListener editorFinishedListener;
	private final Map<PropertyInterface, String> propertyValidations = new HashMap<>();
	private final Map<PropertyInterface, String> emptyValidations = new HashMap<>();
	private Indicator indicator;
	private boolean saveable = true;
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
	
//	/**
//	 * Override this method for a validation specific for this editor.
//	 * (Implement Validatable on the object itself for a general validation
//	 * on the object). You can call super on your own validate method but you
//	 * dont have to. If only a part of the fields of the object should be
//	 * validated normally you wont call super.
//	 * 
//	 * @param object
//	 * @param resultList
//	 */
//	protected void validate(T object) {
//		if (object instanceof Validation) {
//			List<String> resultList = ((Validation) object).validate();
//		}
//	}

	protected abstract boolean save(T object) throws Exception;

	protected boolean isSaveSynchron() {
		return true;
	}
	
	public String getTitle() {
		String resourceName = getClass().getSimpleName() + ".text";
		return Resources.getString(resourceName);
	}

	public Action[] getActions() {
		return new Action[] { cancelAction(), saveAction() };
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
		
		form.setSaveAction(saveAction());
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
		if (saveable) {
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
			ClientToolkit.getToolkit().showNotification(form.getComponent(), "Abschluss fehlgeschlagen\n\n" + x.getLocalizedMessage());
		}
	}
	
	public final void progress(int value, int maximum) {
		if (editorFinishedListener != null) {
			editorFinishedListener.progress(value, maximum);
		}
	}
	
	private class EditorChangeListener implements ChangeListener {

		public EditorChangeListener() {
			updateValidation();
		}
		
		@Override
		public void stateChanged(ChangeEvent e) {
			FormChangeEvent event = (FormChangeEvent) e;
			
			Object newPropertyValue = event.getValue();
			PropertyInterface property = event.getProperty();
			
			property.setValue(editedObject, newPropertyValue);
			
			propertyValidations.remove(property);
			if (newPropertyValue instanceof Validatable) {
				String validationMessage = ((Validatable) newPropertyValue).validate();
				if (validationMessage != null) {
					propertyValidations.put(property, validationMessage);
				}
			}
			updateValidation();
			
			userEdited = true;
		}
	}

	private void updateValidation() {
		List<ValidationMessage> validationMessages = new ArrayList<>();
		if (editedObject instanceof Validation) {
			((Validation) editedObject).validate(validationMessages);
		}
		validateForEmpty(validationMessages);
		indicate(validationMessages);
	}
	
	private void validateForEmpty(List<ValidationMessage> validationMessages) {
		for (PropertyInterface property : form.getProperties()) {
			if (property.getAnnotation(Required.class) != null) {
				EmptyValidator.validate(validationMessages, editedObject, property);
			}
		}
	}
	
	protected void validate(T object, List<ValidationMessage> resultList) {
		// overwrite this method to add Editor specific validation
	}
	
	final void indicate(List<ValidationMessage> validationMessages) {
		for (PropertyInterface property : form.getProperties()) {
			List<String> filteredValidationMessages = ValidationMessage.filterValidationMessage(validationMessages, property);
			if (filteredValidationMessages.contains(property)) {
				filteredValidationMessages.add(propertyValidations.get(property));
			}
			form.setValidationMessage(property, filteredValidationMessages);
		}
		
		saveable = validationMessages.isEmpty() && propertyValidations.isEmpty();
	}
	
	protected final boolean isSaveable() {
		return saveable;
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
		if (form instanceof DemoEnabled) {
			((DemoEnabled) form).fillWithDemoData();
		}
	}
	
}
