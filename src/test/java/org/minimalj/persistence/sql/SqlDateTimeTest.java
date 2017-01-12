package org.minimalj.persistence.sql;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class SqlDateTimeTest {
	
	private static SqlRepository repository;
	
	@BeforeClass
	public static void setupPersistence() {
		repository = new SqlRepository(SqlRepository.embeddedDataSource(), TestEntityDates.class);
	}
	
	@AfterClass
	public static void shutdownPersistence() {
	}
	
	@Test
	public void testCrudDates() {
		TestEntityDates d = new TestEntityDates();
		d.localDate = LocalDate.of(2000, 1, 2);
		d.localTime = LocalTime.of(12, 34, 56);
		d.localDateTime = LocalDateTime.of(2001, 2, 3, 10, 20, 30);
		
		Object id = repository.insert(d);

		//
		
		TestEntityDates d2 = repository.read(TestEntityDates.class, id);
		Assert.assertEquals(d.localDate, d2.localDate);
		Assert.assertEquals(d.localTime, d2.localTime);
		Assert.assertEquals(d.localDateTime, d2.localDateTime);
	}
	
	public static class TestEntityDates {
		public Object id;
		
		public LocalDate localDate;
		public LocalTime localTime;
		public LocalDateTime localDateTime;
		
	}

}
