package org.minimalj.frontend.form.element;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.minimalj.backend.Backend;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.Search;
import org.minimalj.frontend.Frontend.TableActionListener;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.editor.Editor.NewObjectEditor;
import org.minimalj.frontend.editor.SearchDialog;
import org.minimalj.frontend.editor.TableDialog;
import org.minimalj.frontend.form.Form;
import org.minimalj.model.annotation.NotEmpty;
import org.minimalj.model.annotation.Searched;
import org.minimalj.model.properties.Properties;
import org.minimalj.model.properties.Property;
import org.minimalj.repository.query.By;
import org.minimalj.repository.sql.EmptyObjects;
import org.minimalj.util.CloneHelper;
import org.minimalj.util.StringUtils;
import org.minimalj.util.resources.Resources;

public class LookupFormElement<T> extends AbstractLookupFormElement<T> implements Search<T> {
	protected final Class<T> fieldClazz;
	protected Object[] columns;
	private Form<?> newForm;
	private Class<?> formClazz;
	private Function<Object, T> formResultConverter;
	private TableDialog<T> dialog;

	public LookupFormElement(T key) {
		this(key, (Object[]) null);
	}

	@SuppressWarnings("unchecked")
	public LookupFormElement(T key, Object... columns) {
		super(key, true);
		this.columns = columns;
		fieldClazz = (Class<T>) getProperty().getClazz();
		this.columns = columns != null && columns.length > 0 ? columns : getDefaultColumns(fieldClazz).toArray();
		if (this.columns.length == 0) {
			throw new IllegalArgumentException(getProperty().getPath() + ": no columns defined");
		}
	}

	protected List<Property> getDefaultColumns(Class<T> clazz) {
		return Properties.getProperties(getProperty().getClazz()).values(). //
				stream().filter(p -> !StringUtils.equals(p.getName(), "id", "version", "historized")).collect(Collectors.toList());
	}

	@Override
	protected void lookup() {
		List<Action> additionalActions = createAdditionalActions();
		additionalActions = additionalActions.stream().filter(Action::isEnabled).collect(Collectors.toList());
		dialog = createDialog(additionalActions, new SearchDialogActionListener());
		Frontend.showDialog(dialog);
	}
	
	protected TableDialog<T> createDialog(List<Action> additionalActions, TableActionListener<T> listener) {
		if (searchable()) {
			return new SearchDialog<>(this, getTitle(), columns, false, listener, additionalActions);
		} else {
			return new TableDialog<>(this, getTitle(), columns, false, listener, additionalActions);
		}
	}
		
	protected String getTitle() {
		return Resources.getString(SearchDialog.class);
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

	public LookupFormElement<T> newForm(Form<T> form) {
		this.newForm = form;
		this.formClazz = fieldClazz;
		return this;
	}
	
	public LookupFormElement<T> newForm(Class formClazz, Form form, Function<Object, T> formResultConvert) {
		this.newForm = form;
		this.formClazz = formClazz;
		return this;
	}

	private class ClearAction extends Action {
		@Override
		public void run() {
			setValue(null);
		}
	}

	protected class NewReferenceEditor extends NewObjectEditor {

		@Override
		protected Object createObject() {
			return LookupFormElement.this.createObject();
		}
		
		@Override
		protected Class<?> getEditedClass() {
			return formClazz;
		}

		@Override
		protected Form createForm() {
			return newForm;
		}

		@Override
		protected Object save(Object object) {
			return Backend.save(object);
		}

		@Override
		protected void finished(Object result) {
			T converted = (T) formResultConverter.apply(result);
			LookupFormElement.this.setValueInternal(converted);
			Frontend.closeDialog(dialog);
		}
	}
	
	protected Object createObject() {
		return CloneHelper.newInstance(formClazz);
	}

	private class SearchDialogActionListener implements TableActionListener<T> {
		@Override
		public void action(T selectedObject) {
			LookupFormElement.this.setValueInternal(selectedObject);
			Frontend.closeDialog(dialog);
		}
	}
	
	protected boolean searchable() {
		return Properties.getProperties(fieldClazz).values().stream().anyMatch(p -> p.getAnnotation(Searched.class) != null);	
	}
	
	@Override
	public List<T> search(String searchText) {
		if (searchable()) {
			return Backend.find(fieldClazz, By.search(searchText));
		} else {
			return Backend.find(fieldClazz, By.all());
		}
	}

}