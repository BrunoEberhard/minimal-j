package ch.openech.mj.edit;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ch.openech.mj.edit.form.FormVisual;
import ch.openech.mj.edit.validation.Indicator;
import ch.openech.mj.edit.validation.Validatable;
import ch.openech.mj.edit.validation.ValidationMessage;
import ch.openech.mj.resources.ResourceHelper;
import ch.openech.mj.resources.Resources;
import ch.openech.mj.toolkit.ClientToolkit;

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
 * 
 * @author Bruno
 * 
 * @param <T>
 *            Class of the edited Object
 */
public abstract class Editor<T> {

	protected final Action saveAction;
	protected final Action cancelAction;
	private FormVisual<T> form;
	private EditorFinishedListener editorFinishedListener;
	private boolean saveable = true;
	
	// what to implement

	protected abstract FormVisual<T> createForm();

	protected abstract T load();

	protected abstract void validate(T object, List<ValidationMessage> resultList);

	protected abstract boolean save(T object);

	public String getTitle() {
		String resourceName = getClass().getSimpleName() + ".text";
		return Resources.getString(resourceName);
	}
	
	public Action[] getActions() {
		return new Action[]{cancelAction, saveAction};
	}

	/**
	 * An additional information that is displayed above the form
	 * 
	 * @return null (nothing) if not overwritten
	 */
	public String getInformation() {
		return null;
	}
	
	// /////

	protected Editor() {
		saveAction = createSaveAction();
		cancelAction = createCancelAction();
	}
	
	public FormVisual<T> startEditor() {
		if (form != null) {
			throw new IllegalStateException();
		}
		form = createForm();
		T object = load();
		form.setObject(object);
		
		form.setSaveAction(saveAction);
		form.setChangeListener(new EditorChangeListener());
		
		return form;
	}
	
	public void setEditorFinishedListener(EditorFinishedListener editorFinishedListener) {
		this.editorFinishedListener = editorFinishedListener;
	}
	
	private void finish() {
		form = null;
		fireEditorFinished();
	}
	
	private void fireEditorFinished() {
		if (editorFinishedListener != null) {
			editorFinishedListener.finished();
		}
	}
	
	protected T getObject() {
		return form.getObject();
	}
	
	protected void save() {
		if (true || saveable) {
			if (save(getObject())) {
				finish();
			}
		} else {
			ClientToolkit.getToolkit().showNotification(form, "Abschluss nicht möglich.\n\nBitte Eingaben überprüfen.");
		}
	}

	private class EditorChangeListener implements ChangeListener {

		public EditorChangeListener() {
			update(getObject());
		}
		
		@Override
		public void stateChanged(ChangeEvent e) {
			update(getObject());
		}
	}

	private void update(T object) {
		List<ValidationMessage> validationResult = validate(object);
		indicate(validationResult);
	}
	
	protected List<ValidationMessage> validate(T object) {
		List<ValidationMessage> validationResult = new ArrayList<ValidationMessage>();
		if (object instanceof Validatable) {
			Validatable validatable = (Validatable) object;
			validatable.validate(validationResult);
		}
		form.validate(validationResult);
		validate(object, validationResult);
		return validationResult;
	}
	
	protected void indicate(List<ValidationMessage> validationResult) {
		saveable = validationResult.isEmpty();
		form.setValidationMessages(validationResult);
		if (saveAction instanceof Indicator) {
			((Indicator) saveAction).setValidationMessages(validationResult);
		}
	}
	
	protected boolean isSaveable() {
		return saveable;
	}
	
	public void checkedClose() {
		List<ValidationMessage> validationResult = validate(getObject());
		boolean valid = validationResult.isEmpty();
		if (valid) {
			int answer = ClientToolkit.getToolkit().showConfirmDialog(form, "Sollen die aktuellen Eingaben gespeichert werden?", "Schliessen",
					JOptionPane.YES_NO_CANCEL_OPTION);

			if (answer == JOptionPane.YES_OPTION) {
				if (save(getObject())) {
					finish();
				}
			} else if (answer == JOptionPane.CANCEL_OPTION) {
				// do nothing
			} else {
				finish();
			}
		} else {
			int answer = ClientToolkit.getToolkit().showConfirmDialog(form, "Die momentanen Eingaben sind nicht gültig\nund können daher nicht gespeichert werden.\n\nSollen sie verworfen werden?",
					"Schliessen", JOptionPane.YES_NO_OPTION);
			
			if (answer == JOptionPane.NO_OPTION) {
				// do nothing
			} else {
				finish();
			}
		}
	}

	protected Action createSaveAction() {
		return new SaveAction("OkAction") {
			@Override
			public void actionPerformed(ActionEvent e) {
				save();
			}
		};
	}
	
	protected Action createCancelAction() {
		Action action = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				finish();
			}
		};
		ResourceHelper.initProperties(action, Resources.getResourceBundle(), "CancelAction");
		return action;
	}

	//
	
	public interface EditorFinishedListener {
		
		public void finished();
		
	}
	
	
}
