package org.minimalj.frontend.impl.json;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.frontend.Frontend.FormContent;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.form.element.FormElementConstraint;
import org.minimalj.util.StringUtils;

public class JsonFormContent extends JsonComponent implements FormContent {

	public static final String CAPTION = "caption";
	public static final String REQUIRED = "required";
	public static final String VALIDATION_MESSAGE = "validationMessage";
	public static final String SPAN = "span";
	public static final String MIN_HEIGHT = "minHeight";
	public static final String MAX_HEIGHT = "maxHeight";

	private final List<List<JsonComponent>> rows = new ArrayList<>();
	private final int columns;
	private final List<String> rowCss = new ArrayList<>();
	
	private List<JsonComponent> actualRow = new ArrayList<>();
	private boolean startGroup, group;
	private int actualColumn;
	
	public JsonFormContent(int columns, int columnWidth) {
		super("Form");
		if (columns < 1) throw new IllegalArgumentException(JsonFormContent.class.getSimpleName() + " can only work with at least 1 column");
		this.columns = columns;

		put("columns", columns);
		put("columnWidth", columnWidth);
		
		put("rows", rows);
	}

	private void createNewRow() {
		actualRow = new ArrayList<>();
		rows.add(actualRow);
		actualColumn = 0;
		
		if (startGroup) {
			group = true;
			rowCss.add("singleGroup");
			startGroup = false;
		} else if (group) {
			String previousRowCss = rowCss.get(rowCss.size() - 1);
			if (previousRowCss.equals("singleGroup")) {
				rowCss.set(rowCss.size() - 1, "startGroup");
			} else if (previousRowCss.equals("endGroup")) {
				rowCss.set(rowCss.size() - 1, "group");
			}
			rowCss.add("endGroup");
		} else {
			rowCss.add("");
		}
	}
	
	@Override
	public void group(String caption) {
		startGroup = !StringUtils.isEmpty(caption);
		put("rowCss", rowCss);
	}

	public void add(String caption, IComponent component, FormElementConstraint constraint, int span) {
		throw new IllegalArgumentException("Migrate");
	}
	
	@Override
	public void add(String caption, boolean required, IComponent component, FormElementConstraint constraint, int span) {
		if (rows.isEmpty()) {
			createNewRow();
		}
		
		JsonComponent jsonComponent = (JsonComponent) component;
		if (caption != null) {
			jsonComponent.put(CAPTION, caption);
		}
		if (required) {
			jsonComponent.put(REQUIRED, required);
		}
		if (actualColumn >= columns) {
			createNewRow();
		}
		if (span < 1) {
			span = columns - actualColumn;
		}
		jsonComponent.put(SPAN, span);
		setHeights(constraint, jsonComponent);
		actualRow.add(jsonComponent);
		actualColumn += span;
	}

	private void setHeights(FormElementConstraint constraint, JsonComponent jsonComponent) {
		if (constraint != null && constraint.min != 1) {
			jsonComponent.put(MIN_HEIGHT, constraint.min);
		}
		if (constraint != null && constraint.max != 1) {
			jsonComponent.put(MAX_HEIGHT, constraint.max);
		}
	}

	@Override
	public void setValidationMessages(IComponent component, List<String> validationMessages) {
		JsonComponent jsonComponent = (JsonComponent) component;
		if (validationMessages.size() == 1) {
			jsonComponent.put(VALIDATION_MESSAGE, validationMessages.get(0));
		} else if (!validationMessages.isEmpty()) {
			StringBuilder s = new StringBuilder();
			for (int i = 0; i<validationMessages.size()-1; i++) {
				s.append(validationMessages.get(i)).append("\n");
			}
			s.append(validationMessages.get(validationMessages.size()-1));
			jsonComponent.put(VALIDATION_MESSAGE, s.toString());
		} else {
			jsonComponent.put(VALIDATION_MESSAGE, "");
		}
	}

	@Override
	public void setVisible(IComponent component, boolean visible) {
		((JsonComponent) component).setFormElementVisible(visible);
	}
	
}
