package org.minimalj.frontend.form.element;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.minimalj.backend.Backend;
import org.minimalj.frontend.Frontend.Search;
import org.minimalj.frontend.Frontend.TableActionListener;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.editor.Editor.NewObjectEditor;
import org.minimalj.frontend.editor.SearchDialog;
import org.minimalj.frontend.form.Form;
import org.minimalj.model.annotation.NotEmpty;
import org.minimalj.model.properties.Properties;
import org.minimalj.repository.query.By;
import org.minimalj.repository.sql.EmptyObjects;

public class ReferenceFormElement<T> extends AbstractLookupFormElement<T> implements Search<T> {
	private final Class<T> fieldClazz;
	private Object[] columns;
	private Form<T> newForm;
	private SearchDialog<T> dialog;

	public ReferenceFormElement(T key) {
		this(key, (Object[]) null);
	}

	@SuppressWarnings("unchecked")
	public ReferenceFormElement(T key, Object... columns) {
		super(key, true);
		this.columns = columns;
		this.columns = columns != null && columns.length > 0 ? columns : Properties.getProperties(getProperty().getClazz()).values().toArray();
		fieldClazz = (Class<T>) getProperty().getClazz();
	}

	@Override
	protected void lookup() {
		List<Action> additionalActions = createAdditionalActions();
		additionalActions = additionalActions.stream().filter(Action::isEnabled).collect(Collectors.toList());
		dialog = new SearchDialog<>(this, columns, false, new SearchDialogActionListener(), additionalActions);
		dialog.show();
	}

	protected List<Action> createAdditionalActions() {
		List<Action> additionalActions = new ArrayList<>();
		if (newForm != null) {
			additionalActions.add(new NewReferenceEditor());
		}
		boolean required = getProperty().getAnnotation(NotEmpty.class) != null;
		if (!required && !EmptyObjects.isEmpty(getValue())) {
			additionalActions.add(new ClearAction());
		}
		return additionalActions;
	}

	public ReferenceFormElement<T> newForm(Form<T> form) {
		this.newForm = form;
		return this;
	}

	private class ClearAction extends Action {
		@Override
		public void action() {
			setValue(null);
		}
	}

	protected class NewReferenceEditor extends NewObjectEditor<T> {

		@Override
		protected Class<T> getEditedClass() {
			return fieldClazz;
		}

		@Override
		protected Form<T> createForm() {
			return newForm;
		}

		@Override
		protected T save(T object) {
			return Backend.save(object);
		}

		@Override
		protected void finished(T result) {
			ReferenceFormElement.this.setValueInternal(result);
			dialog.closeDialog();
		}
	}

	private class SearchDialogActionListener implements TableActionListener<T> {
		@Override
		public void action(T selectedObject) {
			ReferenceFormElement.this.setValueInternal(selectedObject);
			dialog.closeDialog();
		}
	}

	@Override
	public List<T> search(String searchText) {
		return Backend.find(fieldClazz, By.search(searchText));
	}
}
