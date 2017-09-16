package org.minimalj.repository.ignite;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;
import org.minimalj.repository.ignite.IgniteRepository;
import org.minimalj.repository.query.By;

public class IgniteDbOrderTest {
	
	private static IgniteRepository repository;
	private static List<Object> ids = new ArrayList<>();
	
	@BeforeClass
	public static void setupRepository() {
		repository = new IgniteRepository(TestEntity.class);
		
		ids.add(repository.insert(new TestEntity("a", 1)));
		ids.add(repository.insert(new TestEntity("a", 2)));
		ids.add(repository.insert(new TestEntity("b", 1)));
		ids.add(repository.insert(new TestEntity("b", 2)));
	}
	
	@AfterClass
	public static void shutdownRepository() {
	}
	
	@Test
	public void testOrder() {
		List<TestEntity> entities = repository.find(TestEntity.class, By.range(TestEntity.$.integer, 1, 99).order(TestEntity.$.string, true).order(TestEntity.$.integer, true));
		assertOrder(entities, 0, 1, 2, 3);
		
		entities = repository.find(TestEntity.class, By.range(TestEntity.$.integer, 1, 99).order(TestEntity.$.string, false).order(TestEntity.$.integer, true));
		assertOrder(entities, 2, 3, 0, 1);

		entities = repository.find(TestEntity.class, By.range(TestEntity.$.integer, 1, 99).order(TestEntity.$.string, false).order(TestEntity.$.integer, false));
		assertOrder(entities, 3, 2, 1, 0);

		entities = repository.find(TestEntity.class, By.range(TestEntity.$.integer, 1, 99).order(TestEntity.$.integer, true).order(TestEntity.$.string, true));
		assertOrder(entities, 0, 2, 1, 3);

		entities = repository.find(TestEntity.class, By.range(TestEntity.$.integer, 1, 99).order(TestEntity.$.integer, true).order(TestEntity.$.string, false));
		assertOrder(entities, 2, 0, 3, 1);
	}
	
	private void assertOrder(List<TestEntity> entities, int... order) {
		for (int index = 0; index < order.length; index++) {
			Assert.assertEquals(ids.get(order[index]), entities.get(index).id);
		}
	}

	public static class TestEntity {
		public static final TestEntity $ = Keys.of(TestEntity.class);
		
		public TestEntity() {
			// needed for reflection constructor
		}

		public TestEntity(String string, Integer integer) {
			this.string = string;
			this.integer = integer;
		}

		public Object id;

		@Size(20)
		public String string;
		
		public Integer integer;
	}

}
