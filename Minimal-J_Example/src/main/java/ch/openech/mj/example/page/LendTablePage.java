package ch.openech.mj.example.page;

import static ch.openech.mj.example.model.Lend.*;

import java.util.List;

import ch.openech.mj.example.MjExampleApplication;
import ch.openech.mj.example.model.Lend;
import ch.openech.mj.page.ActionGroup;
import ch.openech.mj.page.PageContext;
import ch.openech.mj.page.RefreshablePage;
import ch.openech.mj.page.TablePage;
import ch.openech.mj.search.IndexSearch;


public class LendTablePage extends TablePage<Lend> implements RefreshablePage {

	private final String text;
	
	public static final Object[] FIELDS = {
		LEND.book.title, //
		LEND.book.author, //
		LEND.till
	};
	
	public LendTablePage(PageContext context, String text) {
		super(context, new IndexSearch<>(MjExampleApplication.persistence().lendByCustomerIndex), FIELDS, text);
		this.text = text;
	}
	
	@Override
	protected void clicked(int selectedId, List<Integer> books) {
		show(BookPage.class, Integer.toString(selectedId));
	}
	@Override
	public String getTitle() {
		return "Ausleihen für " + text;
	}

	@Override
	public ActionGroup getMenu() {
		return null;
	}
	
}
