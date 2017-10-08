package org.minimalj.repository.ignite;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.repository.ignite.IgniteRepository;

public class IgniteDbDateTimeTest {
	
	private static IgniteRepository repository;
	
	@BeforeClass
	public static void setupRepository() {
		repository = new IgniteRepository(TestEntityDates.class);
	}
	
	@AfterClass
	public static void shutdownRepository() {
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
