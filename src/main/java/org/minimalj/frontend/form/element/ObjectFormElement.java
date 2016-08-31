package org.minimalj.frontend.form.element;

import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.editor.Editor;
import org.minimalj.frontend.form.Form;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.GenericUtils;
import org.minimalj.util.resources.Resources;

public abstract class ObjectFormElement<T> extends AbstractObjectFormElement<T> {

	public ObjectFormElement(PropertyInterface property) {
		this(property, true);
	}

	public ObjectFormElement(PropertyInterface property, boolean editable) {
		super(property, editable);
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
			ObjectFormElement.this.setValue(edited);
			return null;
		}

		@Override
		protected void finished(Void result) {
			handleChange();
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
			ObjectFormElement.this.setValue(null);
		}
	}
}
