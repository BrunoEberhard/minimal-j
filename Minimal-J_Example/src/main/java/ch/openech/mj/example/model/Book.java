package ch.openech.mj.example.model;

import java.math.BigDecimal;

import org.joda.time.LocalDate;

import ch.openech.mj.db.model.Constants;
import ch.openech.mj.edit.value.Required;
import ch.openech.mj.example.ExampleFormats;
import ch.openech.mj.model.annotation.Decimal;
import ch.openech.mj.model.annotation.Size;


public class Book {
	public static final Book BOOK = Constants.of(Book.class);

	@Required @Size(ExampleFormats.NAME) 
	public String title = "ab";
	public Media media = Media.hardcover;
	@Size(ExampleFormats.NAME)
	public String author = "cd";
	public Boolean available = Boolean.TRUE;
	public LocalDate date = new LocalDate(2009, 1, 1);
	@Size(4)
	public Integer pages = 3;
	@Size(6) @Decimal(2)
	public BigDecimal price;
}
