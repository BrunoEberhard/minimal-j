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
import org.minimalj.frontend.form.Form;
import org.minimalj.model.annotation.NotEmpty;
import org.minimalj.model.properties.Properties;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.repository.query.By;
import org.minimalj.repository.sql.EmptyObjects;
import org.minimalj.util.CloneHelper;
import org.minimalj.util.StringUtils;

public class ReferenceFormElement<T> extends AbstractLookupFormElement<T> implements Search<T> {
	private final Class<T> fieldClazz;
	private Object[] columns;
	private Form<?> newForm;
	private Class<?> formClazz;
	private Function<Object, T> formResultConverter;
	private SearchDialog<T> dialog;

	public ReferenceFormElement(T key) {
		this(key, (Object[]) null);
	}

	@SuppressWarnings("unchecked")
	public ReferenceFormElement(T key, Object... columns) {
		super(key, true);
		this.columns = columns;
		fieldClazz = (Class<T>) getProperty().getClazz();
		this.columns = columns != null && columns.length > 0 ? columns : getDefaultColumns(fieldClazz).toArray();
		if (this.columns.length == 0) {
			throw new IllegalArgumentException(getProperty().getPath() + ": no columns defined");
		}
	}

	protected List<PropertyInterface> getDefaultColumns(Class<T> clazz) {
		return Properties.getProperties(getProperty().getClazz()).values(). //
				stream().filter(p -> !StringUtils.equals(p.getName(), "id", "version", "historized")).collect(Collectors.toList());
	}

	@Override
	protected void lookup() {
		List<Action> additionalActions = createAdditionalActions();
		additionalActions = additionalActions.stream().filter(Action::isEnabled).collect(Collectors.toList());
		dialog = new SearchDialog<>(this, columns, false, new SearchDialogActionListener(), additionalActions);
		Frontend.showDialog(dialog);
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
		this.formClazz = fieldClazz;
		return this;
	}
	
	public ReferenceFormElement<T> newForm(Class formClazz, Form form, Function<Object, T> formResultConvert) {
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
			return ReferenceFormElement.this.createObject();
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
			ReferenceFormElement.this.setValueInternal(converted);
			Frontend.closeDialog(dialog);
		}
	}
	
	protected Object createObject() {
		return CloneHelper.newInstance(formClazz);
	}

	private class SearchDialogActionListener implements TableActionListener<T> {
		@Override
		public void action(T selectedObject) {
			ReferenceFormElement.this.setValueInternal(selectedObject);
			Frontend.closeDialog(dialog);
		}
	}

	@Override
	public List<T> search(String searchText) {
		return Backend.find(fieldClazz, By.search(searchText));
	}
}
