package ch.openech.mj.example;

import ch.openech.mj.edit.SearchDialogAction;
import ch.openech.mj.edit.fields.ObjectFlowField;
import ch.openech.mj.edit.form.IForm;
import ch.openech.mj.example.model.Book;
import ch.openech.mj.example.model.BookIdentification;
import ch.openech.mj.model.Keys;
import ch.openech.mj.model.PropertyInterface;
import ch.openech.mj.search.IndexSearch;

public class BookField extends ObjectFlowField<BookIdentification> {

	public BookField(PropertyInterface property) {
		super(property);
	}
	
	public BookField(BookIdentification key) {
		this(Keys.getProperty(key));
	}
	
	@Override
	public IForm<BookIdentification> createFormPanel() {
		// not used
		return null;
	}

	@Override
	protected void show(BookIdentification book) {
		addText(book.title);
	}

	@Override
	protected void showActions() {
        addAction(new BookSearchAction());
        addAction(new RemoveObjectAction());
	}
	
	public class BookSearchAction extends SearchDialogAction<Book> {
		
		public BookSearchAction() {
			super(getComponent(), new IndexSearch<>(ExamplePersistence.getInstance().bookIndex), new Object[]{Book.BOOK.bookIdentification.title, Book.BOOK.bookIdentification.author});
		}

		protected void save(Book object) {
			setObject(object.bookIdentification);
		}

	}
}
