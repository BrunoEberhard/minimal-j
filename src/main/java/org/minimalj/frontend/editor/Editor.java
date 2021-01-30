package org.minimalj.frontend.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.minimalj.application.Configuration;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.page.IDialog;
import org.minimalj.model.validation.Validation;
import org.minimalj.model.validation.ValidationMessage;
import org.minimalj.util.CloneHelper;
import org.minimalj.util.ExceptionUtils;
import org.minimalj.util.GenericUtils;
import org.minimalj.util.mock.Mocking;
import org.minimalj.util.resources.Resources;

/**
 * 
 * @param <T> The class of the edited object
 * @param <RESULT> The class of the object returned by the save method. This can be the same as the one 
 * of the edited object. Then you could use the {@link SimpleEditor}. In more complex situations the
 * backend called in the save method will do some business logic resulting in a different output object.
 */
public abstract class Editor<T, RESULT> extends Action {
	private static final Logger logger = Logger.getLogger(Editor.class.getName());

	private T object;
	private Form<T> form;
	private SaveAction saveAction;
	private IDialog dialog;
	
	public Editor() {
		super();
	}

	public Editor(String actionName) {
		super(actionName);
	}
	
	@Override
	protected Object[] getNameArguments() {
		Class<?> editedClass = getEditedClass();
		if (editedClass != null) {
			String resourceName = Resources.getResourceName(editedClass);
			return new Object[]{Resources.getString(resourceName)};
		}
		return null;
	}

	protected Class<?> getEditedClass() {
		return GenericUtils.getGenericClass(getClass());
	}

	public String getTitle() {
		return getName();
	}

	@Override
	public void run() {
		object = createObject();
		form = createForm();
		
		saveAction = new SaveAction();
		
		validate(form);

		form.setChangeListener(this::validate);
		form.setObject(object);
		
		dialog = Frontend.showDialog(getTitle(), form.getContent(), saveAction, new CancelAction(), createActions());
	}
	
	private Action[] createActions() {
		List<Action> additionalActions = createAdditionalActions();
		Action[] actions = new Action[additionalActions.size() + 2];
		int index;
		for (index = 0; index<additionalActions.size(); index++) {
			actions[index] = additionalActions.get(index);
		}
		actions[index++] = new CancelAction();
		actions[index++] = saveAction;
		return actions;
	}
 	
	protected List<Action> createAdditionalActions() {
		List<Action> actions = new ArrayList<>();
		if (Configuration.isDevModeActive() && (object instanceof Mocking || this instanceof Mocking)) {
			actions.add(new FillWithDemoDataAction());
		}
		return actions;
	}
	
	protected abstract T createObject();
	
	protected T getObject() {
		return object;
	}
	
	protected abstract Form<T> createForm();
	
	private void validate(Form<?> form) {
		List<ValidationMessage> validationMessages = new ArrayList<>();
		if (object instanceof Validation) {
			validationMessages.addAll(((Validation) object).validateNullSafe());
		}
		validationMessages.addAll(Validator.validate(object));
		validate(object, validationMessages);
		boolean relevantValidationMessage = form.indicate(validationMessages);
		saveAction.setEnabled(!relevantValidationMessage);
	}
	
	protected void validate(T object, List<ValidationMessage> validationMessages) {
		// 
	}
	
	private void save() {
		try {
			RESULT result = save(object);
			if (closeWith(result)) {
				dialog.closeDialog();
				finished(result);
			}
		} catch (Exception x) {
			ExceptionUtils.logReducedStackTrace(logger, x);
			// TODO clever error handling, for example at login if jdbc is wrong
			Frontend.showError(x.getLocalizedMessage() != null ? x.getLocalizedMessage() : x.getClass().getSimpleName());
        }
	}

	protected boolean closeWith(RESULT result) {
		return true;
	}
	
	protected abstract RESULT save(T object);
	
	protected void finished(RESULT result) {
		//
	}

	protected final class SaveAction extends Action {
		@Override
		public void run() {
			save();
		}
	}
	
	private class CancelAction extends Action {
		@Override
		public void run() {
			cancel();
		}
	}
	
	public void cancel() {
		dialog.closeDialog();
	}

	protected void objectChanged() {
		form.setObject(object);
		validate(form);
	}

	private class FillWithDemoDataAction extends Action {
		@Override
		public void run() {
			mock();
		}
	}
	
	protected void mock() {
		if (object instanceof Mocking) {
			((Mocking) object).mock();
			objectChanged();
		} else if (this instanceof Mocking) {
			form.mock();
			validate(form);
		}
	}

	public static abstract class SimpleEditor<T> extends Editor<T, T> {

		public SimpleEditor() {
		}

		public SimpleEditor(String actionName) {
			super(actionName);
		}	
	}

	public static abstract class NewObjectEditor<T> extends SimpleEditor<T> {

		public NewObjectEditor() {
		}

		public NewObjectEditor(String actionName) {
			super(actionName);
		}

		@Override
		protected T createObject() {
			@SuppressWarnings("unchecked")
			Class<T> clazz = (Class<T>) getEditedClass();
			return CloneHelper.newInstance(clazz);
		}
	}

}
