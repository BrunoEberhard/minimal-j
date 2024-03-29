package org.minimalj.frontend.impl.json;

import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.InputComponentListener;
import org.minimalj.frontend.Frontend.InputType;
import org.minimalj.frontend.Frontend.Search;
import org.minimalj.model.annotation.Autocomplete.Autocompletable;
import org.minimalj.util.StringUtils;

public class JsonTextField extends JsonInputComponent<String> implements Input<String>, Autocompletable {

	public static final String MAX_LENGTH = "maxLength";
	public static final String INPUT_TYPE = "inputType";
	private static final String ALLOWED_CHARACTERS = "allowedCharacters";
	private static final String SUGGESTIONS = "suggestions";
	private static final String AUTOCOMPLETE = "autocomplete";
	private static final String PLACEHOLDER = "placeholder";
	
	private final Search<String> suggestions;
	
	private JsonTextField(String type, InputComponentListener changeListener) {
		super(type, changeListener);
		this.suggestions = null;
	}
	
	public JsonTextField(String type, int maxLength, String allowedCharacters, InputType inputType,
			Search<String> suggestions, InputComponentListener changeListener) {
		super(type, changeListener);
		this.suggestions = suggestions;
		put(MAX_LENGTH, maxLength);
		if (inputType != null) {
			if (inputType == InputType.DATETIME) {
				put(INPUT_TYPE, "datetime-local");
			} else {
				put(INPUT_TYPE, inputType.name().toLowerCase());
			}
		} else {
			put(INPUT_TYPE, "text");
			put(ALLOWED_CHARACTERS, allowedCharacters);
		}
		if (suggestions != null) {
			put(SUGGESTIONS, "true");
		}
	}
	
	public Search<String> getSuggestions() {
		return suggestions;
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
	public void setAutocomplete(String autocomplete) {
		if (!StringUtils.isEmpty(autocomplete)) {
			put(AUTOCOMPLETE, autocomplete);
		} else {
			remove(AUTOCOMPLETE);
		}
	}
	
	// experimental
	public void setPlaceholder(String placeholder) {
		putSilent(PLACEHOLDER, placeholder);
	}
}