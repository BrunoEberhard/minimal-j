package org.minimalj.frontend.impl.javafx.toolkit;

import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.InputComponentListener;
import org.minimalj.frontend.Frontend.Search;

import javafx.beans.value.ChangeListener;

public class FxTextField extends javafx.scene.control.TextField implements Input<String> {

	public FxTextField(int maxLength, String allowedCharacters, Search<String> suggestionSearch, InputComponentListener changeListener) {
		textProperty().addListener((ChangeListener<String>) (observable, oldValue, newValue) -> changeListener.changed(FxTextField.this));

//		UnaryOperator<Change> modifyChange = c -> {
//		    if (c.isContentChange()) {
//		        int newLength = c.getControlNewText().length();
//		        if (newLength > len) {
//		            // replace the input text with the last len chars
//		            String tail = c.getControlNewText().substring(newLength - len, newLength);
//		            c.setText(tail);
//		            // replace the range to complete text
//		            // valid coordinates for range is in terms of old text
//		            int oldLength = c.getControlText().length();
//		            c.setRange(0, oldLength);
//		        }
//		    }
//		    return c;
//		};
//		setTextFormatter(new TextFormatter(modifyChange));

	}

	@Override
	public void setValue(String value) {
		setText(value);
	}

	@Override
	public String getValue() {
		return getText();
	}

}
