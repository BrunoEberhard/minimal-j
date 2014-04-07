package ch.openech.mj.example.page;

import static ch.openech.mj.example.model.Lend.*;

import java.util.List;

import ch.openech.mj.example.model.Lend;
import ch.openech.mj.page.ActionGroup;
import ch.openech.mj.page.PageContext;
import ch.openech.mj.page.RefreshablePage;
import ch.openech.mj.page.TablePage;


public class LendTablePage extends TablePage<Lend> implements RefreshablePage {

	private final String text;
	
	public static final Object[] FIELDS = {
		LEND.book.title, //
		LEND.book.author, //
		LEND.till
	};
	
	public LendTablePage(PageContext context, String text) {
		super(context, FIELDS, text);
		this.text = text;
	}

	@Override
	public String getTitle() {
		return "Ausleihen für " + text;
	}

	@Override
	public ActionGroup getMenu() {
		return null;
	}

	@Override
	protected List<Lend> load(String query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void clicked(Lend selectedObject, List<Lend> selectedObjects) {
		// TODO
		// show(BookPage.class, selectedObject.book.);
	}
	
}
