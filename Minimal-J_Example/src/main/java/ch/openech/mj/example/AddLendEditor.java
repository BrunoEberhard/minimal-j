package ch.openech.mj.example;

import ch.openech.mj.db.Table;
import ch.openech.mj.edit.Editor;
import ch.openech.mj.edit.form.IForm;
import ch.openech.mj.example.model.Lend;
import ch.openech.mj.page.EmptyPage;

public class AddLendEditor extends Editor<Lend> {

	@Override
	public IForm<Lend> createForm() {
		return new LendForm(true);
	}
	
	@Override
	public String save(Lend lend) throws Exception {
		int id = ((Table<Lend>) ExamplePersistence.getInstance().getTable(Lend.class)).insert(lend);
		return "";
	}

	@Override
	public String getTitle() {
		return "Ausleihe hinzufügen";
	}

}
