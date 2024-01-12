package org.minimalj.repository.sql;

import org.junit.Assert;
import org.junit.Test;

public class SqlQueryTest extends SqlTest {

	@Override
	public Class<?>[] getEntityClasses() {
		return new Class<?>[] { G.class };
	}

	@Test
	public void testQuery() {
		G g = new G("testName1");
		repository.insert(g);

		g = repository.execute(G.class, "SELECT * FROM G WHERE g LIKE '%N%'");
		Assert.assertNotNull(g);

		g = repository.execute(G.class, "SELECT * FROM " + repository.name(G.class) + " WHERE g LIKE '%am%'");
		Assert.assertNotNull(g);

		g = repository.execute(G.class,
				"SELECT * FROM " + repository.name(G.class) + " WHERE " + repository.name(G.$.g) + " LIKE '%est%'");
		Assert.assertNotNull(g);

		g = repository.execute(G.class,
				"SELECT * FROM " + $(G.class) + " WHERE " + $(G.$.g) + " LIKE '%est%'");
		Assert.assertNotNull(g);
	}

	@Test
	public void testQueryOnDependable() {
		G g = new G("testName2");
		g.k = new K();
		g.k.k = "test";
		repository.insert(g);

		Long count = repository.execute(Long.class,
				"SELECT count(*) FROM " + repository.name(G.$.k) + " WHERE " + repository.name(K.$.k) + " LIKE '%est%'");
		Assert.assertNotNull(count);
		Assert.assertEquals(1, count.intValue());
	}

	@Test
	public void testQueryOnDependableList() {
		G g = new G("testName2");
		K k = new K();
		k.k = "test";
		g.kList.add(k);
		repository.insert(g);

		Long count = repository.execute(Long.class,
				"SELECT count(*) FROM " + repository.name(G.$.kList) + " WHERE " + repository.name(K.$.k) + " LIKE '%est%'");
		Assert.assertNotNull(count);
		Assert.assertEquals(1, count.intValue());
	}

	
	private String $(Object classOrKey) {
		return repository.name(classOrKey);
	}
	
}
