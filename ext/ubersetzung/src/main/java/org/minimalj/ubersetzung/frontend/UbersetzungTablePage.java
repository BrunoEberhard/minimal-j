package org.minimalj.ubersetzung.frontend;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.editor.Editor.NewObjectEditor;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.page.TablePage.TablePageWithDetail;
import org.minimalj.repository.query.By;
import org.minimalj.ubersetzung.model.Ubersetzung;
import org.minimalj.ubersetzung.model.UbersetzungView;

public class UbersetzungTablePage extends TablePageWithDetail<UbersetzungView, UbersetzungEntryTablePage> {

	public UbersetzungTablePage() {
		super(new Object[] {UbersetzungView.$.lang, UbersetzungView.$.country});
	}

	@Override
	protected List<UbersetzungView> load() {
		return Backend.find(UbersetzungView.class, By.ALL);
	}

	@Override
	protected UbersetzungEntryTablePage createDetailPage(UbersetzungView viewObject) {
		Ubersetzung ubersetzung = Backend.read(Ubersetzung.class, viewObject.id);
		return new UbersetzungEntryTablePage(ubersetzung);
	}

	@Override
	protected UbersetzungEntryTablePage updateDetailPage(UbersetzungEntryTablePage page, UbersetzungView viewObject) {
		Ubersetzung ubersetzung = Backend.read(Ubersetzung.class, viewObject.id);
		page.setUbersetzung(ubersetzung);
		return page;
	}
	
	@Override
	public List<Action> getActions() {
		List<Action> actions = new ArrayList<>();
		actions.add(new NewUbersetzungAction());
		return actions;
	}

	public class NewUbersetzungAction extends NewObjectEditor<Ubersetzung> {

		@Override
		protected Form<Ubersetzung> createForm() {
			Form<Ubersetzung> form = new Form<>();
			form.line(Ubersetzung.$.lang);
			form.line(Ubersetzung.$.country);
			return form;
		}

		@Override
		protected Ubersetzung save(Ubersetzung object) {
			object = Backend.save(object);
			refresh();
			return object;
		}
	}

}
