package org.minimalj.frontend.edit.fields;

import java.util.List;

import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;
import org.minimalj.frontend.toolkit.TextField;
import org.minimalj.model.EnumUtils;
import org.minimalj.model.properties.PropertyInterface;

// TODO: Typisierung bringt hier so was von nichts
public class EnumFormField<E extends Enum<E>> extends AbstractEditField<E> {
	
	private final TextField textFieldDisabled;

	public EnumFormField(PropertyInterface property) {
		this(property, null);
	}

	public EnumFormField(PropertyInterface property, List<E> allowedValues) {
		super(property, true);
		
		textFieldDisabled = ClientToolkit.getToolkit().createReadOnlyTextField();
	}
	
	@Override
	public IComponent getComponent() {
		return textFieldDisabled;
	}

	@Override
	public E getObject() {
		throw new IllegalStateException();
	}

	@Override
	public void setObject(E value) {
		textFieldDisabled.setValue(EnumUtils.getText(value));
	}
	
}
