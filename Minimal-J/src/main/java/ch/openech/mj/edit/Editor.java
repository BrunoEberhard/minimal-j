package ch.openech.mj.edit;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.JOptionPane;

import ch.openech.mj.application.DevMode;
import ch.openech.mj.autofill.DemoEnabled;
import ch.openech.mj.edit.form.IForm;
import ch.openech.mj.edit.validation.Indicator;
import ch.openech.mj.edit.validation.ValidationMessage;
import ch.openech.mj.edit.value.CloneHelper;
import ch.openech.mj.resources.ResourceHelper;
import ch.openech.mj.resources.Resources;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.ConfirmDialogListener;
import ch.openech.mj.toolkit.IAction;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.ResourceAction;
import ch.openech.mj.toolkit.ResourceActionEnabled;

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
	protected static final Object SAVE_SUCCESSFUL = new Object();
	protected static final Object SAVE_FAILED = null;
	
	private T original, editedObject;
	private IForm<T> form;
	protected final SaveAction saveAction = new SaveAction();
	protected final CancelAction cancelAction = new CancelAction();
	protected final FillWithDemoDataAction demoAction = new FillWithDemoDataAction();
	private EditorListener editorListener;
	private Indicator indicator;
	private boolean userEdited;
	
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

	protected abstract Object save(T object) throws Exception;

	public String getTitle() {
		String resourceName = getClass().getSimpleName() + ".text";
		return Resources.getString(resourceName);
	}

	public IAction[] getActions() {
		if (DevMode.isActive()) {
			return new IAction[] { demoAction, cancelAction, saveAction };
		} else {
			return new IAction[] { cancelAction, saveAction };
		}
	}

	// /////

	protected Editor() {
	}
	
	public IForm<T> startEditor() {
		if (form != null) {
			throw new IllegalStateException();
		}
		
		original = load();
		editedObject = createEditedObject(original);

		form = createForm();
		form.setChangeListener(new EditorChangeListener());
		form.setObject(editedObject);

		userEdited = false;
		
		return form;
	}
	
	private T createEditedObject(T original) {
		if (original != null) {
			return CloneHelper.clone(original);
		} else {
			return newInstance();
		}
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
	
	public final void setEditorListener(EditorListener savedListener) {
		this.editorListener = savedListener;
	}
	
	public final void setIndicator(Indicator indicator) {
		this.indicator = indicator;
	}

	protected void finish() {
		form = null;
	}
	
	protected void cancel() {
		fireCanceled();
		finish();
	}

	private void fireCanceled() {
		try {
			editorListener.canceled();
		} catch (Exception x) {
			logger.log(Level.SEVERE, x.getLocalizedMessage(), x);
		}
	}

	public final boolean isFinished() {
		return form == null;
	}
	
	private void fireSaved(Object saveResult) {
		try {
			editorListener.saved(saveResult);
		} catch (Exception x) {
			logger.log(Level.SEVERE, x.getLocalizedMessage(), x);
		}
	}

	protected final T getObject() {
		return editedObject;
	}
	
	protected void save() {
		if (isSaveable()) {
			doSave();
		} else {
			ClientToolkit.getToolkit().showError(form.getComponent(), "Abschluss nicht möglich.\n\nBitte Eingaben überprüfen.");
		}
	}

	private void doSave() {
		try {
			Object saveResult = save(editedObject);
			if (saveResult != null) {
				fireSaved(saveResult);
				finish();
			}
		} catch (Exception x) {
			logger.log(Level.SEVERE, x.getLocalizedMessage(), x);
			ClientToolkit.getToolkit().showError(form.getComponent(), "Abschluss fehlgeschlagen: " + x.getLocalizedMessage());
		}
	}

	private class EditorChangeListener implements IForm.FormChangeListener<T> {

		public void changed() {
			userEdited = true;
		}

		@Override
		public void commit() {
			if (isSaveable()) {
				save();
			}
		}

		@Override
		public void validate(T object, List<ValidationMessage> validationResult) {
			Editor.this.validate(object, validationResult);
		}

		@Override
		public void indicate(List<ValidationMessage> validationMessages, boolean allUsedFieldsValid) {
			saveAction.setEnabled(allUsedFieldsValid);
			saveAction.setValidationMessages(validationMessages);
			
			if (indicator != null) {
				indicator.setValidationMessages(validationMessages);
			}
		}
	}		

	protected void validate(T object, List<ValidationMessage> validationMessages) {
		// overwrite this method to add Editor specific validation
	}
	
	protected final boolean isSaveable() {
		return saveAction.isEnabled();
	}
	
	public void checkedClose() {
		if (!userEdited) {
			cancel();
		} else if (isSaveable()) {
			ConfirmDialogListener listener = new ConfirmDialogListener() {
				@Override
				public void onClose(int answer) {
					if (answer == JOptionPane.YES_OPTION) {
						// finish will be called at the end of save
						save();
					} else { // Cancel or Close
						cancel();
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
						cancel();
					} else { // No or Close
						// do nothing
					}
				}
			};
			
			ClientToolkit.getToolkit().showConfirmDialog(form.getComponent(), "Die momentanen Eingaben sind nicht gültig\nund können daher nicht gespeichert werden.\n\nSollen sie verworfen werden?",
					"Schliessen", JOptionPane.YES_NO_OPTION, listener);
		}
	}
	
	protected final class SaveAction extends ResourceActionEnabled implements Indicator {
		private String description;
		
		@Override
		public void action(IComponent context) {
			save();
		}
		
		@Override
		public void setValidationMessages(List<ValidationMessage> validationMessages) {
			String iconKey;
			String description;
			boolean valid = validationMessages == null || validationMessages.isEmpty();
			if (valid) {
				iconKey = getClass().getSimpleName() + ".icon.Ok";
				description = "Eingaben speichern";
			} else {
				iconKey = getClass().getSimpleName() + ".icon.Error";
				description = ValidationMessage.formatHtml(validationMessages);
			}
			
			Icon icon = ResourceHelper.getIcon(Resources.getResourceBundle(), iconKey);
//			putValue(LARGE_ICON_KEY, icon);
			this.description = description;
			fireChange();
		}

		@Override
		public String getDescription() {
			return description;
		}
	}
	
	private class CancelAction extends ResourceAction {
		@Override
		public void action(IComponent context) {
			cancel();
		}
	}
	
	private class FillWithDemoDataAction extends ResourceAction {
		public void action(IComponent context) {
			fillWithDemoData();
		}
	}
	
	//
	
	public interface EditorListener {
		
		public void saved(Object savedResult);
		
		public void canceled();
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
