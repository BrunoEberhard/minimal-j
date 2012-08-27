package ch.openech.mj.example.model;

import static ch.openech.mj.db.model.annotation.PredefinedFormat.Boolean;
import static ch.openech.mj.db.model.annotation.PredefinedFormat.Date;
import static ch.openech.mj.db.model.annotation.PredefinedFormat.Decimal6_2;
import static ch.openech.mj.db.model.annotation.PredefinedFormat.Int4;
import static ch.openech.mj.db.model.annotation.PredefinedFormat.String30;
import ch.openech.mj.db.model.Constants;
import ch.openech.mj.db.model.annotation.Is;
import ch.openech.mj.edit.value.Required;


public class Book {
	public static final Book BOOK = Constants.of(Book.class);

	@Is(String30) @Required 
	public String title = "ab";
	public String media = "1";
	@Is("baseName")
	public String author = "cd";
	@Is(Boolean)
	public String available = "1";
	@Is(Date)
	public String date = "2009-01-01";
	@Is(Int4)
	public String pages = "3";
	@Is(Decimal6_2)
	public String price = "3";
}
