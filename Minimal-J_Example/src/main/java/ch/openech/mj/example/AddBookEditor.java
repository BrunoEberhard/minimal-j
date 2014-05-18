package ch.openech.mj.example;

import ch.openech.mj.backend.Backend;
import ch.openech.mj.edit.Editor;
import ch.openech.mj.edit.form.IForm;
import ch.openech.mj.example.model.Book;
import ch.openech.mj.example.page.BookPage;
import ch.openech.mj.page.PageLink;

public class AddBookEditor extends Editor<Book> {

	@Override
	public IForm<Book> createForm() {
		return new BookForm(true);
	}
	
	@Override
	public String save(Book book) throws Exception {
		long id = Backend.getInstance().insert(book);
		return PageLink.link(BookPage.class, id);
	}

	@Override
	public String getTitle() {
		return "Buch hinzufügen";
	}

}
