package org.minimalj.example.library.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.minimalj.model.Keys;
import org.minimalj.model.Rendering;
import org.minimalj.model.annotation.Decimal;
import org.minimalj.model.annotation.NotEmpty;
import org.minimalj.model.annotation.Searched;
import org.minimalj.model.annotation.Size;
import org.minimalj.util.mock.Mocking;


public class Book implements Rendering, Mocking {
	public static final Book $ = Keys.of(Book.class);

	public Object id;
	
	@NotEmpty @Size(ExampleFormats.NAME) @Searched
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
	public void mock() {
		title = "The dark tower";
		author = "Stephan King";
		available = true;
		date = LocalDate.of(2009, 1, 1);
		pages = 800;
		price = new BigDecimal(3990).divide(new BigDecimal(100));
	}

	@Override
	public String render(RenderType renderType, Locale locale) {
		return author + ": " + title;
	}
}
