package ch.openech.mj.example.page;

import static ch.openech.mj.example.model.Book.*;

import java.util.List;

import ch.openech.mj.example.ExamplePersistence;
import ch.openech.mj.example.model.Book;
import ch.openech.mj.page.ActionGroup;
import ch.openech.mj.page.PageContext;
import ch.openech.mj.page.RefreshablePage;
import ch.openech.mj.page.TablePage;
import ch.openech.mj.search.FulltextIndexSearch;
import ch.openech.mj.search.Item;
import ch.openech.mj.search.Search;


public class BookTablePage extends TablePage<Book> implements RefreshablePage {

	private final String text;
	
	private static final Object[] FIELDS = {
		BOOK.title, //
		BOOK.author, //
		BOOK.date, //
		BOOK.media, //
		BOOK.pages, //
		BOOK.available, //
	};
	
	private static Search<Book> search = new FulltextIndexSearch<>(Book.class, ExamplePersistence.getInstance().bookIndex(), FIELDS);

	public BookTablePage(PageContext context, String text) {
		super(context, search, text);
		this.text = text;
	}
	
	@Override
	protected void clicked(Item item, List<Item> items) {
		show(BookPage.class, (String) item.getId());
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
