package org.minimalj.frontend.form.element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.SwitchComponent;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.editor.Validator.IndexProperty;
import org.minimalj.frontend.form.Form;
import org.minimalj.model.Keys;
import org.minimalj.model.properties.ChainedProperty;
import org.minimalj.model.properties.Property;
import org.minimalj.model.validation.ValidationMessage;

public class TableFormElement<T> extends AbstractFormElement<List<T>> {
	private static final Logger logger = Logger.getLogger(TableFormElement.class.getName());

	private final SwitchComponent switchComponent;
	private final boolean editable;
	private List<T> object;
	private final List<Form<T>> rowForms = new ArrayList<>();
	private final Map<T, Form<T>> formByObject = new HashMap<>();

	private final TableRowFormFactory<? super T> formFactory;
	private final List<Property> propertyAsChain;

	public TableFormElement(List<T> key, List<Object> columns, List<Action> actions, boolean editable) {
		this(Keys.getProperty(key), columns, actions, editable);
	}

	public TableFormElement(Property property, List<Object> columns, List<Action> actions, boolean editable) {
		this(property, new SimpleTableRowFormFactory<T>(columns, actions), editable);
	}

	@Deprecated // actions not used anymore, RowFormFactory provides footer
	public TableFormElement(List<T> key, TableRowFormFactory<? super T> formFactory, List<Action> actions, boolean editable) {
		this(key, formFactory, editable);
	}	
	
	public TableFormElement(List<T> key, TableRowFormFactory<? super T> formFactory, boolean editable) {
		this(Keys.getProperty(key), formFactory, editable);
	}

	public TableFormElement(Property property, TableRowFormFactory<? super T> formFactory, boolean editable) {
		super(property);
		this.editable = editable;
		this.formFactory = formFactory;
		this.switchComponent = Frontend.getInstance().createSwitchComponent();
		this.propertyAsChain = ChainedProperty.getChain(getProperty());
		update(true);
	}

	public static interface TableRowFormFactory<T> {

		// "NonNull"
		public Form<T> create(T object, int row, boolean editable);
		
		public default IComponent createFooter() {
			return null;
		}
		
		public static IComponent createFooter(List<Action> actions, int columns, int columnWidth) {
			if (actions != null && !actions.isEmpty()) {
				Form<Object> rowForm = new Form<>(Form.READ_ONLY, columns, columnWidth);
				rowForm.setIgnoreCaption(true);
				List<Object> actionElements = new ArrayList<>();
				for (Action action : actions) {
					if (actionElements.size() == columns) {
						rowForm.line(actionElements.toArray());
						actionElements.clear();
					}
					actionElements.add(new ActionFormElement(action));
				}
				while (actionElements.size() < columns) {
					actionElements.add("");
				}
				rowForm.line(actionElements.toArray());
				return rowForm.getContent();
			} else {
				return null;
			}
		}
	}

	protected static class SimpleTableRowFormFactory<T> implements TableRowFormFactory<T> {
		private final List<Object> columns;
		private final List<Action> actions;
		
		public SimpleTableRowFormFactory(List<Object> columns, List<Action> actions) {
			this.columns = Collections.unmodifiableList(columns);
			this.actions = actions;
		}

		@Override
		public Form<T> create(T object, int row, boolean editable) {
			Form<T> rowForm = new Form<>(editable, columns.size());
			rowForm.line(columns.toArray());
			return rowForm;
		}
		
		@Override
		public IComponent createFooter() {
			return TableRowFormFactory.createFooter(actions, columns.size(), Form.DEFAULT_COLUMN_WIDTH);
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
		update(false);
	}
	
	/**
	 * If an action removes an object the validation message of that object should
	 * be removed. For this a change must be fired to let the editor do a
	 * validation. To always fire a change in setValue is not a solution. setValue
	 * is mainly used by the Editor
	 * 
	 * @param object the updated list. Could be the same as before. The FormElement
	 *               is updated anyway.
	 */
	public void setValueAndFireChange(List<T> object) {
		setValue(object);
		fireChange();
	}

	@Override
	public IComponent getComponent() {
		return switchComponent;
	}
	
	@Override
	public FormElementConstraint getConstraint() {
		return new FormElementConstraint(1, FormElementConstraint.MAX);
	}

	@Override
	public String getCaption() {
		return null;
	}

	public void clearFormCache() {
		formByObject.clear();
	}
	
	private void update(boolean force) {
		List<IComponent> rows = new ArrayList<>();
		rowForms.clear();
		boolean structuralChange = force;
		if (object != null) {
			for (int index = 0; index < object.size(); index++) {
				T rowObject = object.get(index);
				int i = index;
				structuralChange |= !formByObject.containsKey(rowObject);
				@SuppressWarnings("unchecked")
				Form<T> rowForm = (Form<T>) formByObject.computeIfAbsent(rowObject, r -> (Form<T>) formFactory.create(r, i, editable));
				rowForms.add(rowForm);
				if (rowForm != null) {
					rowForm.setChangeListener(form -> fireChange(i, rowObject, rowForm));
					rowForm.setObject(rowObject);
					rows.add(rowForm.getContent());
					
				}
			}
			List<T> unsedForms = formByObject.entrySet().stream().filter(e -> !rowForms.contains(e.getValue())).map(e -> e.getKey()).collect(Collectors.toList());
			unsedForms.forEach(formByObject::remove);
			structuralChange |= !unsedForms.isEmpty();
		}

		if (structuralChange) {
			addFooter(rows);
			IComponent vertical = Frontend.getInstance().createVerticalGroup(rows.toArray(new IComponent[rows.size()]));
			switchComponent.show(vertical);
		}
	}
	
	private void addFooter(List<IComponent> rows) {
		IComponent footer = formFactory.createFooter();
		if (footer != null) {
			rows.add(footer);
		}
	}
	
	protected void fireChange(int index, T object, Form<T> form) {
		super.fireChange();
	}
	
	protected void updateValues() {
		for (int index = 0; index < object.size(); index++) {
			updateValues(index);
		}
	}

	protected void updateValues(int index) {
		Form<T> rowForm = rowForms.get(index);
		T rowObject = object.get(index);
		rowForm.setObject(rowObject);
	}

	public void setValidationMessages(List<ValidationMessage> validationMessages) {
		@SuppressWarnings("unchecked")
		ArrayList<ValidationMessage>[] validationMessagesByRow = new ArrayList[rowForms.size()];

		for (ValidationMessage message : validationMessages) {
			List<Property> messagePropertyAsChain = ChainedProperty.getChain(message.getProperty());

			Property unchained = ChainedProperty.buildChain(messagePropertyAsChain.subList(propertyAsChain.size() + 1, messagePropertyAsChain.size()));
			IndexProperty indexProperty = (IndexProperty) messagePropertyAsChain.get(propertyAsChain.size());
			int index = indexProperty.getIndex();

			message = new ValidationMessage(unchained, message.getFormattedText());
			if (index >= validationMessagesByRow.length) {
				logger.severe("No row for : " + message);
				continue;
			}
			
			if (validationMessagesByRow[index] == null) {
				validationMessagesByRow[index] = new ArrayList<>();
			}
			validationMessagesByRow[index].add(message);
		}

		for (int i = 0; i < rowForms.size(); i++) {
			if (rowForms.get(i) != null) {
				rowForms.get(i).indicate(validationMessagesByRow[i] != null ? validationMessagesByRow[i] : Collections.emptyList());
			}
		}
	}

}
