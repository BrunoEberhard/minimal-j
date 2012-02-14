package ch.openech.mj.edit.fields;

import ch.openech.mj.db.model.Constants;
import ch.openech.mj.db.model.Format;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.IComponentDelegate;
import ch.openech.mj.toolkit.TextField;


public class TextFormField implements IComponentDelegate, FormField<String> {

	private final String name;
	private final Format format;
	private final TextField textField;
	
	public TextFormField(Object key, Format format) {
		this.name = Constants.getConstant(key);
		this.format = format;
		this.textField = ClientToolkit.getToolkit().createTextField();
		
		textField.setEditable(false);
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
	public void setObject(String string) {
		if (format != null) {
			string = format.display(string);
		}
		textField.setText(string);
	}

}
