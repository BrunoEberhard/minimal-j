package org.minimalj.frontend.impl.util;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.InputComponentListener;
import org.minimalj.model.CodeItem;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.model.validation.ValidationMessage;
import org.minimalj.repository.query.By;
import org.minimalj.repository.query.Criteria;

public class BooleanColumnFilter implements ColumnFilter {

	private final PropertyInterface property;
	private final List<CodeItem<Boolean>> codeItems;
	
	private Input<CodeItem<Boolean>> component;
	
	private BooleanColumnFilter(PropertyInterface property, List<CodeItem<Boolean>> codeItems) {
		this.property = Objects.requireNonNull(property);
		if (property.getClazz() != Boolean.class) {
			throw new IllegalArgumentException(property.getClazz().getName() + " is not a Boolean");
		}
		this.codeItems = codeItems;
	}
	
	public BooleanColumnFilter(PropertyInterface property, String textTrue, String textFalse) {
		this(property, Arrays.asList(new CodeItem<>(true, textTrue), new CodeItem<>(false, textFalse)));
	}
	
	public BooleanColumnFilter(PropertyInterface property, String textTrue, String textFalse, String textNull) {
		this(property, Arrays.asList(new CodeItem<>(true, textTrue), new CodeItem<>(false, textFalse), new CodeItem<>(null, textNull)));
	}

	@Override
	public IComponent getComponent(InputComponentListener listener) {
		if (component == null) {
			component = Frontend.getInstance().createComboBox(codeItems, listener);
		}
		return component;
	}

	@Override
	public boolean active() {
		return component.getValue() != null;
	}
	
	public ValidationMessage validate() {
		return null;
	}

	@Override
	public boolean test(Object object) {
		if (!active()) {
			return true;
		}
		Object value = property.getValue(object);
		return value == component.getValue().getKey();
	}

	@Override
	public Criteria getCriteria() {
		Object value = component.getValue();
		return By.field(property, value);
	}

}
