package org.minimalj.frontend.form.element;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.editor.Editor.NewObjectEditor;
import org.minimalj.frontend.editor.Editor.SimpleEditor;
import org.minimalj.frontend.form.Form;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.model.properties.VirtualProperty;
import org.minimalj.util.GenericUtils;
import org.minimalj.util.resources.Resources;

@SuppressWarnings({ "rawtypes", "unchecked" })
public abstract class SmallListFormElement<T> extends AbstractFormElement<List<T>> /* implements Enable, */ {
	private final boolean editable;
	private final Input<List<T>> lookup;
	
	public SmallListFormElement(PropertyInterface property) {
		this(property, true);
	}

	public SmallListFormElement(PropertyInterface property, boolean editable) {
		super(property);
		this.editable = editable;
		this.lookup = Frontend.getInstance().createLookup(this::show, listener());
	}

	public void show() {
		if (getValue() == null || getValue().isEmpty()) {
			new AddListEntryEditor().action();
		} else {
			new SmallListFormElementEditor().action();
		}
	}

	public IComponent getComponent() {
		return lookup;
	};

	@Override
	public List<T> getValue() {
		List<T> list = lookup.getValue();
		if (list == null) {
			list = new ArrayList<>();
		}
		return list;
	}

	@Override
	public void setValue(List<T> list) {
		lookup.setValue(list);
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
			list.add(object);
			SmallListFormElement.this.setValue(list);
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
			SmallListFormElement.this.setValue(object);
			return object;
		}

		@Override
		protected List<T> createObject() {
			return SmallListFormElement.this.getValue();
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
