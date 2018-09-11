package org.minimalj.frontend.form.element;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.editor.Editor;
import org.minimalj.frontend.form.Form;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.repository.sql.EmptyObjects;
import org.minimalj.util.CloneHelper;
import org.minimalj.util.GenericUtils;
import org.minimalj.util.IdUtils;
import org.minimalj.util.resources.Resources;

public abstract class ObjectFormElement<T> extends AbstractObjectFormElement<T> {

	public ObjectFormElement(PropertyInterface property) {
		this(property, true);
	}

	public ObjectFormElement(PropertyInterface property, boolean editable) {
		super(property, editable);
	}
	
	@Override
	protected void show(T object) {
		if (isEditable()) {
			if (!EmptyObjects.isEmpty(object)) {
				add(object, getEditorAction(), new RemoveObjectAction());
			} else {
				add(object, getEditorAction());
			}
		} else {
			add(object);
		}
	}

	protected abstract Form<T> createForm();

	public class ObjectFormElementEditor extends Editor<T, Void> {
		public ObjectFormElementEditor() {
			assertEditable(this);
		}

		public ObjectFormElementEditor(String name) {
			super(name);
			assertEditable(this);
		}
		
		@Override
		protected Class<?> getEditedClass() {
			return GenericUtils.getGenericClass(ObjectFormElement.this.getClass());
		}
		
		protected boolean persist() {
			return IdUtils.hasId(getEditedClass());
		}

		@Override
		public Form<T> createForm() {
			return ObjectFormElement.this.createForm();
		}

		@Override
		public T createObject() {
			return ObjectFormElement.this.createObject();
		}

		@Override
		public Void save(T edited) {
			if (persist()) {
				edited = Backend.save(edited);
			}
			ObjectFormElement.this.setValue(edited);
			return null;
		}

		@Override
		protected void finished(Void result) {
			handleChange();
		}
		
		@Override
		protected List<Action> createAdditionalActions() {
			List<Action> actions = super.createAdditionalActions();
			if (!getProperty().isFinal()) {
				actions.add(new DeleteObjectAction());
			}
			return actions;
		}

		private class DeleteObjectAction extends Action {

			@Override
			public void action() {
				ObjectFormElement.this.setValue(null);
				cancel();
			}
		}
	}
	
	public class NewObjectFormElementEditor extends ObjectFormElementEditor {
		public NewObjectFormElementEditor() {
			assertEditable(this);
		}

		public NewObjectFormElementEditor(String name) {
			super(name);
			assertEditable(this);
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public T createObject() {
			return (T) CloneHelper.newInstance(GenericUtils.getGenericClass(ObjectFormElement.this.getClass()));
		}
	}

	protected Action getEditorAction() {
		return new ObjectFormElementEditor();
	}
	
	public class RemoveObjectAction extends Action {

		public RemoveObjectAction() {
			assertEditable(this);
		}

		@Override
		protected Object[] getNameArguments() {
			Class<?> editedClass = GenericUtils.getGenericClass(ObjectFormElement.this.getClass());
			if (editedClass != null) {
				String resourceName = Resources.getResourceName(editedClass);
				return new Object[] { Resources.getString(resourceName) };
			} else {
				return null;
			}
		}
		
		@Override
		public void action() {
			Class<T> editedClass = (Class<T>) GenericUtils.getGenericClass(ObjectFormElement.this.getClass());
			if (!getProperty().isFinal()) {
				ObjectFormElement.this.setValue(null);
			} else {
				ObjectFormElement.this.setValue(EmptyObjects.getEmptyObject(editedClass));
			}
		}
	}
}
