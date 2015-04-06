package org.minimalj.frontend.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.minimalj.frontend.lanterna.component.LanternaForm;
import org.minimalj.frontend.toolkit.ClientToolkit.IComponent;
import org.minimalj.frontend.toolkit.FormContent;
import org.minimalj.model.validation.ValidationMessage;

public class JsonFormContent extends JsonComponent implements FormContent {

	public static final String CAPTION = "caption";
	public static final String VALIDATION_MESSAGE = "validationMessage";
	public static final String SPAN = "span";

	private final List<List<Map<String, Object>>> rows = new ArrayList<>();
	private final int columns;
	
	private List<Map<String, Object>> actualRow = new ArrayList<>();
	private int actualColumn;
	
	public JsonFormContent(int columns, int columnWidth) {
		super("Form");
		if (columns < 1) throw new IllegalArgumentException(LanternaForm.class.getSimpleName() + " can only work with at least 1 column");
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
		createNewRow();
		actualRow.add(jsonComponent.getValues());
		addComponent(jsonComponent);
		actualColumn = columns;
	}

	@Override
	public void add(String caption, IComponent component, int span) {
		JsonValueComponent jsonComponent = (JsonValueComponent) component;
		jsonComponent.put(CAPTION, caption);
		if (actualColumn >= columns) {
			createNewRow();
		}
		if (span > 1) {
			jsonComponent.put(SPAN, span);
		}
		actualRow.add(jsonComponent.getValues());
		addComponent(jsonComponent);
		actualColumn += span;
	}

	@Override
	public void setValidationMessages(IComponent component, List<String> validationMessages) {
		JsonValueComponent jsonComponent = (JsonValueComponent) component;
		if (!validationMessages.isEmpty()) {
			String validationMessage = ValidationMessage.formatHtmlString(validationMessages);
			jsonComponent.put(VALIDATION_MESSAGE, validationMessage);
		} else {
			jsonComponent.put(VALIDATION_MESSAGE, "");
		}
	}

}
