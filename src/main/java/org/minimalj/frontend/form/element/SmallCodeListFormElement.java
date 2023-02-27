package org.minimalj.frontend.form.element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.minimalj.frontend.editor.Editor.SimpleEditor;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.form.element.AbstractLookupFormElement.LookupParser;
import org.minimalj.model.Code;
import org.minimalj.model.Rendering;
import org.minimalj.model.properties.Properties;
import org.minimalj.model.properties.Property;
import org.minimalj.model.validation.InvalidValues;
import org.minimalj.util.Codes;
import org.minimalj.util.StringUtils;

public class SmallCodeListFormElement<T extends Code> extends AbstractLookupFormElement<List<T>> implements LookupParser {
	
	private final Map<String, T> values = new HashMap<>();

	public SmallCodeListFormElement(List<T> key) {
		this(key, true);
	}

	public SmallCodeListFormElement(List<T> key, boolean editable) {
		super(key, editable);
		renderValues();
	}

	public SmallCodeListFormElement(Property property, boolean editable) {
		super(property, editable);
		renderValues();
	}

	private void renderValues() {
		for (T code : Codes.get(getClazz())) {
			values.put(render(code).toString(), code);
		}
	}

	@SuppressWarnings("unchecked")
	private Class<T> getClazz() {
		return (Class<T>) getProperty().getGenericClass();
	}

	@Override
	protected String render(List<T> value) {
		StringBuilder s = new StringBuilder();
		if (value != null) {
			for (T element : value) {
				if (s.length() > 0) {
					s.append(", ");
				}
				s.append(render(element));
			}
		}
		return s.toString();
	}

	protected CharSequence render(T element) {
		return Rendering.render(element);
	}

	@Override
	public Object parse(String text) {
		List<T> value = new ArrayList<>();
		if (!StringUtils.isEmpty(text)) {
			String[] split = text.split(",");
			for (String s : split) {
				s = s.trim();
				if (values.containsKey(s)) {
					value.add(values.get(s));
				} else {
					((List) value).add(InvalidValues.createInvalidString(s));
				}
			}
		}
		return value;
	}

	public void lookup() {
		// kein Editieren, nur hinzufügen
		new AddListEntryEditor().run();
	}

	public static class ReferenceHolder<R> {
		public R object;
	}

	protected Form<ReferenceHolder<T>> createForm() {
		Form<ReferenceHolder<T>> form = new Form<>(Form.EDITABLE, 1, Form.DEFAULT_COLUMN_WIDTH * 3 / 2);
		form.setIgnoreCaption(true);
		createForm(form);
		return form;
	}

	protected void createForm(Form<ReferenceHolder<T>> form) {
		form.line(new ComboBoxFormElement<T>(Properties.getProperty(ReferenceHolder.class, "object"), Codes.get(getClazz())));
	}

	private class AddListEntryEditor extends SimpleEditor<ReferenceHolder<T>> {

		@Override
		protected Class<?> getEditedClass() {
			return getClazz();
		}
		
		@Override
		protected ReferenceHolder<T> createObject() {
			return new ReferenceHolder<T>();
		}

		@Override
		protected Form<ReferenceHolder<T>> createForm() {
			return SmallCodeListFormElement.this.createForm();
		}

		@Override
		protected ReferenceHolder<T> save(ReferenceHolder<T> object) {
			List<T> list = SmallCodeListFormElement.this.getValue();
			if (list == null)
				list = new ArrayList<>();
			list.add(object.object);
			SmallCodeListFormElement.this.setValueInternal(list);
			return object;
		}
	}

}
