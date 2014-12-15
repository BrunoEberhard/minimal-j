package org.minimalj.frontend.edit.fields;

import java.util.List;
import java.util.Locale;

import org.minimalj.frontend.toolkit.ClientToolkit;
import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;
import org.minimalj.frontend.toolkit.TextField;
import org.minimalj.model.Code;
import org.minimalj.model.Rendering;
import org.minimalj.model.Rendering.RenderType;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.Codes;

public class CodeFormField<T extends Code> extends AbstractEditField<Object> {
	
	private final List<T> codes;
	private final TextField textFieldDisabled;

	@SuppressWarnings("unchecked")
	public CodeFormField(PropertyInterface property) {
		super(property, true);
		codes = Codes.get((Class<T>) property.getClazz());
		
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
		Code code = Codes.findCode(codes, value);
		String text;
		if (code instanceof Rendering) {
			text = ((Rendering) code).render(RenderType.PLAIN_TEXT, Locale.getDefault());
		} else if (code != null) {
			text = code.toString();
		} else {
			text = null;
		}
		textFieldDisabled.setText(text);
	}

}
