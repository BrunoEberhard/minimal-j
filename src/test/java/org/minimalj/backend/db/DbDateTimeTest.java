package org.minimalj.backend.db;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class DbDateTimeTest {
	
	private static DbPersistence persistence;
	
	@BeforeClass
	public static void setupDb() throws SQLException {
		persistence = new DbPersistence(DbPersistence.embeddedDataSource(), D.class);
	}
	
	@AfterClass
	public static void shutdownDb() throws SQLException {
	}
	
	@Test
	public void testCrudDates() throws SQLException {
		D d = new D();
		d.localDate = LocalDate.of(2000, 01, 02);
		d.localTime = LocalTime.of(12, 34, 56);
		d.localDateTime = LocalDateTime.of(2001, 02, 03, 10, 20, 30);
		
		Object id = persistence.insert(d);

		//
		
		D d2 = persistence.read(D.class, id);
		Assert.assertEquals(d.localDate, d2.localDate);
		Assert.assertEquals(d.localTime, d2.localTime);
		Assert.assertEquals(d.localDateTime, d2.localDateTime);
	}

}
