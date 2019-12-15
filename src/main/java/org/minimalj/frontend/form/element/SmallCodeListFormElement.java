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
import org.minimalj.model.properties.PropertyInterface;
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

	public SmallCodeListFormElement(PropertyInterface property, boolean editable) {
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

	private CharSequence render(T element) {
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
		new AddListEntryEditor().action();
	}

	public class ReferenceHolder {
		public T object;
	}

	protected Form<ReferenceHolder> createForm() {
		Form<ReferenceHolder> form = new Form<>();
		createForm(form);
		return form;
	}

	protected void createForm(Form<ReferenceHolder> form) {
		form.line(new ComboBoxFormElement<T>(Properties.getProperty(ReferenceHolder.class, "object"), Codes.get(getClazz())));
	}

	private class AddListEntryEditor extends SimpleEditor<ReferenceHolder> {

		@Override
		protected Class<?> getEditedClass() {
			return getClazz();
		}
		
		@Override
		protected SmallCodeListFormElement<T>.ReferenceHolder createObject() {
			return new ReferenceHolder();
		}

		@Override
		protected Form<ReferenceHolder> createForm() {
			return SmallCodeListFormElement.this.createForm();
		}

		@Override
		protected ReferenceHolder save(ReferenceHolder object) {
			List<T> list = SmallCodeListFormElement.this.getValue();
			if (list == null)
				list = new ArrayList<>();
			list.add(object.object);
			SmallCodeListFormElement.this.setValueInternal(list);
			return object;
		}
	}

}
