package org.minimalj.repository.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.minimalj.model.Keys;
import org.minimalj.model.View;
import org.minimalj.model.annotation.Size;
import org.minimalj.repository.query.By;

public class SqlViewTest extends SqlTest {

	@Override
	public Class<?>[] getEntityClasses() {
		return new Class<?>[] { TestClass.class };
	}

	@Test
	public void testReadViewWithLists() {
		TestClass test = new TestClass();
		
		test.listWithId.add(new G("withId"));
		test.listWithoutId.add(new B("withoutId"));
		
		Object id = repository.insert(test);
		TestView1 view = repository.read(TestView1.class, id);
		
		Assert.assertEquals(1, view.listWithId.size());
		Assert.assertEquals(1, view.listWithoutId.size());
	}

	@Test
	public void testFindViewWithLists() {
		TestClass test = new TestClass();
		test.field = UUID.randomUUID().toString();
		
		test.listWithId.add(new G("withId"));
		test.listWithoutId.add(new B("withoutId"));
		
		repository.insert(test);
		List<TestView1> views = repository.find(TestView1.class, By.field(TestView1.$.field, test.field));

		Assert.assertEquals(1, views.size());
		TestView1 view = views.get(0);
		
		Assert.assertEquals(1, view.listWithId.size());
		Assert.assertEquals(1, view.listWithoutId.size());
	}
	
	public static class TestClass {
		public TestClass() {
			// needed for reflection constructor
		}

		public Object id;
		
		@Size(255)
		public String field;

		public List<G> listWithId = new ArrayList<>();

		public List<B> listWithoutId = new ArrayList<>();
	}
	
	public static class TestView1 implements View<TestClass> {
		public static final TestView1 $ = Keys.of(TestView1.class);
		
		public Object id;

		@Size(255)
		public String field;
		
		public List<G> listWithId = new ArrayList<>();

		public List<B> listWithoutId = new ArrayList<>();
	}
	
}
