package org.minimalj.frontend.edit.fields;

import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.frontend.toolkit.IComponent;
import org.minimalj.frontend.toolkit.TextField;
import org.minimalj.model.Code;
import org.minimalj.model.Codes;
import org.minimalj.model.PropertyInterface;

// TODO: Typisierung bringt hier so was von nichts
public class CodeFormField extends AbstractEditField<String> {
	
	private final Code code;
	private final TextField textFieldDisabled;

	public CodeFormField(PropertyInterface property, String codeName) {
		super(property, true);
		this.code = Codes.getCode(codeName);
		
		textFieldDisabled = ClientToolkit.getToolkit().createReadOnlyTextField();
	}
	
	@Override
	public IComponent getComponent() {
		return textFieldDisabled;
	}

	@Override
	public String getObject() {
		throw new IllegalStateException();
	}

	@Override
	public void setObject(String value) {
		textFieldDisabled.setText(code.getText(value));
	}
	
}
