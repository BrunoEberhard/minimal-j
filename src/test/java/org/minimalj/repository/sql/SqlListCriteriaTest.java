package org.minimalj.repository.sql;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;
import org.minimalj.model.properties.ChainedProperty;
import org.minimalj.repository.query.By;

public class SqlListCriteriaTest extends SqlTest {

	@Override
	public Class<?>[] getEntityClasses() {
		return new Class<?>[] { ListOwner.class, ListTarget.class };
	}

	@Test
	public void testReferenceInList() {
		ListTarget target1 = new ListTarget("1");
		target1.id = repository.insert(target1);
		ListTarget target2 = new ListTarget("2");
		target2.id = repository.insert(target2);
		ListTarget target3 = new ListTarget("3");
		target3.id = repository.insert(target3);

		insertOwner("A", target1, target2);
		insertOwner("B", target2);
		insertOwner("C");

		ChainedProperty key = new ChainedProperty(Keys.getProperty(ListOwner.$.items), Keys.getProperty(ListItem.$.target));
		Assert.assertEquals(1, repository.count(ListOwner.class, By.field(key, target1)));
		Assert.assertEquals(2, repository.count(ListOwner.class, By.field(key, target2)));
		Assert.assertEquals(0, repository.count(ListOwner.class, By.field(key, target3)));
	}

	@Test
	public void testStringInList() {
		insertOwner("A", null, null);
		ChainedProperty key = new ChainedProperty(Keys.getProperty(ListOwner.$.items), Keys.getProperty(ListItem.$.text));
		Assert.assertEquals(1, repository.count(ListOwner.class, By.field(key, "item")));
		Assert.assertEquals(0, repository.count(ListOwner.class, By.field(key, "other")));
	}

	private void insertOwner(String name, ListTarget... targets) {
		ListOwner owner = new ListOwner();
		owner.name = name;
		for (ListTarget target : targets) {
			ListItem item = new ListItem();
			item.target = target;
			item.text = "item";
			owner.items.add(item);
		}
		repository.insert(owner);
	}

	public static class ListOwner {
		public static final ListOwner $ = Keys.of(ListOwner.class);

		public Object id;
		@Size(30)
		public String name;
		public final List<ListItem> items = new ArrayList<>();
	}

	public static class ListItem {
		public static final ListItem $ = Keys.of(ListItem.class);

		public ListTarget target;
		@Size(30)
		public String text;
	}

	public static class ListTarget {
		public Object id;
		@Size(30)
		public String name;

		public ListTarget() {
			//
		}

		public ListTarget(String name) {
			this.name = name;
		}
	}
}
