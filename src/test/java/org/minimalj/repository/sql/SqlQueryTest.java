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
	
	private String $(Object classOrKey) {
		return repository.name(classOrKey);
	}
	
}
