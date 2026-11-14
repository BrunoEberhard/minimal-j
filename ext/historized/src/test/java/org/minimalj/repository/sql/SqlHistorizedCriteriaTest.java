package org.minimalj.repository.sql;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;
import org.minimalj.model.properties.ChainedProperty;
import org.minimalj.repository.DataSourceFactory;
import org.minimalj.repository.query.By;

/**
 * By.field must only find the current version of a historized entity.
 */
public class SqlHistorizedCriteriaTest {

	private SqlHistorizedRepository repository;

	@Before
	public void setupRepository() {
		repository = new SqlHistorizedRepository(DataSourceFactory.embeddedDataSource(), TestHistorizedOwner.class, TestHistorizedTarget.class);
	}

	@Test
	public void testFieldAfterUpdate() {
		TestHistorizedOwner owner = new TestHistorizedOwner();
		owner.name = "A";
		Object id = repository.insert(owner);

		owner = repository.read(TestHistorizedOwner.class, id);
		owner.name = "B";
		repository.update(owner);

		Assert.assertEquals(0, repository.count(TestHistorizedOwner.class, By.field(TestHistorizedOwner.$.name, "A")));
		Assert.assertEquals(1, repository.count(TestHistorizedOwner.class, By.field(TestHistorizedOwner.$.name, "B")));
		Assert.assertEquals(1, repository.find(TestHistorizedOwner.class, By.field(TestHistorizedOwner.$.name, "B")).size());
	}

	@Test
	public void testListAfterUpdate() {
		TestHistorizedTarget target1 = insertTarget("1");
		TestHistorizedTarget target2 = insertTarget("2");

		TestHistorizedOwner owner = new TestHistorizedOwner();
		owner.name = "A";
		TestHistorizedItem item = new TestHistorizedItem();
		item.target = target1;
		owner.items.add(item);
		Object id = repository.insert(owner);

		ChainedProperty key = new ChainedProperty(Keys.getProperty(TestHistorizedOwner.$.items), Keys.getProperty(TestHistorizedItem.$.target));
		Assert.assertEquals(1, repository.count(TestHistorizedOwner.class, By.field(key, target1)));
		Assert.assertEquals(0, repository.count(TestHistorizedOwner.class, By.field(key, target2)));

		owner = repository.read(TestHistorizedOwner.class, id);
		owner.items.get(0).target = target2;
		repository.update(owner);

		Assert.assertEquals(0, repository.count(TestHistorizedOwner.class, By.field(key, target1)));
		Assert.assertEquals(1, repository.count(TestHistorizedOwner.class, By.field(key, target2)));
	}

	private TestHistorizedTarget insertTarget(String name) {
		TestHistorizedTarget target = new TestHistorizedTarget();
		target.name = name;
		target.id = repository.insert(target);
		return target;
	}

	public static class TestHistorizedOwner {
		public static final TestHistorizedOwner $ = Keys.of(TestHistorizedOwner.class);

		public Object id;
		public int version;
		public boolean historized;
		@Size(30)
		public String name;
		public final List<TestHistorizedItem> items = new ArrayList<>();
	}

	public static class TestHistorizedItem {
		public static final TestHistorizedItem $ = Keys.of(TestHistorizedItem.class);

		public TestHistorizedTarget target;
	}

	public static class TestHistorizedTarget {
		public Object id;
		@Size(30)
		public String name;
	}
}
