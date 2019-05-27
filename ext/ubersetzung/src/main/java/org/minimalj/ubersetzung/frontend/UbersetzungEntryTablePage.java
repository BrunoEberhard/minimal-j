package org.minimalj.ubersetzung.frontend;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.form.element.TextFormElement;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.page.SimpleTableEditorPage;
import org.minimalj.ubersetzung.model.Ubersetzung;
import org.minimalj.ubersetzung.model.UbersetzungEntry;

public class UbersetzungEntryTablePage extends SimpleTableEditorPage<UbersetzungEntry> {

	private Ubersetzung ubersetzung;

	public UbersetzungEntryTablePage(Ubersetzung ubersetzung) {
		this.ubersetzung = ubersetzung;
	}

	@Override
	protected Object[] getColumns() {
		return new Object[] { UbersetzungEntry.$.key, UbersetzungEntry.$.value };
	}

	@Override
	protected List<UbersetzungEntry> load() {
		return ubersetzung.entries;
	}

	@Override
	protected UbersetzungEntry save(UbersetzungEntry object) {
		ubersetzung.entries.add(object);
		Backend.save(ubersetzung);
		return object;
	}

	@Override
	protected void delete(List<UbersetzungEntry> selectedObjects) {
		ubersetzung.entries.removeAll(selectedObjects);
		Backend.save(ubersetzung);
	}

	public void setUbersetzung(Ubersetzung mainObject) {
		this.ubersetzung = mainObject;
		refresh();
	}

	@Override
	protected Page getDetailPage(UbersetzungEntry view) {
		// UbersetzungEntry has no id, cannot be displayed in ObjectPage
		return null;
	}

	@Override
	protected Form<UbersetzungEntry> createForm(boolean editable, boolean newObject) {
		Form<UbersetzungEntry> form = new Form<>(editable);
		form.line(ubersetzung.lang + " / " + ubersetzung.country);

		if (newObject) {
			form.line(UbersetzungEntry.$.key);
		} else {
			form.line(new TextFormElement(UbersetzungEntry.$.key));
		}

		form.line(UbersetzungEntry.$.value);
		return form;
	}

}
