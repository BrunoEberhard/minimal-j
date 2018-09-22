package org.minimalj.frontend.form.element;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.Search;
import org.minimalj.frontend.Frontend.TableActionListener;
import org.minimalj.frontend.page.IDialog;
import org.minimalj.repository.query.By;

// To make this class generic is a little bit senseless as
// there are no checks at all
public class ReferenceFormElement<T> extends AbstractLookupFormElement<T> {
	private final Class<T> fieldClazz;
	private final Object[] searchColumns;
	private IDialog dialog;

	public ReferenceFormElement(T key, Object... searchColumns) {
		this(key, false, searchColumns);
	}

	@SuppressWarnings("unchecked")
	public ReferenceFormElement(T key, boolean textEditable, Object... searchColumns) {
		super(key, textEditable, true);
		fieldClazz = (Class<T>) getProperty().getClazz();
		this.searchColumns = searchColumns;
	}

	@Override
	protected void lookup() {
		dialog = Frontend.showSearchDialog(new ReferenceFieldSearch(), searchColumns, new SearchDialogActionListener());
	}

	@Override
	protected T parse(String text) {
		return null;
	}

	private class SearchDialogActionListener implements TableActionListener<T> {
		@Override
		public void action(T selectedObject) {
			ReferenceFormElement.this.setValueInternal(selectedObject);
			dialog.closeDialog();
		}
	}

	private class ReferenceFieldSearch implements Search<T> {

		@Override
		public List<T> search(String searchText) {
			return (List<T>) Backend.find(fieldClazz, By.search(searchText, searchColumns));
		}
	}
}
