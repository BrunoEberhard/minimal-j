package org.minimalj.frontend.form.element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.Input;
import org.minimalj.model.CodeItem;
import org.minimalj.model.EnumUtils;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.NotEmpty;
import org.minimalj.model.properties.Property;
import org.minimalj.util.mock.Mocking;
import org.minimalj.util.resources.Resources;

public class RadioButtonsFormElement<T> extends AbstractFormElement<T> implements Enable, Mocking {

	private final List<CodeItem<T>> values;
	private final Input<CodeItem<T>> radioButtons;

	public RadioButtonsFormElement(Boolean key, String resourceName) {
		this(Keys.getProperty(key), createCodeItems(key, resourceName));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public RadioButtonsFormElement(Enum<?> key) {
		this(Keys.getProperty(key), (List) EnumUtils.itemList(key.getClass()));
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public <E extends Enum<E>> RadioButtonsFormElement(E key, E... values) {
		this(Keys.getProperty(key), (List) EnumUtils.itemList(Arrays.asList(values)));
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	static <T> List<CodeItem<T>> createCodeItems(Boolean key, String resourceNames) {
		Property property = Keys.getProperty(key);
		List<CodeItem<T>> codeItems = new ArrayList<>();
		if (property.getAnnotation(NotEmpty.class) == null) {
			codeItems.add(new CodeItem(null, Resources.getString(resourceNames + ".null")));
		}
		codeItems.add(new CodeItem(Boolean.FALSE, Resources.getString(resourceNames + ".false")));
		codeItems.add(new CodeItem(Boolean.TRUE, Resources.getString(resourceNames + ".true")));
		return codeItems;
	}
	
	private RadioButtonsFormElement(Property property, List<CodeItem<T>> values) {
		super(property);
		this.values = values;
		radioButtons = Frontend.getInstance().createRadioButtons(values, listener());
	}
	
	@Override
	public IComponent getComponent() {
		return radioButtons;
	}

	@Override
	public void setEnabled(boolean enabled) {
		radioButtons.setEditable(enabled);
	}

	@Override
	public T getValue() {
		CodeItem<T> codeItem = radioButtons.getValue();
		return codeItem != null ? codeItem.getKey() : null;
	}

	@Override
	public void setValue(T value) {
		CodeItem<T> item = null;
		if (value != null) {
			for (CodeItem<T> i : values) {
				if (i.getKey().equals(value)) {
					item = i;
					break;
				}
			}
		}
		
		radioButtons.setValue(item);
	}

	@Override
	public void mock() {
		int index = (int)(Math.random() * values.size());
		setValue(values.get(index).getKey());
	}
	
}