package org.minimalj.frontend.form.element;

import java.util.List;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.Input;
import org.minimalj.model.Code;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.Codes;
import org.minimalj.util.mock.Mocking;

public class CodeFormElement extends AbstractFormElement<Code> implements Enable, Mocking {

	private final List<Code> codes;
	private final Input<Code> comboBox;

	public CodeFormElement(PropertyInterface property) {
		super(property);
		codes = Codes.get((Class<Code>) property.getClazz());
		comboBox = Frontend.getInstance().createComboBox(codes, listener());
	}
	
	@Override
	public IComponent getComponent() {
		return comboBox;
	}

	@Override
	public void setEnabled(boolean enabled) {
		comboBox.setEditable(enabled);
	}

	@Override
	public Code getValue() {
		return comboBox.getValue();
	}

	@Override
	public void setValue(Code value) {
		comboBox.setValue(value);
	}

	@Override
	public void mock() {
		int index = (int)(Math.random() * codes.size());
		setValue(codes.get(index));
	}
	
}
