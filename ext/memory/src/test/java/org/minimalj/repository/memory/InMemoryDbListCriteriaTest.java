package org.minimalj.repository.memory;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;
import org.minimalj.model.properties.ChainedProperty;
import org.minimalj.repository.query.By;

public class InMemoryDbListCriteriaTest {

	private InMemoryRepository repository;

	@Before
	public void setupRepository() {
		repository = new InMemoryRepository(TestClassOwner.class, TestClassListTarget.class);
	}

	@Test
	public void testReferenceInList() {
		TestClassListTarget target1 = insertTarget("1");
		TestClassListTarget target2 = insertTarget("2");
		TestClassListTarget target3 = insertTarget("3");

		insertOwner("A", target1, target2);
		insertOwner("B", target2);
		insertOwner("C");

		ChainedProperty key = new ChainedProperty(Keys.getProperty(TestClassOwner.$.items), Keys.getProperty(TestClassItem.$.target));
		Assert.assertEquals(1, repository.count(TestClassOwner.class, By.field(key, target1)));
		Assert.assertEquals(2, repository.count(TestClassOwner.class, By.field(key, target2)));
		Assert.assertEquals(0, repository.count(TestClassOwner.class, By.field(key, target3)));
	}

	@Test
	public void testStringInList() {
		insertOwner("A", null, null);
		ChainedProperty key = new ChainedProperty(Keys.getProperty(TestClassOwner.$.items), Keys.getProperty(TestClassItem.$.text));
		Assert.assertEquals(1, repository.count(TestClassOwner.class, By.field(key, "item")));
		Assert.assertEquals(0, repository.count(TestClassOwner.class, By.field(key, "other")));
	}

	private TestClassListTarget insertTarget(String name) {
		TestClassListTarget target = new TestClassListTarget();
		target.name = name;
		target.id = repository.insert(target);
		return target;
	}

	private void insertOwner(String name, TestClassListTarget... targets) {
		TestClassOwner owner = new TestClassOwner();
		owner.name = name;
		for (TestClassListTarget target : targets) {
			TestClassItem item = new TestClassItem();
			item.target = target;
			item.text = "item";
			owner.items.add(item);
		}
		repository.insert(owner);
	}

	public static class TestClassOwner {
		public static final TestClassOwner $ = Keys.of(TestClassOwner.class);

		public Object id;
		@Size(30)
		public String name;
		public final List<TestClassItem> items = new ArrayList<>();
	}

	public static class TestClassItem {
		public static final TestClassItem $ = Keys.of(TestClassItem.class);

		public TestClassListTarget target;
		@Size(30)
		public String text;
	}

	public static class TestClassListTarget {
		public Object id;
		@Size(30)
		public String name;
	}
}
