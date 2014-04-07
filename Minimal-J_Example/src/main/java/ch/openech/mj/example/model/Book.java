package ch.openech.mj.example.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import org.joda.time.LocalDate;

import ch.openech.mj.autofill.DemoEnabled;
import ch.openech.mj.example.ExampleFormats;
import ch.openech.mj.model.Keys;
import ch.openech.mj.model.Search;
import ch.openech.mj.model.annotation.Decimal;
import ch.openech.mj.model.annotation.Required;
import ch.openech.mj.model.annotation.Size;


public class Book implements DemoEnabled, Serializable {
	public static final Book BOOK = Keys.of(Book.class);

	public static final Search<Book> BY_FULLTEXT = new Search<>(BOOK.title, BOOK.author);

	public int id;
	
	@Required @Size(ExampleFormats.NAME) 
	public String title;

	@Size(ExampleFormats.NAME)
	public String author;

	public final Set<Media> media = new HashSet<>();
	public Boolean available;
	public LocalDate date;
	@Size(4)
	public Integer pages;
	@Size(6) @Decimal(2)
	public BigDecimal price;
	
	@Override
	public void fillWithDemoData() {
		title = "The dark tower";
//		media = Media.hardcover;
		author = "Stephan King";
		available = true;
		date = new LocalDate(2009, 1, 1);
		pages = 800;
		price = new BigDecimal(3990).divide(new BigDecimal(100));
	}

	@Override
	public String toString() {
		return author + ": " + title;
	}

}
