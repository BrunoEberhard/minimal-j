package org.minimalj.test.headless;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.junit.jupiter.api.Assertions;
import org.minimalj.frontend.Frontend.SwitchComponent;
import org.minimalj.frontend.impl.json.JsonAction;
import org.minimalj.frontend.impl.json.JsonCheckBox;
import org.minimalj.frontend.impl.json.JsonCombobox;
import org.minimalj.frontend.impl.json.JsonComponent;
import org.minimalj.frontend.impl.json.JsonFormContent;
import org.minimalj.frontend.impl.json.JsonInputComponent;
import org.minimalj.frontend.impl.json.JsonLookup;
import org.minimalj.frontend.impl.json.JsonLookupActions;
import org.minimalj.frontend.impl.json.JsonPasswordField;
import org.minimalj.model.Rendering;
import org.minimalj.test.PageContainerTestFacade.ActionTestFacade;
import org.minimalj.test.PageContainerTestFacade.FormElementTestFacade;
import org.minimalj.test.PageContainerTestFacade.FormTestFacade;
import org.minimalj.test.headless.HeadlessFrontend.HeadlessRadioButtons;
import org.minimalj.test.headless.HeadlessFrontend.HeadlessSwitch;
import org.minimalj.util.StringUtils;

public class HeadlessFormTestFacade implements FormTestFacade {

	private final JsonFormContent form;

	public HeadlessFormTestFacade(JsonFormContent form) {
		this.form = form;
	}

	public List<List<JsonComponent>> getRows() {
		return (List<List<JsonComponent>>) form.get("rows");
	}

	@Override
	public void printElementCaptions() {
		var captions = new ArrayList<String>();
		collectCaptions(form, captions);
		System.out.println("Captions of visible elements: " + String.join(", ", captions));
	}
	
	@Override
	public FormElementTestFacade getElement(String caption, Boolean isBooleanValue) {
		var element = getElement(form, caption, isBooleanValue);
		if (element == null) {
			var captions = new ArrayList<String>();
			collectCaptions(form, captions);
			System.out.println(caption + " not in: " + String.join(", ", captions));
		}
		return element;
	}

	@Override
	public FormElementTestFacade getElement(String caption, int index) {
		JsonComponent component = getComponents(form, c -> caption.equals(c.get(JsonFormContent.CAPTION)) || caption.equals(c.get("text"))).get(index);
		component = HeadlessFormTestFacade.unpackComponent(component);
		return new HeadlessFormElement(component);
	}

	@Override
	public FormElementTestFacade getElement(int row, int column) {
		return new HeadlessFormElement(getRows().get(row).get(column));
	}

	private FormElementTestFacade getElement(Object o, String caption, Boolean isBooleanValue) {
		if (o instanceof JsonComponent jsonComponent) {
			if (Boolean.TRUE.equals(jsonComponent.get("hideFormElement"))) {
				return null;
			}
			if (caption.equals(JsonFormContent.getCaptionOrName(jsonComponent))) {
				// TODO check isBooleanValue
				return new HeadlessFormElement(jsonComponent);
			}
		}
		if (o instanceof Collection collection) {
			for (var item : collection) {
				var result = getElement(item, caption, isBooleanValue);
				if (result != null) {
					return result;
				}
			}
		}
		if (o instanceof Map map) {
			for (var item : map.values()) {
				var result = getElement(item, caption, isBooleanValue);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}
	
	private void collectCaptions(Object o, List<String> captions) {
		if (o instanceof JsonComponent jsonComponent) {
			String caption = (String) jsonComponent.get(JsonFormContent.CAPTION);
			if (!StringUtils.isEmpty(caption)) {
				if (Boolean.TRUE.equals(jsonComponent.get("hideFormElement"))) {
					captions.add(caption + " (hidden)");
				} else {
					captions.add(caption);
				}
			}
		}
		if (o instanceof Collection collection) {
			collection.forEach(i -> collectCaptions(i, captions));
		}
		if (o instanceof Map map) {
			map.values().forEach(i -> collectCaptions(i, captions));
		}
	}

	@Override
	public void set(String caption, boolean checked) {
		for (var row : getRows()) {
			for (var cell : row) {
				cell = unpackComponent(cell);
				if (cell instanceof JsonCheckBox jsonCheckBox && (caption.equals(cell.get("text")) || caption.equals(cell.get(JsonFormContent.CAPTION)))) {
					jsonCheckBox.changedValue(checked);
				}
				if (cell instanceof HeadlessRadioButtons radioButtons) {
					radioButtons.setValueText(caption);
				}
			}
		}
	}

	private JsonComponent getComponent(Object o, Predicate<JsonComponent> filter) {
		var components = getComponents(o, filter);
		return components.isEmpty() ? null : components.get(0);
	}

	private List<JsonComponent> getComponents(Object o, Predicate<JsonComponent> filter) {
		List<JsonComponent> components = new ArrayList<>();
		getComponents(o, filter, components);
		return components;
	}

	private void getComponents(Object o, Predicate<JsonComponent> filter, List<JsonComponent> components) {
		if (o instanceof JsonComponent jsonComponent) {
			if (Boolean.TRUE.equals(jsonComponent.get("hideFormElement"))) {
				return;
			}
			if (filter.test(jsonComponent)) {
				components.add(jsonComponent);
			}
		}
		if (o instanceof Collection collection) {
			for (var item : collection) {
				getComponents(item, filter, components);
			}
		}
		if (o instanceof Map map) {
			for (var item : map.values()) {
				getComponents(item, filter, components);
			}
		}
	}

	@Override
	public ActionTestFacade getAction(String label) {
		Predicate<JsonComponent> filter = new Predicate<JsonComponent>() {
			@Override
			public boolean test(JsonComponent component) {
				return component instanceof JsonAction && label.equals(component.get("text"));
			}
		};
		JsonAction jsonAction = (JsonAction) getComponent(form, filter);
		if (jsonAction != null) {
			return new ActionTestFacade() {
				@Override
				public void run() {
					jsonAction.run();
				}

				@Override
				public boolean isEnabled() {
					return jsonAction.isEnabled();
				}
			};
		}
		return null;
	}

	public static JsonComponent unpackComponent(JsonComponent component) {
		while (component instanceof SwitchComponent) {
			component = (JsonComponent) component.get("component");
		}
		return component;
	}

	public static JsonComponent unpackText(JsonComponent component) {
		component = unpackComponent(component);
		if (component instanceof JsonLookup jsonLookup) {
			component = (JsonComponent) jsonLookup.get("input");
		}
		if (component instanceof JsonLookupActions jsonLookupActions) {
			component = (JsonComponent) jsonLookupActions.get("input");
		}
		return component;
	}

	public static String getText(JsonComponent component) {
		component = unpackText(component);
		if (component instanceof HeadlessRadioButtons radioButtons) {
			return radioButtons.getValueText();
		} else if (component instanceof JsonCombobox jsonCombobox) {
			var value = jsonCombobox.getValue();
			if (value == null) {
				return (String) jsonCombobox.get("nullText");
			} else {
				return Rendering.toString(value);
			}
		} else {
			if (component instanceof JsonInputComponent inputComponent || component.containsKey(JsonInputComponent.VALUE)) {
				return (String) component.get(JsonInputComponent.VALUE);
			} else if (component instanceof JsonAction jsonAction) {
				return (String) jsonAction.get("text");
			} else {
				throwNotInputComponent(component);
				return null;
			}
		}
	}
	
	public static void setText(JsonComponent component, String value) {
		if (component instanceof JsonPasswordField passwordField) {
			passwordField.changedValue(value.toCharArray());
		} else if (component instanceof JsonCombobox jsonCombobox) {
			var options = (LinkedHashMap<String, Map<String, String>>) jsonCombobox.get("options");
			for (var optionEntry : options.entrySet()) {
				var caption = optionEntry.getValue().get("text");
				if (StringUtils.equals(caption, value)) {
					jsonCombobox.changedValue(optionEntry.getKey());
					return;
				}
			}
			if (jsonCombobox.containsKey("nullText") && StringUtils.equals((String) jsonCombobox.get("nullText"), value)) {
				jsonCombobox.changedValue(null);
			}
		} else {
			component = unpackText(component);
			if (component instanceof JsonInputComponent inputComponent) {
				inputComponent.changedValue(value);
			} else {
				throwNotInputComponent(component);
			}
		}
	}
	
	private static void throwNotInputComponent(JsonComponent component) {
		String type = (String) component.get("type");
		if (StringUtils.equals(type, "groupVertical", "groupHorizontal")) {
			throw new IllegalArgumentException("Component is a group not an input. Please use element.groupItem(x) to select item in group");
		} else {
			throw new IllegalArgumentException("Component is not an input but a " + type);
		}
	}

	public static class HeadlessFormElement implements FormElementTestFacade {
		private final JsonComponent component;

		public HeadlessFormElement(JsonComponent component) {
			this.component = component;
		}

		private JsonComponent getComponent() {
			return unpackComponent(component);
		}

		@Override
		public String getText() {
			return HeadlessFormTestFacade.getText(component);
		}

		@Override
		public void setText(String value) {
			var component = getComponent();
			HeadlessFormTestFacade.setText(component, value);
		}
		
		@Override
		public FormElementTestFacade groupItem(int... positions) {
			var component = getComponent();
			for (int pos : positions) {
				component = (JsonComponent) ((List) component.get("components")).get(pos);
			}
			return new HeadlessFormElement(component);
		}

		@Override
		public void setChecked(boolean checked) {
			if (getComponent() instanceof JsonCheckBox jsonCheckBox) {
				jsonCheckBox.changedValue(checked);
			}
		}

		@Override
		public boolean isChecked() {
			if (getComponent() instanceof JsonCheckBox jsonCheckBox) {
				return jsonCheckBox.getValue();
			} else {
				Assertions.fail("Should be a CheckBox not a " + getComponent().getClass().getSimpleName());
				return false;
			}
		}

		@Override
		public String getValidation() {
			return (String) component.get(JsonFormContent.VALIDATION_MESSAGE);
		}

		@Override
		public void lookup() {
			var component = getComponent();
			if (component instanceof JsonLookup jsonLookup) {
				jsonLookup.showLookupDialog();
			}
		}

		@Override
		public List<String> getComboBoxValues() {
			var component = getComponent();
			if (component instanceof JsonCombobox jsonCombobox) {
				var options = (LinkedHashMap<String, Map<String, String>>) jsonCombobox.get("options");
				return options.values().stream().map(m -> m.get("text")).toList();
			}
			Assertions.fail("Should be a Combobox not a " + getComponent().getClass().getSimpleName());
			return null;
		}

		@Override
		public void action(String text) {
			if (getComponent() instanceof JsonLookupActions jsonLookupActions) {
				List<JsonAction> actionLabels = (List<JsonAction>) jsonLookupActions.get("actions");
				for (var jsonAction : actionLabels) {
					if (text.equals(jsonAction.get("text"))) {
						jsonAction.run();
						return;
					}
				}
			}
		}

		private JsonComponent getLineComponent(int line) {
			var component = getComponent();
			if (component.get("components") instanceof List components) {
				return unpackComponent((JsonComponent) components.get(line));
			}
			return null;
		}

		@Override
		public String getLine(int line) {
			var component = getLineComponent(line);
			return HeadlessFormTestFacade.getText(component);
		}

		@Override
		public List<ActionTestFacade> getLineActions(int line) {
			var component = getLineComponent(line);
			if (component instanceof JsonLookupActions jsonLookupActions) {
				var actionTestFacades = new ArrayList<ActionTestFacade>();
				var actions = (List<JsonAction>) jsonLookupActions.get("actions");
				for (var action : actions) {
					actionTestFacades.add(new ActionTestFacade() {
						@Override
						public void run() {
							action.run();
						}

						@Override
						public boolean isEnabled() {
							return action.isEnabled();
						}
						
						@Override
						public String getDescription() {
							return (String) action.get("description");
						}
					});
				}
				return actionTestFacades;
			} else {
				return null;
			}
		}

		@Override
		public FormTestFacade row(int pos) {
			var component = getComponent();
			if (component.get("components") instanceof List components) {
				component = (JsonComponent) components.get(pos);
				if (component instanceof JsonFormContent jsonFormContent) {
					return new HeadlessFormTestFacade(jsonFormContent);
				}
				Assertions.fail("Should be a Form");
			}
			Assertions.fail("Should be a vertical group, not " + component.get("type"));
			return null;
		}
	}

}
