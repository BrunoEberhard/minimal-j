package org.minimalj.ubersetzung.frontend;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.page.TableEditorPage;
import org.minimalj.repository.query.By;
import org.minimalj.ubersetzung.model.Ubersetzung;
import org.minimalj.ubersetzung.model.UbersetzungView;

public class UbersetzungTablePage extends TableEditorPage<UbersetzungView, Ubersetzung> {

	@Override
	protected Object[] getColumns() {
		return new Object[] { UbersetzungView.$.lang, UbersetzungView.$.country };
	}

	@Override
	protected List<UbersetzungView> load() {
		return Backend.find(UbersetzungView.class, By.ALL);
	}

	@Override
	protected UbersetzungEntryTablePage getDetailPage(UbersetzungView viewObject) {
		Ubersetzung ubersetzung = Backend.read(Ubersetzung.class, viewObject.id);
		return new UbersetzungEntryTablePage(ubersetzung);
	}

	@Override
	protected Form<Ubersetzung> createForm(boolean editable, boolean newObject) {
		Form<Ubersetzung> form = new Form<>(editable);
		form.line(Ubersetzung.$.lang);
		form.line(Ubersetzung.$.country);
		return form;
	}

}
