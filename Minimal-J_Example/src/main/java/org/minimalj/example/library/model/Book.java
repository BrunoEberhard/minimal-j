package org.minimalj.example.library.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import org.joda.time.LocalDate;
import org.minimalj.example.library.example.ExampleFormats;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Decimal;
import org.minimalj.model.annotation.Required;
import org.minimalj.model.annotation.Searched;
import org.minimalj.model.annotation.Size;
import org.minimalj.util.DemoEnabled;


public class Book implements DemoEnabled, Serializable {
	public static final Book BOOK = Keys.of(Book.class);

	public int id;
	
	@Required @Size(ExampleFormats.NAME) @Searched
	public String title;

	@Size(ExampleFormats.NAME) @Searched
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
