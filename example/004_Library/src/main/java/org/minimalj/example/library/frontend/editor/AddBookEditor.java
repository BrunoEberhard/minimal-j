package org.minimalj.example.library.frontend.editor;

import org.minimalj.backend.Backend;
import org.minimalj.example.library.frontend.form.BookForm;
import org.minimalj.example.library.frontend.page.BookPage;
import org.minimalj.example.library.model.Book;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.editor.Editor.NewObjectEditor;
import org.minimalj.frontend.form.Form;

public class AddBookEditor extends NewObjectEditor<Book> {

	@Override
	public Form<Book> createForm() {
		return new BookForm(true);
	}
	
	@Override
	public Book save(Book book) {
		return Backend.getInstance().insert(book);
	}

	@Override
	public String getTitle() {
		return "Buch hinzufügen";
	}
	
	@Override
	protected void finished(Book book) {
		Frontend.getBrowser().show(new BookPage(book));
	}

}
