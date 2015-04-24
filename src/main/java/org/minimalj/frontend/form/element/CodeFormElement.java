package org.minimalj.frontend.form.element;

import java.util.List;

import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;
import org.minimalj.frontend.toolkit.ClientToolkit.Input;
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
		comboBox = ClientToolkit.getToolkit().createComboBox(codes, listener());
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
		int index = (int)(Math.random() * (double)codes.size());
		setValue(codes.get(index));
	}
	
}
