package org.minimalj.frontend.impl.util;

import java.text.MessageFormat;
import java.util.List;
import java.util.function.Consumer;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.SwitchComponent;
import org.minimalj.frontend.editor.Editor.SimpleEditor;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.form.element.AbstractFormElement;
import org.minimalj.frontend.impl.util.ColumnFilterEditor.ColumnFilterModel;
import org.minimalj.model.Keys;
import org.minimalj.model.Selection;
import org.minimalj.model.annotation.NotEmpty;
import org.minimalj.model.annotation.Size;
import org.minimalj.model.validation.Validation;
import org.minimalj.model.validation.ValidationMessage;
import org.minimalj.util.ChangeListener;
import org.minimalj.util.resources.Resources;

public class ColumnFilterEditor extends SimpleEditor<ColumnFilterModel> {

	private final String propertyName;
	private final String filterString;
	private final List<ColumnFilterPredicate> columnFilters;
	private final ColumnFilterPredicate columnFilter;
	private final Consumer<String> finishedListener;

	public ColumnFilterEditor(String propertyName, String filterString, List<ColumnFilterPredicate> columnFilters, ColumnFilterPredicate columnFilter, Consumer<String> finishedListener) {
		this.propertyName = propertyName;
		this.filterString = filterString;
		this.columnFilters = columnFilters;
		this.columnFilter = columnFilter != null ? columnFilter : columnFilters.get(0);
		this.finishedListener = finishedListener;
	}
	
	@Override
	public String getTitle() {
		return MessageFormat.format(Resources.getString(ColumnFilterEditor.class), propertyName);
	}

	public static class ColumnFilterModel implements Validation {
		public static final ColumnFilterModel $ = Keys.of(ColumnFilterModel.class);

		@NotEmpty
		public Selection<ColumnFilterPredicate> filter;

		@Size(10240)
		public String filterString;

		@Override
		public List<ValidationMessage> validate() {
			if (filter.selectedValue != null && !filter.selectedValue.valid()) {
				return List.of(Validation.createInvalidValidationMessage($.filterString));
			} else {
				return null;
			}
		}
	}

	@Override
	protected ColumnFilterModel createObject() {
		ColumnFilterModel model = new ColumnFilterModel();
		model.filter = new Selection<>(columnFilter, columnFilters);
		model.filterString = this.filterString;
		return model;
	}

	@Override
	protected Form<ColumnFilterModel> createForm() {
		Form<ColumnFilterModel> form = new Form<>();
		
		form.line(Keys.getProperty(ColumnFilterModel.$.filter));

		ColumnFilterFormElement columnFilterFormElement = new ColumnFilterFormElement(ColumnFilterModel.$.filterString, getObject().filter.selectedValue);
		form.line(columnFilterFormElement);

		form.addDependecy(ColumnFilterModel.$.filter, (filter, model) -> columnFilterFormElement.setColumnFilter(filter.selectedValue), ColumnFilterModel.$.filterString);
		return form;
	}

	@Override
	protected ColumnFilterModel save(ColumnFilterModel filter) {
		finishedListener.accept(filter.filterString);
		return filter;
	}

	public static class ColumnFilterFormElement extends AbstractFormElement<String> implements ChangeListener<String> {

		private final SwitchComponent switchComponent;
		private ColumnFilterPredicate columnFilter;
		private IComponent formElement;
		private String text;

		public ColumnFilterFormElement(String key, ColumnFilterPredicate columnFilter) {
			super(key);

			switchComponent = Frontend.getInstance().createSwitchComponent();
			setColumnFilter(columnFilter);
		}

//		@Override
//		public String getCaption() {
//			return null;
//		}

		@Override
		public String getValue() {
			return text;
		}

		@Override
		public void setValue(String value) {
			this.text = value;
			if (columnFilter != null) {
				columnFilter.setFilterString(value);
			}
		}

		@Override
		public IComponent getComponent() {
			return switchComponent;
		}

		public String setColumnFilter(ColumnFilterPredicate columnFilter) {
			ColumnFilterPredicate oldFilter = this.columnFilter;
			this.columnFilter = columnFilter;
			if (this.columnFilter != null) {
				if (oldFilter != null) {
					this.columnFilter.copyFrom(oldFilter);
				}
				this.columnFilter.setChangeListener(this);
				formElement = this.columnFilter.getComponent();
				switchComponent.show(formElement);
				return text;
			} else {
				formElement = null;
				switchComponent.show(null);
				return null;
			}
		}
		
		@Override
		public void changed(String value) {
			this.text = value;
			fireChange();
		}
	}

}
