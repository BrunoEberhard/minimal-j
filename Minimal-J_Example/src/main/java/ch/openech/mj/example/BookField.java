package ch.openech.mj.example;

import ch.openech.mj.edit.SearchDialogAction;
import ch.openech.mj.edit.fields.ObjectFlowField;
import ch.openech.mj.edit.form.IForm;
import ch.openech.mj.example.model.Book;
import ch.openech.mj.model.Keys;
import ch.openech.mj.model.PropertyInterface;
import ch.openech.mj.search.FulltextIndexSearch;

public class BookField extends ObjectFlowField<Book> {

	public BookField(PropertyInterface property) {
		super(property);
	}
	
	public BookField(Book key) {
		this(Keys.getProperty(key));
	}
	
	@Override
	public IForm<Book> createFormPanel() {
		// not used
		return null;
	}

	@Override
	protected void show(Book book) {
		addText(book.title);
	}

	@Override
	protected void showActions() {
        addAction(new BookSearchAction());
        addAction(new RemoveObjectAction());
	}
	
	public class BookSearchAction extends SearchDialogAction<Book> {
		
		public BookSearchAction() {
			super(getComponent(), new FulltextIndexSearch<>(Book.class, ExamplePersistence.getInstance().bookIndex(), Book.BOOK.title, Book.BOOK.author));
		}

		protected void save(Book object) {
			setObject(object);
		}

	}
}
