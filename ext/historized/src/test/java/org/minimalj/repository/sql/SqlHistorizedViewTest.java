package org.minimalj.repository.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.model.Keys;
import org.minimalj.model.View;
import org.minimalj.model.annotation.Size;
import org.minimalj.repository.DataSourceFactory;
import org.minimalj.repository.query.By;

public class SqlHistorizedViewTest {

	private static SqlHistorizedRepository repository;

	@BeforeClass
	public static void setupRepository() {
		repository = new SqlHistorizedRepository(DataSourceFactory.embeddedDataSource(), TestHistorizedClass.class);
	}

	@Test
	public void testHistorizedReadViewWithLists() {
		TestHistorizedClass test = new TestHistorizedClass();
		
		// test.listWithId.add(new G("withId"));
		test.listWithoutId.add(new B("withoutId"));
		
		Object id = repository.insert(test);
		TestHistorizedView1 view = repository.read(TestHistorizedView1.class, id);
		
		// Assert.assertEquals(1, view.listWithId.size());
		Assert.assertEquals(1, view.listWithoutId.size());
	}

	@Test
	public void testHistorizedFindViewWithLists() {
		TestHistorizedClass test = new TestHistorizedClass();
		test.field = UUID.randomUUID().toString();
		
		// test.listWithId.add(new G("withId"));
		test.listWithoutId.add(new B("withoutId"));
		
		repository.insert(test);
		List<TestHistorizedView1> views = repository.find(TestHistorizedView1.class, By.field(TestHistorizedView1.$.field, test.field));

		Assert.assertEquals(1, views.size());
		TestHistorizedView1 view = views.get(0);
		
		// Assert.assertEquals(1, view.listWithId.size());
		Assert.assertEquals(1, view.listWithoutId.size());
	}
	
	public static class TestHistorizedClass {
		public TestHistorizedClass() {
			// needed for reflection constructor
		}

		public Object id;
		public int version;
		public boolean historized;
		
		@Size(255)
		public String field;

		// references to lists with ids in historized classes not supported
		// public List<G> listWithId = new ArrayList<>();

		public List<B> listWithoutId = new ArrayList<>();
	}
	
	public static class TestHistorizedView1 implements View<TestHistorizedClass> {
		public static final TestHistorizedClass $ = Keys.of(TestHistorizedClass.class);
		
		public Object id;
		public int version;
		public boolean historized;

		@Size(255)
		public String field;
		
		// public List<G> listWithId = new ArrayList<>();

		public List<B> listWithoutId = new ArrayList<>();
	}
	
	
}
