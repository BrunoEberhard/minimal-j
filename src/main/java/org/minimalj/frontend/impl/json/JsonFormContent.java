package org.minimalj.frontend.impl.json;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.frontend.Frontend.FormContent;
import org.minimalj.frontend.Frontend.IComponent;

public class JsonFormContent extends JsonComponent implements FormContent {

	public static final String CAPTION = "caption";
	public static final String VALIDATION_MESSAGE = "validationMessage";
	public static final String SPAN = "span";

	private final List<List<JsonComponent>> rows = new ArrayList<>();
	private final int columns;
	
	private List<JsonComponent> actualRow = new ArrayList<>();
	private int actualColumn;
	
	public JsonFormContent(int columns, int columnWidth) {
		super("Form");
		if (columns < 1) throw new IllegalArgumentException(JsonFormContent.class.getSimpleName() + " can only work with at least 1 column");
		this.columns = columns;

		put("columns", columns);
		put("columnWidth", columnWidth);
		
		put("rows", rows);
		
		createNewRow();
	}

	private void createNewRow() {
		actualRow = new ArrayList<>();
		rows.add(actualRow);
		actualColumn = 0;
	}
	
	@Override
	public void add(IComponent component) {
		JsonComponent jsonComponent = (JsonComponent) component;
		if (actualColumn > 0) {
			createNewRow();
		}
		if (columns > 1) {
			jsonComponent.put(SPAN, columns);
		}
		actualRow.add(jsonComponent);
		actualColumn = columns;
	}

	@Override
	public void add(String caption, IComponent component, int span) {
		JsonComponent jsonComponent = (JsonComponent) component;
		jsonComponent.put(CAPTION, caption);
		if (actualColumn >= columns) {
			createNewRow();
		}
		if (span > 1) {
			jsonComponent.put(SPAN, span);
		}
		actualRow.add(jsonComponent);
		actualColumn += span;
	}

	@Override
	public void setValidationMessages(IComponent component, List<String> validationMessages) {
		JsonComponent jsonComponent = (JsonComponent) component;
		if (validationMessages.size() == 1) {
			jsonComponent.put(VALIDATION_MESSAGE, validationMessages.get(0));
		} else if (!validationMessages.isEmpty()) {
			StringBuilder s = new StringBuilder();
			for (int i = 0; i<validationMessages.size()-1; i++) {
				s.append(validationMessages.get(i));
				s.append("<BR>");
			}
			s.append(validationMessages.get(validationMessages.size()-1));
		} else {
			jsonComponent.put(VALIDATION_MESSAGE, "");
		}
	}

}
