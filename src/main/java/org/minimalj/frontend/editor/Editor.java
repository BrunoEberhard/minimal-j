package org.minimalj.frontend.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.minimalj.application.DevMode;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.page.IDialog;
import org.minimalj.model.validation.Validation;
import org.minimalj.model.validation.ValidationMessage;
import org.minimalj.util.ChangeListener;
import org.minimalj.util.CloneHelper;
import org.minimalj.util.ExceptionUtils;
import org.minimalj.util.GenericUtils;
import org.minimalj.util.mock.Mocking;
import org.minimalj.util.resources.Resources;

public abstract class Editor<T, RESULT> extends Action {
	private static final Logger logger = Logger.getLogger(Editor.class.getName());

	private T object;
	private Form<T> form;
	private final List<ValidationMessage> validationMessages = new ArrayList<>();
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
		Class<?> editedClass = GenericUtils.getGenericClass(getClass());
		if (editedClass != null) {
			String resourceName = Resources.getResourceName(editedClass);
			return new Object[]{Resources.getString(resourceName)};
		} else {
			return null;
		}
	}

	public String getTitle() {
		return getName();
	}

	@Override
	public void action() {
		object = createObject();
		form = createForm();
		
		saveAction = new SaveAction();
		
		validate();

		form.setChangeListener(new EditorChangeListener());
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
		List<Action> actions = new ArrayList<Action>();
		if (DevMode.isActive()) {
			actions.add(new FillWithDemoDataAction());
		}
		return actions;
	}
	
	protected abstract T createObject();
	
	protected T getObject() {
		return object;
	}
	
	protected abstract Form<T> createForm();
	
	private void validate() {
		validationMessages.clear();
		if (object instanceof Validation) {
			((Validation) object).validate(validationMessages);
		}
		ObjectValidator.validate(object, validationMessages, form.getProperties());
		validate(object, validationMessages);
		form.indicate(validationMessages);
		saveAction.setValidationMessages(validationMessages);
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
			Frontend.showError(x.getLocalizedMessage());
			return;
		}
	}

	protected boolean closeWith(RESULT result) {
		return true;
	}
	
	protected abstract RESULT save(T object);
	
	protected void finished(RESULT result) {
		//
	}

	private class EditorChangeListener implements ChangeListener<Form<?>> {

		@Override
		public void changed(Form<?> form) {
			validate();
		}
	}	

	protected final class SaveAction extends Action {
		private String description;
		private boolean valid = false;
		
		@Override
		public void action() {
			save();
		}
		
		public void setValidationMessages(List<ValidationMessage> validationMessages) {
			valid = validationMessages == null || validationMessages.isEmpty();
			description = ValidationMessage.formatHtml(validationMessages);
			fireChange();
		}

		@Override
		public boolean isEnabled() {
			return valid;
		}
		
		@Override
		public String getDescription() {
			return description != null ? description : super.getDescription();
		}
	}
	
	private class CancelAction extends Action {
		@Override
		public void action() {
			cancel();
		}
	}
	
	public void cancel() {
		dialog.closeDialog();
	}

	private class FillWithDemoDataAction extends Action {
		@Override
		public void action() {
			fillWithDemoData();
			validate();
		}
	}
	
	protected void fillWithDemoData() {
		if (object instanceof Mocking) {
			((Mocking) object).mock();
			form.setObject(object);
		} else {
			form.mock();
		}
	}

	public static abstract class SimpleEditor<T> extends Editor<T, T> {

		public SimpleEditor() {
		}

		public SimpleEditor(String actionName) {
			super(actionName);
		}
		
		@Override
		protected T save(T changedObject) {
			return changedObject;
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
			Class<T> clazz = (Class<T>) GenericUtils.getGenericClass(NewObjectEditor.this.getClass());
			T newInstance = CloneHelper.newInstance(clazz);
			return newInstance;
		}
	}

}
