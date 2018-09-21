package org.minimalj.frontend.form.element;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.Frontend.Search;
import org.minimalj.frontend.Frontend.TableActionListener;
import org.minimalj.frontend.page.IDialog;
import org.minimalj.model.Keys;
import org.minimalj.model.Rendering;
import org.minimalj.repository.query.By;

// To make this class generic is a little bit senseless as
// there are no checks at all
public class ReferenceFormElement<T extends Rendering> extends AbstractFormElement<T> {
	private final Class<T> fieldClazz;
	private final Object[] searchColumns;
	protected final Input<T> lookup;
	private IDialog dialog;
	
	@SuppressWarnings("unchecked")
	public ReferenceFormElement(Object key, Object... searchColumns) {
		super(Keys.getProperty(key));
		fieldClazz = (Class<T>) getProperty().getClazz();
		this.searchColumns = searchColumns;
		lookup = Frontend.getInstance().createLookup(this::showSearchDialog, listener());
	}

	private void showSearchDialog() {
		dialog = Frontend.showSearchDialog(new ReferenceFieldSearch(), searchColumns, new SearchDialogActionListener());
	}

	private class SearchDialogActionListener implements TableActionListener<T> {
		@Override
		public void action(T selectedObject) {
			lookup.setValue(selectedObject);
			dialog.closeDialog();
		}
	}

	private class ReferenceFieldSearch implements Search<T> {

		@Override
		public List<T> search(String searchText) {
			return (List<T>) Backend.find(fieldClazz, By.search(searchText, searchColumns));
		}
	}
	
	@Override
	public IComponent getComponent() {
		return lookup;
	}

	@Override
	public T getValue() {
		return lookup.getValue();
	}

	@Override
	public void setValue(T object) {
		lookup.setValue(object);
	}
}
