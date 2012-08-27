package ch.openech.mj.db.model.annotation;

import java.math.BigDecimal;

import ch.openech.mj.db.model.BooleanFormat;
import ch.openech.mj.db.model.DateFormat;
import ch.openech.mj.db.model.Formats;
import ch.openech.mj.db.model.NumberFormat;
import ch.openech.mj.db.model.PlainFormat;

public class PredefinedFormat {

	public static final String Date = "Date";
	public static final String DatePartially = "DatePartially";

	public static final String String2 = "String2";
	public static final String String30 = "String30";
	public static final String String36 = "String36";
	public static final String String255 = "String255";
	
	public static final String Int2 = "Int2";
	public static final String Int3 = "Int3";
	public static final String Int4 = "Int4";
	public static final String Int9 = "Int9";
	public static final String Int10 = "Int10";
	public static final String Int12 = "Int12";

	public static final String Decimal6_2 = "Decimal6_2";

	
	public static final String Boolean = "Boolean";

	static {
		Formats.getInstance().register(Date, new DateFormat(false));
		Formats.getInstance().register(DatePartially, new DateFormat(true));

		Formats.getInstance().register(String2, new PlainFormat(2));
		Formats.getInstance().register(String30, new PlainFormat(30));
		Formats.getInstance().register(String36, new PlainFormat(36));
		Formats.getInstance().register(String255, new PlainFormat(255));
		
		Formats.getInstance().register(Int2, new NumberFormat(Integer.class, 2, false));
		Formats.getInstance().register(Int3, new NumberFormat(Integer.class, 3, false));
		Formats.getInstance().register(Int4, new NumberFormat(Integer.class, 4, false));
		Formats.getInstance().register(Int9, new NumberFormat(Integer.class, 9, false));
		Formats.getInstance().register(Int10, new NumberFormat(Integer.class, 10, false));
		Formats.getInstance().register(Int12, new NumberFormat(Integer.class, 12, false));

		Formats.getInstance().register(Boolean, new BooleanFormat(false));

		Formats.getInstance().register(Decimal6_2, new NumberFormat(BigDecimal.class, 6, 2, false));
	}

	public static void initialize() {
		//
	}
	
}
