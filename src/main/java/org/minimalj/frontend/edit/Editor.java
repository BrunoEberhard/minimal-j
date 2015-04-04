package org.minimalj.frontend.edit;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.minimalj.application.DevMode;
import org.minimalj.frontend.edit.form.Form;
import org.minimalj.frontend.toolkit.Action;
import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.frontend.toolkit.ClientToolkit.ConfirmDialogType;
import org.minimalj.frontend.toolkit.ClientToolkit.DialogListener;
import org.minimalj.frontend.toolkit.ClientToolkit.IContent;
import org.minimalj.model.validation.ValidationMessage;
import org.minimalj.util.CloneHelper;
import org.minimalj.util.GenericUtils;
import org.minimalj.util.mock.Mocking;
import org.minimalj.util.resources.Resources;

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
	private Form<T> form;
	protected SaveAction saveAction;
	protected CancelAction cancelAction;
	protected FillWithDemoDataAction demoAction;
	private EditorListener editorListener;
	private Indicator indicator;
	private boolean userEdited;
	
	// what to implement

	protected abstract Form<T> createForm();

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

	public Action[] getActions() {
		if (DevMode.isActive()) {
			return new Action[] { demoAction, cancelAction, saveAction };
		} else {
			return new Action[] { cancelAction, saveAction };
		}
	}

	// /////

	protected Editor() {
	}
	
	public void startEditor() {
		if (!isFinished()) {
			throw new IllegalStateException();
		}
		
		initActions();
		
		original = load();
		editedObject = createEditedObject(original);
		
		form = createForm();
		if (form != null) {
			form.setChangeListener(new EditorChangeListener());
			form.setObject(editedObject);
		}

		userEdited = false;
	}
	
	private void initActions() {
		saveAction = new SaveAction();
		cancelAction = new CancelAction();
		demoAction = new FillWithDemoDataAction();
	}
	
	public IContent getContent() {
		return form.getContent();
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
		Class<T> clazz = (Class<T>) org.minimalj.util.GenericUtils.getGenericClass(Editor.this.getClass());
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
		editedObject = null;
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
		return editedObject == null;
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
			try {
				Object saveResult = save(editedObject);
				fireSaved(saveResult);
				finish();
			} catch (Exception x) {
				String message = x.getMessage() != null ? x.getMessage() : x.getClass().getSimpleName();
				logger.log(Level.SEVERE, message, x);
				ClientToolkit.getToolkit().showError("Technical problems: " + message);
			}
		} else {
			ClientToolkit.getToolkit().showError("Save is not possible because input is not valid");
		}
	}

	private class EditorChangeListener implements Form.FormChangeListener<T> {

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
			DialogListener listener = new DialogListener() {
				@Override
				public void close(Object answer) {
					if (answer == DialogResult.YES) {
						// finish will be called at the end of save
						save();
					} else if (answer == DialogResult.NO) {
						cancel();
					} // else do nothing (dialog will not close)
				}
			};
			ClientToolkit.getToolkit().showConfirmDialog("Sollen die aktuellen Eingaben gespeichert werden?", "Schliessen",
					ConfirmDialogType.YES_NO_CANCEL, listener);

		} else {
			DialogListener listener = new DialogListener() {
				@Override
				public void close(Object answer) {
					if (answer == DialogResult.YES) {
						cancel();
					} else { // No or Close
						// do nothing
					}
				}
			};
			
			ClientToolkit.getToolkit().showConfirmDialog("Die momentanen Eingaben sind nicht gültig\nund können daher nicht gespeichert werden.\n\nSollen sie verworfen werden?",
					"Schliessen", ConfirmDialogType.YES_NO, listener);
		}
	}
	
	protected final class SaveAction extends Action implements Indicator {
		private String description;
		
		@Override
		public void action() {
			save();
		}
		
		@Override
		public void setValidationMessages(List<ValidationMessage> validationMessages) {
//			String iconKey;
			String description;
			boolean valid = validationMessages == null || validationMessages.isEmpty();
			if (valid) {
//				iconKey = getClass().getSimpleName() + ".icon.Ok";
				description = "Eingaben speichern";
			} else {
//				iconKey = getClass().getSimpleName() + ".icon.Error";
				description = ValidationMessage.formatHtml(validationMessages);
			}
			
//			Icon icon = ResourceHelper.getIcon(Resources.getResourceBundle(), iconKey);
//			putValue(LARGE_ICON_KEY, icon);
			this.description = description;
			fireChange();
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
	
	private class FillWithDemoDataAction extends Action {
		public void action() {
			fillWithDemoData();
		}
	}
	
	//
	
	public interface EditorListener {
		
		public void saved(Object savedResult);
		
		public void canceled();
	}
	
	public void fillWithDemoData() {
		if (editedObject instanceof Mocking) {
			((Mocking) editedObject).mock();
			// re-set the object to update the FormFields
			form.setObject(editedObject);
		} else {
			form.mock();
		}
	}
	
}
