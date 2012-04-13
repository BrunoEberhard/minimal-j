package ch.openech.mj.example;

import java.sql.SQLException;
import java.util.List;

import ch.openech.mj.edit.Editor;
import ch.openech.mj.edit.form.FormVisual;
import ch.openech.mj.edit.validation.ValidationMessage;
import ch.openech.mj.example.model.Book;

public class AddBookEditor extends Editor<Book> {

	@Override
	public Book newInstance() {
		return new Book();
	}

	@Override
	public void validate(Book object, List<ValidationMessage> resultList) {
		// nothing to validate
	}

	@Override
	public FormVisual<Book> createForm() {
		return new BookForm(true);
	}
	
	@Override
	public boolean save(Book book) {
		try {
			ExamplePersistence.getInstance().book().insert(book);
			return true;
		} catch (SQLException e) {
			throw new RuntimeException("Buch konnte nicht gespeichert werden", e);
		}
	}

	@Override
	public String getTitle() {
		return "Buch hinzufügen";
	}

}
