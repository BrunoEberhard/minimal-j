package org.minimalj.frontend.edit.fields;

import java.util.List;

import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;
import org.minimalj.frontend.toolkit.TextField;
import org.minimalj.model.PropertyInterface;
import org.minimalj.model.annotation.Code;
import org.minimalj.util.Codes;

public class CodeFormField<T extends Code> extends AbstractEditField<Object> {
	
	private final List<T> codes;
	private final TextField textFieldDisabled;

	public CodeFormField(PropertyInterface property) {
		super(property, true);
		codes = Codes.get((Class<T>) property.getFieldClazz());
		
		textFieldDisabled = ClientToolkit.getToolkit().createReadOnlyTextField();
	}
	
	@Override
	public IComponent getComponent() {
		return textFieldDisabled;
	}

	@Override
	public Object getObject() {
		throw new IllegalStateException();
	}

	@Override
	public void setObject(Object value) {
		Object code = Codes.findCode(codes, value);
		textFieldDisabled.setText(code != null ? code.toString() : null);
	}

}
