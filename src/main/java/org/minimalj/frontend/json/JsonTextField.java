package org.minimalj.frontend.json;

import org.minimalj.frontend.toolkit.ClientToolkit.InputComponentListener;
import org.minimalj.frontend.toolkit.ClientToolkit.InputType;
import org.minimalj.frontend.toolkit.ClientToolkit.Search;
import org.minimalj.frontend.toolkit.TextField;

public class JsonTextField extends JsonInputComponent<String> implements TextField {

	private static final String MAX_LENGTH = "maxLength";
	private static final String ALLOWED_CHARACTERS = "allowedCharacters";
	private static final String INPUT_TYPE = "inputType";
	private static final String AUTOCOMPLETE = "autocomplete";
	
	public JsonTextField(String type) {
		super(type, null);
	}
	
	public JsonTextField(String type, InputComponentListener changeListener) {
		super(type, changeListener);
	}
	
	public JsonTextField(String type, int maxLength, String allowedCharacters, InputType inputType, Search<String> autocomplete,
			InputComponentListener changeListener) {
		super(type, changeListener);
		put(MAX_LENGTH, maxLength);
		put(ALLOWED_CHARACTERS, allowedCharacters);
		if (inputType != null) {
			put(INPUT_TYPE, inputType.name());
		}
	}
	
	@Override
	public void setValue(String text) {
		put(VALUE, text);
	}

	@Override
	public String getValue() {
		return (String) get(VALUE);
	}

	@Override
	public void setCommitListener(Runnable runnable) {
		// ?
	}
}
