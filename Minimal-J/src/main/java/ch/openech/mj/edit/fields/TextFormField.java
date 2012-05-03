package ch.openech.mj.edit.fields;

import ch.openech.mj.db.model.Constants;
import ch.openech.mj.db.model.Format;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.IComponent;
import ch.openech.mj.toolkit.TextField;


public class TextFormField implements FormField<String> {

	private final String name;
	private final Format format;
	private final TextField textField;
	
	public TextFormField(Object key, Format format) {
		this.name = Constants.getConstant(key);
		this.format = format;
		this.textField = ClientToolkit.getToolkit().createReadOnlyTextField();
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public IComponent getComponent() {
		return textField;
	}

	@Override
	public void setObject(String string) {
		if (format != null) {
			string = format.display(string);
		}
		textField.setText(string);
	}

	public void setEnabled(boolean enabled) {
		textField.setEnabled(enabled);
	}

}
