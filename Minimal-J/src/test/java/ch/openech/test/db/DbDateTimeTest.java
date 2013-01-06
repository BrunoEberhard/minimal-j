package ch.openech.test.db;

import java.sql.SQLException;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.openech.mj.db.DbPersistence;
import ch.openech.mj.db.Table;

public class DbDateTimeTest {
	
	private static DbPersistence persistence;
	private static Table<D> table;
	
	@BeforeClass
	public static void setupDb() throws SQLException {
		persistence = new DbPersistence();
		table = persistence.addClass(D.class);
		persistence.connect();
	}
	
	@AfterClass
	public static void shutdownDb() throws SQLException {
		persistence.commit();
		persistence.disconnect();
	}
	
	@Test
	public void testCrudDates() throws SQLException {
		D d = new D();
		d.localDate = new LocalDate(2000, 01, 02);
		d.localTime = new LocalTime(12, 34, 56);
		d.localDateTime = new LocalDateTime(2001, 02, 03, 10, 20, 30);
		
		int id = table.insert(d);
		persistence.commit();

		//
		
		D d2 = table.read(id);
		Assert.assertEquals("The count of the C's attached to A should match", d.localDate, d2.localDate);
		Assert.assertEquals("The count of the C's attached to A should match", d.localTime, d2.localTime);
		Assert.assertEquals("The count of the C's attached to A should match", d.localDateTime, d2.localDateTime);
	}

}
