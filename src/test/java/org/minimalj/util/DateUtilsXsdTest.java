package org.minimalj.util;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import junit.framework.TestCase;

public class DateUtilsXsdTest  extends TestCase {
    private Calendar c;

    @Override
    public void setUp() {
        c = Calendar.getInstance(TimeZone.getTimeZone("Europe/Zurich"));
        c.set(Calendar.YEAR, 2042);
        c.set(Calendar.MONTH, 11); 
        c.set(Calendar.DAY_OF_MONTH, 6);
        c.set(Calendar.HOUR_OF_DAY, 21);
        c.set(Calendar.MINUTE, 30);
        c.set(Calendar.SECOND, 15);
        c.set(Calendar.MILLISECOND, 0);
    }

    public void testParse() throws ParseException {
        Date d = DateUtils.parseXsd("2042-12-06T21:30:15+01:00");
        assertEquals(c.getTime(), d);
    }

    public void testFormat() throws ParseException {
        String s = DateUtils.formatXsd(c.getTime());
        assertEquals("2042-12-06T21:30:15+01:00", s);
    }

}