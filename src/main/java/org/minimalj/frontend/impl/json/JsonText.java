package org.minimalj.frontend.impl.json;

import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.impl.util.HtmlString;
import org.minimalj.model.Rendering;

public class JsonText extends JsonComponent implements Input<String> {

	public JsonText(String value) {
		super("Text");
		setValue(value);
	}

	public JsonText(Rendering rendering) {
		super("Text");
		if (rendering != null) {
			CharSequence c = rendering.render();
			if (c instanceof HtmlString) {
				setValue(((HtmlString) c).getHtml());
			} else if (c != null) {
				setValue(c.toString());
			}
		}
	}

	@Override
	public void setValue(String string) {
		put(JsonInputComponent.VALUE, string);
	}

	@Override
	public String getValue() {
		return (String) get(JsonInputComponent.VALUE);
	}

	@Override
	public void setEditable(boolean editable) {
		// ignored
	}
}
