package ch.openech.mj.example.model;

import ch.openech.mj.db.model.Constants;
import ch.openech.mj.db.model.annotation.Boolean;
import ch.openech.mj.db.model.annotation.Date;
import ch.openech.mj.db.model.annotation.Decimal;
import ch.openech.mj.db.model.annotation.FormatName;
import ch.openech.mj.db.model.annotation.Int;
import ch.openech.mj.db.model.annotation.Varchar;
import ch.openech.mj.edit.value.Required;


public class Book {
	public static final Book BOOK = Constants.of(Book.class);

	@Required @Varchar(30)
	public String title = "ab";
	public String media = "1";
	@FormatName("baseName")
	public String author = "cd";
	@Boolean
	public String available = "1";
	@Required @Date
	public String date = "2009-01-01";
	@Int(4)
	public String pages = "3";
	@Decimal(size = 6, decimalPlaces = 2)
	public String price = "3";
}
