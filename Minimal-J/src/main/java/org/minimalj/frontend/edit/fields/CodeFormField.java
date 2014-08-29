package org.minimalj.frontend.edit.fields;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;
import org.minimalj.frontend.toolkit.TextField;
import org.minimalj.model.PropertyInterface;
import org.minimalj.transaction.criteria.Criteria;
import org.minimalj.util.CodeUtils;

// TODO: Typisierung bringt hier so was von nichts
public class CodeFormField extends AbstractEditField<Object> {
	
	private final List codes;
	private final TextField textFieldDisabled;

	public CodeFormField(PropertyInterface property, String codeName) {
		super(property, true);
		codes = Backend.getInstance().read(property.getFieldClazz(), Criteria.all(), 1000);
		
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
		Object code = CodeUtils.findCode(codes, value);
		textFieldDisabled.setText(code.toString());
	}

}
