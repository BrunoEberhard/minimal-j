package ch.openech.mj.example;

import static ch.openech.mj.example.model.Book.BOOK;

import java.util.List;

import ch.openech.mj.example.model.Book;
import ch.openech.mj.page.Page;
import ch.openech.mj.page.RefreshablePage;
import ch.openech.mj.toolkit.ClientToolkit;
import ch.openech.mj.toolkit.VisualTable;
import ch.openech.mj.toolkit.VisualTable.ClickListener;


public class BookTablePage extends Page implements RefreshablePage {

	private String text;
	private VisualTable<Book> table;

	private static final String[] FIELD_NAMES = {
		BOOK.title, //
		BOOK.author, //
		BOOK.date, //
		BOOK.media, //
		BOOK.pages, //
		BOOK.available, //
	};

	public BookTablePage(String text) {
		super();
		this.text = text;
		table = ClientToolkit.getToolkit().createVisualTable(Book.class, FIELD_NAMES);
		table.setClickListener(new BookTableClickListener());
		refresh();
	}
	
	@Override
	public Object getPanel() {
		return table;
	}

	private class BookTableClickListener implements ClickListener {

		@Override
		public void clicked() {
			Book book = table.getSelectedObject();
			if (book != null) {
				show(BookViewPage.class, String.valueOf(book.id));
			}
		}
	}
	
	@Override
	public void refresh() {
		try {
			List<Book> result = ExamplePersistence.getInstance().book().find(text);
			table.setObjects(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
