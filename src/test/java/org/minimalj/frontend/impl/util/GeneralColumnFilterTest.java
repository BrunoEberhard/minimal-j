package org.minimalj.frontend.impl.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Locale;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.application.Application;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.Input;
import org.minimalj.frontend.impl.json.JsonFrontend;
import org.minimalj.model.Keys;
import org.minimalj.repository.DataSourceFactory;
import org.minimalj.repository.Repository;
import org.minimalj.repository.sql.SqlRepository;
import org.minimalj.test.TestUtil;
import org.minimalj.util.LocaleContext;

public class GeneralColumnFilterTest {

	private ColumnFilter filter;
	private Repository repository;
	
	@BeforeClass
	public static void setLocale() {
		LocaleContext.setLocale(() -> Locale.GERMAN);
	}

	@Before
	public void initRepository() {
		repository = new SqlRepository(DataSourceFactory.embeddedDataSource(), GeneralColumnFilterTestEntity.class);
		Application.setInstance(new Application() {});
		Frontend.setInstance(new JsonFrontend());
	}
	
	@After
	public void after() {
		TestUtil.shutdown();
	}
	
	private void assertFilter(ColumnFilter filter, GeneralColumnFilterTestEntity entity, boolean result) {
		Assert.assertEquals(result, filter.test(entity));
		entity.id = repository.insert(entity);
		long count = repository.count(GeneralColumnFilterTestEntity.class, filter.getCriteria());
		Assert.assertEquals(result ? 1 : 0, count);
		repository.delete(entity);
	}

	@Test
	public void testParseDate() {
		GeneralColumnFilterTestEntity entity = new GeneralColumnFilterTestEntity();

		filter = ColumnFilter.createFilter(Keys.getProperty(GeneralColumnFilterTestEntity.$.date));
		Input<String> textField = (Input<String>) filter.getComponent(source -> {});
		
		textField.setValue("1.2.2003");
		
		entity.date = LocalDate.of(2003, 2, 1);
		assertFilter(filter, entity, true);
		
		entity.date = LocalDate.of(2003, 2, 2);
		assertFilter(filter, entity, false);

		entity.date = LocalDate.of(2003, 1, 31);
		assertFilter(filter, entity, false);
		
		//
		
		textField.setValue("-1.2.2003");

		entity.date = LocalDate.of(2003, 2, 1);
		assertFilter(filter, entity, true);

		entity.date = LocalDate.of(2003, 2, 2);
		assertFilter(filter, entity, false);

		entity.date = LocalDate.of(2003, 1, 31);
		assertFilter(filter, entity, true);

		//
		
		textField.setValue("1.2.2003-");

		entity.date = LocalDate.of(2003, 2, 1);
		assertFilter(filter, entity, true);

		entity.date = LocalDate.of(2003, 2, 2);
		assertFilter(filter, entity, true);

		entity.date = LocalDate.of(2003, 1, 31);
		assertFilter(filter, entity, false);

		//
		
		textField.setValue("1.2.2003-3.2.2003");

		entity.date = LocalDate.of(2003, 1, 31);
		assertFilter(filter, entity, false);

		entity.date = LocalDate.of(2003, 2, 1);
		assertFilter(filter, entity, true);

		entity.date = LocalDate.of(2003, 2, 3);
		assertFilter(filter, entity, true);

		entity.date = LocalDate.of(2003, 2, 4);
		assertFilter(filter, entity, false);
	}
	
	@Test
	public void testParseDateTime() {
		GeneralColumnFilterTestEntity entity = new GeneralColumnFilterTestEntity();

		filter = ColumnFilter.createFilter(Keys.getProperty(GeneralColumnFilterTestEntity.$.dateTime));
		Input<String> textField = (Input<String>) filter.getComponent(source -> {});
		textField.setValue("1.2.2003");
		
		entity.dateTime = LocalDateTime.of(2003, 2, 1, 4, 56);
		assertFilter(filter, entity, true);

		entity.dateTime = LocalDateTime.of(2003, 2, 1, 0, 0);
		assertFilter(filter, entity, true);

		entity.dateTime = LocalDateTime.of(2003, 2, 1, 23, 59, 59, 999);
		assertFilter(filter, entity, true);

		entity.dateTime = LocalDateTime.of(2003, 2, 2, 4, 56);
		assertFilter(filter, entity, false);

		entity.dateTime = LocalDateTime.of(2003, 1, 31, 4, 56);
		assertFilter(filter, entity, false);

		//

		textField.setValue("-1.2.2003");
		
		entity.dateTime = LocalDateTime.of(2003, 2, 1, 4, 56);
		assertFilter(filter, entity, true);

		entity.dateTime = LocalDateTime.of(2003, 2, 1, 0, 0);
		assertFilter(filter, entity, true);

		entity.dateTime = LocalDateTime.of(2003, 2, 1, 23, 59, 59, 999);
		assertFilter(filter, entity, true);

		entity.dateTime = LocalDateTime.of(2003, 2, 2, 4, 56);
		assertFilter(filter, entity, false);

		entity.dateTime = LocalDateTime.of(2003, 1, 31, 4, 56);
		assertFilter(filter, entity, true);

		//

		textField.setValue("1.2.2003-");
		
		entity.dateTime = LocalDateTime.of(2003, 2, 1, 4, 56);
		assertFilter(filter, entity, true);

		entity.dateTime = LocalDateTime.of(2003, 2, 1, 0, 0);
		assertFilter(filter, entity, true);

		entity.dateTime = LocalDateTime.of(2003, 2, 1, 23, 59, 59, 999);
		assertFilter(filter, entity, true);

		entity.dateTime = LocalDateTime.of(2003, 2, 2, 4, 56);
		assertFilter(filter, entity, true);

		entity.dateTime = LocalDateTime.of(2003, 1, 31, 4, 56);
		assertFilter(filter, entity, false);

		//

		textField.setValue("1.2.2003 04:56");
		
		entity.dateTime = LocalDateTime.of(2003, 2, 1, 4, 56);
		assertFilter(filter, entity, true);

		entity.dateTime = LocalDateTime.of(2003, 2, 1, 4, 57);
		assertFilter(filter, entity, false);

		entity.dateTime = LocalDateTime.of(2003, 2, 1, 4, 55);
		assertFilter(filter, entity, false);

		//
		
		textField.setValue("1.2.2003 04:56-");
		
		entity.dateTime = LocalDateTime.of(2003, 2, 1, 4, 56);
		assertFilter(filter, entity, true);

		entity.dateTime = LocalDateTime.of(2003, 2, 1, 4, 57);
		assertFilter(filter, entity, true);

		entity.dateTime = LocalDateTime.of(2003, 2, 1, 4, 55);
		assertFilter(filter, entity, false);

		//
		
		textField.setValue("-1.2.2003 04:56");
		
		entity.dateTime = LocalDateTime.of(2003, 2, 1, 4, 56);
		assertFilter(filter, entity, true);

		entity.dateTime = LocalDateTime.of(2003, 2, 1, 4, 57);
		assertFilter(filter, entity, false);

		entity.dateTime = LocalDateTime.of(2003, 2, 1, 4, 55);
		assertFilter(filter, entity, true);

		//
		
		textField.setValue("1.2.2003 04:55-1.2.2003 04:56");
		
		entity.dateTime = LocalDateTime.of(2003, 2, 1, 4, 56);
		assertFilter(filter, entity, true);

		entity.dateTime = LocalDateTime.of(2003, 2, 1, 4, 57);
		assertFilter(filter, entity, false);

		entity.dateTime = LocalDateTime.of(2003, 2, 1, 4, 55);
		assertFilter(filter, entity, true);

		entity.dateTime = LocalDateTime.of(2003, 2, 1, 4, 54);
		assertFilter(filter, entity, false);

		//
		
		textField.setValue("2");

		entity.dateTime = LocalDateTime.of(2003, 2, 1, 4, 56);
		assertFilter(filter, entity, true);
	}
	

	public static class GeneralColumnFilterTestEntity {
		public static final GeneralColumnFilterTestEntity $ = Keys.of(GeneralColumnFilterTestEntity.class);

		public Object id;
		
		public LocalDate date;

		public LocalTime time;

		public LocalDateTime dateTime;
	}

}
