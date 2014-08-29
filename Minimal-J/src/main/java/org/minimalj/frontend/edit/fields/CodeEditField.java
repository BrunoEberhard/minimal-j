package org.minimalj.frontend.edit.fields;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;
import org.minimalj.frontend.toolkit.ComboBox;
import org.minimalj.frontend.toolkit.SwitchComponent;
import org.minimalj.frontend.toolkit.TextField;
import org.minimalj.model.PropertyInterface;
import org.minimalj.transaction.criteria.Criteria;
import org.minimalj.util.CodeUtils;
import org.minimalj.util.DemoEnabled;

// TODO: Typisierung bringt hier so was von nichts
public class CodeEditField extends AbstractEditField<Object> implements Enable, DemoEnabled {
	private final List codes;
	
	private final SwitchComponent switchComponent;
	private final ComboBox comboBox;
	private final TextField textFieldDisabled;

	public CodeEditField(PropertyInterface property, String prefix) {
		super(property, true);
		codes = Backend.getInstance().read(property.getFieldClazz(), Criteria.all(), 1000);
		
		comboBox = ClientToolkit.getToolkit().createComboBox(listener());
		comboBox.setObjects(codes);
		
		textFieldDisabled = ClientToolkit.getToolkit().createReadOnlyTextField();
		textFieldDisabled.setText("-");
		
		switchComponent = ClientToolkit.getToolkit().createSwitchComponent(comboBox, textFieldDisabled);
		switchComponent.show(comboBox);
	}
	
	@Override
	public IComponent getComponent() {
		return switchComponent;
	}

	@Override
	public void setEnabled(boolean enabled) {
		if (enabled) {
			switchComponent.show(comboBox);
		} else {
			switchComponent.show(textFieldDisabled);
		}
	}

	@Override
	public Object getObject() {
		if (switchComponent.getShownComponent() == comboBox) {
			if (comboBox.getSelectedObject() != null) {
				return CodeUtils.getCode(comboBox.getSelectedObject());
			}
		}
		return null;
	}

	@Override
	public void setObject(Object value) {
		if (switchComponent.getShownComponent() == textFieldDisabled) {
			return;
		}
		Object code = CodeUtils.findCode(codes, value);
		comboBox.setSelectedObject(code);
	}

	@Override
	public void fillWithDemoData() {
		int index = (int)(Math.random() * (double)codes.size());
		setObject(CodeUtils.getCode(codes.get(index)));
	}
	
}
