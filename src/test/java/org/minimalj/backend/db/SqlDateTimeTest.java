package org.minimalj.backend.db;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.backend.sql.SqlPersistence;

public class SqlDateTimeTest {
	
	private static SqlPersistence persistence;
	
	@BeforeClass
	public static void setupPersistence() {
		persistence = new SqlPersistence(SqlPersistence.embeddedDataSource(), D.class);
	}
	
	@AfterClass
	public static void shutdownPersistence() {
	}
	
	@Test
	public void testCrudDates() {
		D d = new D();
		d.localDate = LocalDate.of(2000, 1, 2);
		d.localTime = LocalTime.of(12, 34, 56);
		d.localDateTime = LocalDateTime.of(2001, 2, 3, 10, 20, 30);
		
		Object id = persistence.insert(d);

		//
		
		D d2 = persistence.read(D.class, id);
		Assert.assertEquals(d.localDate, d2.localDate);
		Assert.assertEquals(d.localTime, d2.localTime);
		Assert.assertEquals(d.localDateTime, d2.localDateTime);
	}

}
