import java.util.Locale;

import junit.framework.Assert;

import org.joda.time.DateTimeFieldType;
import org.joda.time.LocalDate;
import org.joda.time.Partial;
import org.joda.time.ReadablePartial;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Test;


public class LocalDateExampleTest {

	@Test
	public void testParseAndFormatISO() {
		LocalDate date = ISODateTimeFormat.date().parseLocalDate("2012-11-03");
		Assert.assertEquals(2012, date.getYear());
		Assert.assertEquals(11, date.getMonthOfYear());
		Assert.assertEquals(3, date.getDayOfMonth());
		String dateString = ISODateTimeFormat.date().print(date);
		Assert.assertEquals("2012-11-03", dateString);
	}
	
	@Test
	public void testParseAndFormatLocale() {
		Locale.setDefault(Locale.GERMAN);
		LocalDate date = DateTimeFormat.shortDate().parseLocalDate("3.11.12");
		Assert.assertEquals(2012, date.getYear());
		Assert.assertEquals(11, date.getMonthOfYear());
		Assert.assertEquals(3, date.getDayOfMonth());
		String dateString = DateTimeFormat.mediumDate().print(date);
		Assert.assertEquals("03.11.2012", dateString);

		dateString = DateTimeFormat.shortDate().print(date);
		Assert.assertEquals("03.11.12", dateString);
	}
	
	@Test
	public void testParseAndFormatPartial() {
		Locale.setDefault(Locale.GERMAN);
		ReadablePartial p = new Partial(DateTimeFieldType.year(), 2012);
		Assert.assertEquals(2012, p.get(DateTimeFieldType.year()));
		Assert.assertFalse(p.isSupported(DateTimeFieldType.monthOfYear()));
	}

}
