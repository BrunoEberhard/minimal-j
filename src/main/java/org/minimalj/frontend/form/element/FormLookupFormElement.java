package org.minimalj.frontend.form.element;

import org.minimalj.frontend.editor.Editor.SimpleEditor;
import org.minimalj.frontend.form.Form;
import org.minimalj.util.CloneHelper;

public abstract class FormLookupFormElement<T> extends AbstractLookupFormElement<T> {

	public FormLookupFormElement(Object key, boolean editable) {
		super(key, editable);
	}

	@Override
	protected void lookup() {
		new ParserFormElementEditor().action();
	}

	@SuppressWarnings("unchecked")
	protected T createObject() {
		return (T) CloneHelper.newInstance(getProperty().getClazz());
	}

	protected abstract Form<T> createForm();

	public class ParserFormElementEditor extends SimpleEditor<T> {

		@Override
		public T createObject() {
			return getValue() != null ? CloneHelper.clone(getValue()) : FormLookupFormElement.this.createObject();
		}

		@Override
		public Form<T> createForm() {
			return FormLookupFormElement.this.createForm();
		}

		@Override
		protected T save(T object) {
			FormLookupFormElement.this.setValueInternal(object);
			return object;
		}
	}

}
