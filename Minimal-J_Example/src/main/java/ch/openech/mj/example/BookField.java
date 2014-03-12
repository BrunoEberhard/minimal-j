package ch.openech.mj.example;

import ch.openech.mj.edit.SearchDialogAction;
import ch.openech.mj.edit.fields.ObjectFlowField;
import ch.openech.mj.edit.form.IForm;
import ch.openech.mj.example.model.Book;
import ch.openech.mj.model.Keys;
import ch.openech.mj.model.Reference;

public class BookField extends ObjectFlowField<Reference<Book>> {

	public BookField(Reference<Book> key) {
		super(Keys.getProperty(key));
	}
	
	@Override
	public IForm<Reference<Book>> createFormPanel() {
		// not used
		return null;
	}

	@Override
	protected void show(Reference<Book> book) {
		addText((String) book.get(Book.BOOK.title));
	}

	@Override
	protected void showActions() {
        addAction(new BookSearchAction());
        addAction(new RemoveObjectAction());
	}
	
	public class BookSearchAction extends SearchDialogAction<Book> {
		
		public BookSearchAction() {
			super(getComponent(), Book.BY_FULLTEXT, new Object[]{Book.BOOK.title, Book.BOOK.author});
		}

		protected void save(Book book) {
			Reference<Book> reference = getObject();
			reference.set(book);
		}

	}
}
