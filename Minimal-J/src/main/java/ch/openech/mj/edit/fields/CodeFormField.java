package ch.openech.mj.edit.fields;

import ch.openech.mj.db.model.Code;
import ch.openech.mj.db.model.PropertyInterface;
import ch.openech.mj.model.Codes;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.TextField;

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
