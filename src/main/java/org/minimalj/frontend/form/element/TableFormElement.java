package org.minimalj.frontend.form.element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.minimalj.application.Configuration;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.SwitchComponent;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.editor.Validator.IndexProperty;
import org.minimalj.frontend.form.Form;
import org.minimalj.model.Keys;
import org.minimalj.model.properties.ChainedProperty;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.model.validation.ValidationMessage;

public class TableFormElement<T> extends AbstractFormElement<List<T>> {
	private static final Logger logger = Logger.getLogger(TableFormElement.class.getName());

	private final SwitchComponent switchComponent;
	private final boolean editable;
	private List<T> object;
	private final List<Form<?>> rowForms = new ArrayList<>();
	private final Map<T, Form<T>> formByObject = new HashMap<>();

	private final TableRowFormFactory<? super T> formFactory;
	private final List<Action> actions;
	private final List<PropertyInterface> propertyAsChain;

	public TableFormElement(List<T> key, List<Object> columns, List<Action> actions, boolean editable) {
		this(Keys.getProperty(key), columns, actions, editable);
	}

	public TableFormElement(PropertyInterface property, List<Object> columns, List<Action> actions, boolean editable) {
		this(property, new SimpleTableRowFormFactory<T>(columns), actions, editable);
	}

	public TableFormElement(List<T> key, TableRowFormFactory<? super T> formFactory, List<Action> actions, boolean editable) {
		this(Keys.getProperty(key), formFactory, actions, editable);
	}

	public TableFormElement(PropertyInterface property, TableRowFormFactory<? super T> formFactory, List<Action> actions, boolean editable) {
		super(property);
		this.editable = editable;
		this.formFactory = formFactory;
		this.actions = Collections.unmodifiableList(actions);
		this.switchComponent = Frontend.getInstance().createSwitchComponent();
		this.propertyAsChain = ChainedProperty.getChain(getProperty());
	}

	public static interface TableRowFormFactory<T> {

		// "NonNull"
		public Form<T> create(T object, int row, boolean editable);
	}

	protected static class SimpleTableRowFormFactory<T> implements TableRowFormFactory<T> {
		private final List<Object> columns;

		public SimpleTableRowFormFactory(List<Object> columns) {
			this.columns = Collections.unmodifiableList(columns);
		}

		@Override
		public Form<T> create(T object, int row, boolean editable) {
			Form<T> rowForm = new Form<>(editable, columns.size());
			rowForm.line(columns.toArray());
			return rowForm;
		}
	}

	protected final boolean isEditable() {
		return editable;
	}

	@Override
	public List<T> getValue() {
		return object;
	}

	@Override
	public void setValue(List<T> object) {
		this.object = object;
		handleChange();
	}

	@Override
	public IComponent getComponent() {
		return switchComponent;
	}

	@Override
	public String getCaption() {
		return null;
	}

	protected void handleChange() {
		update();
		super.fireChange();
	}

	public void clearFormCache() {
		formByObject.clear();
	}
	
	private void update() {
		int rowCount = object.size();
		if (!actions.isEmpty()) {
			rowCount = rowCount + 1;
		}
		IComponent[] rows = new IComponent[rowCount];
		if (!actions.isEmpty()) {
			rows[rowCount - 1] = createActions(actions);
		}
		rowForms.clear();
		for (int index = 0; index < object.size(); index++) {
			T rowObject = object.get(index);
			int i = index;
			@SuppressWarnings("unchecked")
			Form<T> rowForm = (Form<T>) formByObject.computeIfAbsent(rowObject, r -> (Form<T>) formFactory.create(r, i, editable));
			rowForm = checkNonNull(rowForm, rowObject);
			rowForms.add(rowForm);
			rowForm.setChangeListener(form -> super.fireChange());
			rowForm.setObject(rowObject);
			rows[index] = rowForm.getContent();
		}
		List<T> unsedForms = formByObject.entrySet().stream().filter(e -> !rowForms.contains(e.getValue())).map(e -> e.getKey()).collect(Collectors.toList());
		unsedForms.forEach(formByObject::remove);

		IComponent vertical = Frontend.getInstance().createVerticalGroup(rows);
		switchComponent.show(vertical);
	}

	private IComponent createActions(List<Action> actions) {
		IComponent[] components = new IComponent[actions.size()];
		for (int i = 0; i < actions.size(); i++) {
			components[i] = Frontend.getInstance().createText(actions.get(i));
		}
		return Frontend.getInstance().createHorizontalGroup(components);
	}

	private Form<T> checkNonNull(Form<T> rowForm, T rowObject) {
		if (rowForm == null) {
			rowForm = new Form<>();
			String message = formFactory + " should not return null for " + rowObject;
			if (Configuration.isDevModeActive()) {
				rowForm.addTitle("No form for " + rowObject);
				logger.severe(message);
			} else {
				logger.warning(message);
			}
		}
		return rowForm;
	}

	public void setValidationMessages(List<ValidationMessage> validationMessages) {
		@SuppressWarnings("unchecked")
		ArrayList<ValidationMessage>[] validationMessagesByRow = new ArrayList[rowForms.size()];

		for (ValidationMessage message : validationMessages) {
			List<PropertyInterface> messagePropertyAsChain = ChainedProperty.getChain(message.getProperty());

			PropertyInterface unchained = ChainedProperty.buildChain(messagePropertyAsChain.subList(propertyAsChain.size() + 1, messagePropertyAsChain.size()));
			IndexProperty indexProperty = (IndexProperty) messagePropertyAsChain.get(propertyAsChain.size());
			int index = indexProperty.getIndex();

			message = new ValidationMessage(unchained, message.getFormattedText());
			if (validationMessagesByRow[index] == null) {
				validationMessagesByRow[index] = new ArrayList<>();
			}
			validationMessagesByRow[index].add(message);
		}

		for (int i = 0; i < rowForms.size(); i++) {
			rowForms.get(i).indicate(validationMessagesByRow[i] != null ? validationMessagesByRow[i] : Collections.emptyList());
		}
	}

}
