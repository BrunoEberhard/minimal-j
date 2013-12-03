package ch.openech.mj.example.page;

import static ch.openech.mj.example.model.Book.*;

import java.util.List;

import ch.openech.mj.example.MjExampleApplication;
import ch.openech.mj.example.model.Book;
import ch.openech.mj.page.ActionGroup;
import ch.openech.mj.page.PageContext;
import ch.openech.mj.page.RefreshablePage;
import ch.openech.mj.page.TablePage;
import ch.openech.mj.search.IndexSearch;


public class BookTablePage extends TablePage<Book> implements RefreshablePage {

	private final String text;
	
	public static final Object[] FIELDS = {
		BOOK.bookIdentification.title, //
		BOOK.bookIdentification.author, //
		BOOK.date, //
		BOOK.media, //
		BOOK.pages, //
		BOOK.available, //
	};
	
	public BookTablePage(PageContext context, String text) {
		super(context, new IndexSearch<>(MjExampleApplication.persistence().bookIndex), FIELDS, text);
		this.text = text;
	}
	
	@Override
	protected void clicked(int selectedId, List<Integer> books) {
		show(BookPage.class, Integer.toString(selectedId));
	}
	@Override
	public String getTitle() {
		return "Treffer für " + text;
	}

	@Override
	public ActionGroup getMenu() {
		return null;
	}
	
}
