package org.minimalj.frontend.form.element;

import org.minimalj.frontend.editor.Editor.SimpleEditor;
import org.minimalj.frontend.form.Form;
import org.minimalj.model.validation.InvalidValues;
import org.minimalj.util.CloneHelper;
import org.minimalj.util.StringUtils;

public abstract class FormatLookupFormElement<T> extends AbstractLookupFormElement<T> {

	public FormatLookupFormElement(Object key, boolean textEditable, boolean editable) {
		super(key, textEditable, editable);
	}

	@Override
	protected T parse(String text) {
		if (!StringUtils.isEmpty(text)) {
			T result = getValue();
			if (result == null) {
				result = createObject();
			}
			InvalidValues.markValid(result);
			parse(result, text);
			return result;
		} else {
			return null;
		}
	}

	protected abstract void parse(T object, String text);

	@Override
	protected void lookup() {
		new ParserFormElementEditor().action();
	}

	@SuppressWarnings("unchecked")
	private T createObject() {
		return (T) CloneHelper.newInstance(getProperty().getClazz());
	}

	protected abstract Form<T> createForm();

	public class ParserFormElementEditor extends SimpleEditor<T> {

		@Override
		public T createObject() {
			return getValue() != null ? CloneHelper.clone(getValue()) : FormatLookupFormElement.this.createObject();
		}

		@Override
		public Form<T> createForm() {
			return FormatLookupFormElement.this.createForm();
		}

		@Override
		protected T save(T object) {
			FormatLookupFormElement.this.setValue(object);
			return object;
		}
	}

}
