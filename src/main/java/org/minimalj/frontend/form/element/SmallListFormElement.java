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
public abstract class SmallListFormElement<T> extends AbstractLookupFormElement<List<T>> {
	
	public SmallListFormElement(List<T> key) {
		this(key, true);
	}

	public SmallListFormElement(List<T> key, boolean editable) {
		super(key, editable);
	}

	@Override
	protected abstract String render(List<T> value);

	@Override
	public void lookup() {
		if (this instanceof LookupParser || getValue() == null || getValue().isEmpty()) {
			new AddListEntryEditor().run();
		} else {
			new SmallListFormElementEditor().run();
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
			List<T> list = SmallListFormElement.this.getValue();
			if (list == null)
				list = new ArrayList<>();
			list.add(object);
			SmallListFormElement.this.setValueInternal(list);
			return object;
		}
	}
	
	protected int getColumnWidth() {
		return Form.DEFAULT_COLUMN_WIDTH;
	}

	private class SmallListFormElementEditor extends SimpleEditor<List<T>> {

		public SmallListFormElementEditor() {
			super(Resources.getPropertyName(getProperty()));
		}

		@Override
		protected Form<List<T>> createForm() {
			Form<List<T>> form = new Form<>(Form.EDITABLE, 1, getColumnWidth());
			form.line(new InnerSmallListFormElement());
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
		public String getCaption() {
			return null;
		}

		@Override
		protected Form<T> createForm(boolean newObject) {
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
