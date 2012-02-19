package ch.openech.mj.edit.fields;

import ch.openech.mj.db.model.AccessorInterface;
import ch.openech.mj.db.model.Constants;
import ch.openech.mj.db.model.Format;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.IComponentDelegate;
import ch.openech.mj.toolkit.TextField;

public class TypeUnknownField implements IComponentDelegate, ch.openech.mj.edit.fields.FormField<Object> {

	private final String name;
	private final TextField textField;
	
	public TypeUnknownField(Object key, AccessorInterface accessor) {
		this.name = Constants.getConstant(key);
		
		textField = ClientToolkit.getToolkit().createReadOnlyTextField();
		textField.setText(Format.class.getSimpleName() + " not found:" + accessor.getName());
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Object getComponent() {
		return textField;
	}

	@Override
	public void setObject(Object object) {
		// unused
	}

}
