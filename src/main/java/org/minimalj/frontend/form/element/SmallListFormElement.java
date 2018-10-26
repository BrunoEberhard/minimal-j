package org.minimalj.frontend.form.element;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.frontend.editor.Editor.NewObjectEditor;
import org.minimalj.frontend.editor.Editor.SimpleEditor;
import org.minimalj.frontend.form.Form;
import org.minimalj.model.properties.VirtualProperty;
import org.minimalj.util.GenericUtils;
import org.minimalj.util.resources.Resources;

@SuppressWarnings({ "rawtypes", "unchecked" })
public abstract class SmallListFormElement<T> extends AbstractLookupFormElement<List<T>> /* implements Enable, */ {
	
	private final boolean textEditable;

	public SmallListFormElement(List<T> key) {
		this(key, false, true);
	}

	public SmallListFormElement(List<T> key, boolean textEditable, boolean editable) {
		super(key, textEditable, editable);
		this.textEditable = textEditable;
	}

	@Override
	protected List<T> parse(String text) {
		return null;
	}

	public void lookup() {
		if (textEditable || getValue() == null || getValue().isEmpty()) {
			new AddListEntryEditor().action();
		} else {
			new SmallListFormElementEditor().action();
		}
	}

	protected abstract Form<T> createForm();

	private class AddListEntryEditor extends NewObjectEditor<T> {

		@Override
		protected Class<?> getEditedClass() {
			return GenericUtils.getGenericClass(SmallListFormElement.this.getClass());
		}
		
		@Override
		protected Form<T> createForm() {
			return SmallListFormElement.this.createForm();
		}

		@Override
		protected T save(T object) {
			List list = SmallListFormElement.this.getValue();
			if (list == null)
				list = new ArrayList<>();
			list.add(object);
			SmallListFormElement.this.setValueInternal(list);
			return object;
		}
	}

	private class SmallListFormElementEditor extends SimpleEditor<List<T>> {

		public SmallListFormElementEditor() {
			super(Resources.getPropertyName(getProperty()));
		}

		@Override
		protected Form<List<T>> createForm() {
			Form<List<T>> form = new Form<>();
			form.lineWithoutCaption(new InnerSmallListFormElement());
			return form;
		}

		@Override
		protected List<T> save(List<T> object) {
			SmallListFormElement.this.setValueInternal(object);
			return object;
		}

		@Override
		protected List<T> createObject() {
			return new ArrayList<>(SmallListFormElement.this.getValue());
		}
	}

	public class InnerSmallListFormElement extends ListFormElement<T> {

		public InnerSmallListFormElement() {
			super(new SmallListFormElementProperty());
		}

		@Override
		protected Form<T> createForm(boolean edit) {
			return SmallListFormElement.this.createForm();
		}

		@Override
		protected Class<T> getEditedClass() {
			return (Class<T>) GenericUtils.getGenericClass(SmallListFormElement.this.getClass());
		}
	}

	private class SmallListFormElementProperty extends VirtualProperty {

		@Override
		public String getName() {
			return "";
		}

		@Override
		public Class<?> getClazz() {
			return List.class;
		}

		@Override
		public Object getValue(Object object) {
			return object;
		}

		@Override
		public void setValue(Object object, Object value) {
			if (object != value) {
				((List) object).clear();
				((List) object).addAll((List) value);
			}
		}
	}

}
