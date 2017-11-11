package org.minimalj.ubersetzung.frontend;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.editor.Editor.NewObjectEditor;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.page.TablePage;
import org.minimalj.ubersetzung.model.Ubersetzung;
import org.minimalj.ubersetzung.model.UbersetzungEntry;

public class UbersetzungEntryTablePage extends TablePage<UbersetzungEntry> {

	private Ubersetzung ubersetzung;
	private NewUbersetzungEntryAction newAction;
	private DeleteUbersetzungEntryAction deleteAction;
	
	public UbersetzungEntryTablePage(Ubersetzung ubersetzung) {
		super(new Object[] {UbersetzungEntry.$.key, UbersetzungEntry.$.value});
		this.ubersetzung = ubersetzung;
		newAction = new NewUbersetzungEntryAction(ubersetzung);
		deleteAction = new DeleteUbersetzungEntryAction(ubersetzung);
	}
	
	@Override
	public List<Action> getActions() {
		List<Action> actions = new ArrayList<>();
		actions.add(newAction);
		return actions;
	}
	
	@Override
	protected List<UbersetzungEntry> load() {
		return ubersetzung.entries;
	}

	public void setUbersetzung(Ubersetzung mainObject) {
		this.ubersetzung = mainObject;
		newAction.setUbersetzung(mainObject);
		refresh();
	}

	public class NewUbersetzungEntryAction extends NewObjectEditor<UbersetzungEntry> {

		public Ubersetzung ubersetzung;
		
		public NewUbersetzungEntryAction(Ubersetzung ubersetzung) {
			setUbersetzung(ubersetzung);
		}
		
		@Override
		protected Form<UbersetzungEntry> createForm() {
			Form<UbersetzungEntry> form = new Form<>();
			form.text(ubersetzung.lang + " / " + ubersetzung.country);
			form.line(UbersetzungEntry.$.key);
			form.line(UbersetzungEntry.$.value);
			return form;
		}

		@Override
		protected UbersetzungEntry save(UbersetzungEntry object) {
			ubersetzung.entries.add(object);
			Backend.save(ubersetzung);
			refresh();
			return object;
		}

		public void setUbersetzung(Ubersetzung ubersetzung) {
			this.ubersetzung = ubersetzung;
			setEnabled(ubersetzung != null);
		}
	}
	
	public class DeleteUbersetzungEntryAction extends TableSelectionAction {

		private List<UbersetzungEntry> selection;
		
		public DeleteUbersetzungEntryAction(Ubersetzung ubersetzung) {
			setUbersetzung(ubersetzung);
		}

		@Override
		public void selectionChanged(List<UbersetzungEntry> selection) {
			this.selection = selection;
			setEnabled(!selection.isEmpty());
		}
		
		@Override
		public void action() {
			ubersetzung.entries.removeAll(selection);
			Backend.save(ubersetzung);
			refresh();
		}
	}
}
