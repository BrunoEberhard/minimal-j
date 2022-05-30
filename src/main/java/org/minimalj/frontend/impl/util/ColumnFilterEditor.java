package org.minimalj.frontend.impl.util;

import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.FormContent;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.InputComponentListener;
import org.minimalj.frontend.Frontend.SwitchContent;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.page.Page.Dialog;
import org.minimalj.util.ChangeListener;
import org.minimalj.util.resources.Resources;

public class ColumnFilterEditor extends Action implements Dialog {

	private final List<ColumnFilterPredicate> columnFilters;
	private final Consumer<String> finishedListener;
	
	private final String title;
	private final SaveAction saveAction = new SaveAction();
	private final CancelAction cancelAction = new CancelAction();

	private Input<ColumnFilterPredicate> filterComboBox;
	private ColumnFilterPredicate predicate;
	private String filterString;
	private Input<String> textField;
	
	private SwitchContent switchContent;
	private FormContent formContent;

	public ColumnFilterEditor(String propertyName, Input<String> textField, List<ColumnFilterPredicate> columnFilters, Consumer<String> finishedListener) {
		this.textField = textField;
		this.columnFilters = Objects.requireNonNull(columnFilters);
		this.finishedListener = Objects.requireNonNull(finishedListener);
		this.title = MessageFormat.format(Resources.getString(ColumnFilterEditor.class), propertyName);
	}
	
	@Override
	public void run() {
		FilterPredicateListener filterPredicateListener = new FilterPredicateListener();
		columnFilters.forEach(filter -> filter.setChangeListener(filterPredicateListener));
		
		switchContent = Frontend.getInstance().createSwitchContent();
		filterComboBox = Frontend.getInstance().createComboBox(columnFilters, new FilterComboBoxListener());

		initializeFilter();
		
		Frontend.showDialog(this);
	}
	
	@Override
	public String getTitle() {
		return title;
	}
	
	@Override
	public IContent getContent() {
		return switchContent;
	}
	
	@Override
	public List<Action> getActions() {
		return List.of(cancelAction, saveAction);
	}
	
	public SaveAction getSaveAction() {
		return saveAction;
	}
	
	@Override
	public Action getCancelAction() {
		return cancelAction;
	}
	
	private class FilterComboBoxListener implements InputComponentListener {

		@Override
		public void changed(IComponent source) {
			ColumnFilterPredicate previousPredicate = predicate;
			predicate = filterComboBox.getValue();
			if (predicate != null) {
				predicate.copyFrom(previousPredicate);
			}
			updateForm();
		}
	}
	
	private class FilterPredicateListener implements ChangeListener<String> {
		@Override
		public void changed(String filterString) {
			ColumnFilterEditor.this.filterString = filterString;
			saveAction.setEnabled(predicate != null && predicate.valid());
		}
	}
	
	protected final class SaveAction extends Action {
		@Override
		public void run() {
			finishedListener.accept(filterString);
			Frontend.closeDialog(ColumnFilterEditor.this);
		}
	}
	
	private class CancelAction extends Action {
		@Override
		public void run() {
			Frontend.closeDialog(ColumnFilterEditor.this);
		}
	}
	
	private void initializeFilter() {
		predicate = columnFilters.get(0);
		for (int i = columnFilters.size() - 1; i >= 0; i--) {
			predicate = columnFilters.get(i);
			if (predicate.isFilterStringValid(textField.getValue())) {
				break;
			}
		}
		predicate.setFilterString(textField.getValue());
		filterComboBox.setValue(predicate);
		updateForm();
	}

	private void updateForm() {
		formContent = Frontend.getInstance().createFormContent(2, 100);
		formContent.add(Resources.getString("ColumnFilterModel.filter"), false, filterComboBox, null, 2); // TODO i18n
		if (predicate != null) {
			predicate.fillForm(formContent);
		}
		
		switchContent.show(formContent);
	}

}
