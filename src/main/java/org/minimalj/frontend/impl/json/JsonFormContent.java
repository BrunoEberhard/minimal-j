package org.minimalj.frontend.impl.json;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.frontend.Frontend.FormContent;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.form.element.FormElementConstraint;
import org.minimalj.util.StringUtils;

public class JsonFormContent extends JsonComponent implements FormContent {

	public static final String CAPTION = "caption";
	public static final String REQUIRED = "required";
	public static final String VALIDATION_MESSAGE = "validationMessage";
	public static final String SPAN = "span";
	public static final String MIN_HEIGHT = "minHeight";
	public static final String MAX_HEIGHT = "maxHeight";

	public static final String CSS_GROUP_SINGLE_ROW = "groupSingleRow";
	public static final String CSS_GROUP_START = "groupStart";
	public static final String CSS_GROUP_END = "groupEnd";
	public static final String CSS_GROUP = "group";
	
	public static final String CSS_IGNORE_CAPTION = "ignoreCaption";

	
	private final List<List<JsonComponent>> rows = new ArrayList<>();
	private final int columns;
	private final List<String> rowCss = new ArrayList<>();
	
	private List<JsonComponent> actualRow = new ArrayList<>();
	private boolean startGroup = true;
	private int actualColumn;
	private boolean ignoreCaption = false;
	
	public JsonFormContent(int columns, int columnWidth) {
		super("Form");
		if (columns < 1) throw new IllegalArgumentException(JsonFormContent.class.getSimpleName() + " can only work with at least 1 column");
		this.columns = columns;

		put("columns", columns);
		put("columnWidth", columnWidth);
		
		put("rows", rows);
		put("rowCss", rowCss);
	}

	private void createNewRow() {
		actualRow = new ArrayList<>();
		rows.add(actualRow);
		actualColumn = 0;
		
		String ignoreCaptionCss = ignoreCaption ? " " + CSS_IGNORE_CAPTION : "";
		if (startGroup) {
			rowCss.add(CSS_GROUP_SINGLE_ROW + ignoreCaptionCss);
			startGroup = false;
		} else {
			int previousIndex = rowCss.size() - 1;
			String previousRowCss = rowCss.get(previousIndex);
			previousRowCss = previousRowCss.replace(CSS_GROUP_SINGLE_ROW, CSS_GROUP_START);
			previousRowCss = previousRowCss.replace(CSS_GROUP_END, CSS_GROUP);
			rowCss.set(previousIndex, previousRowCss);
			rowCss.add(CSS_GROUP_END + ignoreCaptionCss);
		}
	}
	
	@Override
	public void group(String caption) {
		startGroup = true;
	}
	
	@Override
	public void setIgnoreCaption(boolean ignoreCaption) {
		this.ignoreCaption = ignoreCaption;
	}

	public void add(String caption, IComponent component, FormElementConstraint constraint, int span) {
		throw new IllegalArgumentException("Migrate");
	}
	
	@Override
	public void add(String caption, boolean required, IComponent component, FormElementConstraint constraint, int span) {
		if (rows.isEmpty()) {
			createNewRow();
		}
		
		JsonComponent jsonComponent = component != null ? (JsonComponent) component : new JsonComponent("Empty");
		if (!StringUtils.isBlank(caption)) {
			jsonComponent.put(CAPTION, caption);
		} else if (!ignoreCaption && (jsonComponent instanceof JsonText || jsonComponent instanceof Input<?> || jsonComponent instanceof JsonAction)) {
			// if there is no caption the component needs an offset or would be
			// displayed too high.
			// (this is not the case if ignoreCaption is active, then all components are 
			// on upper edge)
			jsonComponent.setCssClass("noCaption");
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
