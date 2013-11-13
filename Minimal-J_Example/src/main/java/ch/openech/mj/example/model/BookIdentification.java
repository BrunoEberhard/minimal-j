package ch.openech.mj.example.model;

import ch.openech.mj.example.ExampleFormats;
import ch.openech.mj.model.Keys;
import ch.openech.mj.model.annotation.Required;
import ch.openech.mj.model.annotation.Size;


public class BookIdentification {
	public static final BookIdentification BOOK_IDENTIFICATION = Keys.of(BookIdentification.class);

	@Required @Size(ExampleFormats.NAME) 
	public String title;

	@Size(ExampleFormats.NAME)
	public String author;

	public String display(int columns, int rows) {
		return author + ": " + title;
	}
	
}
