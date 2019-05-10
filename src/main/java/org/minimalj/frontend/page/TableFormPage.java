package org.minimalj.frontend.page;

import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;

import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.Frontend.TableActionListener;
import org.minimalj.frontend.form.Form;
import org.minimalj.util.resources.Resources;

public abstract class TableFormPage<T> extends TableDetailPage<T> {

	public TableFormPage() {
		super();
	}

	@Override
	protected Page getDetailPage(T detail) {
		return new DetailPage(detail);
	}

	protected abstract Form<T> createForm();

	/*
	 * This DetailPage does not reload the detail object. This is good because the
	 * details could also be depending entities. Depending entities don't have an id
	 * an cannot be loaded separately.
	 */
	public class DetailPage extends Page implements TableActionListener<T> {

		private final Form<T> form;

		public DetailPage(T object) {
			form = Objects.requireNonNull(TableFormPage.this.createForm());
			form.setObject(Objects.requireNonNull(object));
		}

		@Override
		public String getTitle() {
			String title = Resources.getStringOrNull(getClass());
			if (title != null) {
				return title;
			} else {
				return MessageFormat.format(Resources.getString(DetailPage.class.getSimpleName() + ".title"), TableFormPage.this.getNameArguments());
			}
		}

		@Override
		public IContent getContent() {
			return form.getContent();
		}

		@Override
		public void action(T selectedObject) {
			form.setObject(selectedObject);
		}

		@Override
		public void selectionChanged(List<T> selectedObjects) {
			if (selectedObjects != null && !selectedObjects.isEmpty()) {
				action(selectedObjects.get(selectedObjects.size() - 1));
			}
		}
	}

}
