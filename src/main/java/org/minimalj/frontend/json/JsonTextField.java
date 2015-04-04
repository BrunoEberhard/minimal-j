package org.minimalj.frontend.json;

import org.minimalj.frontend.toolkit.ClientToolkit.InputComponentListener;
import org.minimalj.frontend.toolkit.ClientToolkit.InputType;
import org.minimalj.frontend.toolkit.ClientToolkit.Search;
import org.minimalj.frontend.toolkit.IFocusListener;
import org.minimalj.frontend.toolkit.TextField;

public class JsonTextField extends JsonComponent implements TextField {

	private static final String EDITABLE = "editable";
	private static final String MAX_LENGTH = "maxLength";
	private static final String ALLOWED_CHARACTERS = "allowedCharacters";
	private static final String INPUT_TYPE = "inputType";
	private static final String AUTOCOMPLETE = "autocomplete";
	
	public JsonTextField(String component) {
		super(component);
	}

	public JsonTextField(String clazz, int maxLength, String allowedCharacters, InputType inputType, Search<String> autocomplete,
			InputComponentListener changeListener) {
		this(clazz);
		put(MAX_LENGTH, MAX_LENGTH);
		put(ALLOWED_CHARACTERS, allowedCharacters);
		if (inputType != null) {
			put(INPUT_TYPE, inputType.name());
		}
		// TODO autocomplete, changeListener
	}

	@Override
	public void setText(String text) {
		put(VALUE, text);
	}

	@Override
	public String getText() {
		return (String) get(VALUE);
	}

	@Override
	public void setEditable(boolean editable) {
		put(EDITABLE, editable);
	}

	@Override
	public void setFocusListener(IFocusListener focusListener) {
		// ?
	}

	@Override
	public void setCommitListener(Runnable runnable) {
		// ?
	}
}
