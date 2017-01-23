package org.minimalj.repository.sql;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.repository.DataSourceFactory;

public class SqlQueryTest {

	private static SqlRepository repository;

	@BeforeClass
	public static void setupRepository() {
		repository = new SqlRepository(DataSourceFactory.embeddedDataSource(), G.class);
	}

	@AfterClass
	public static void shutdownRepository() {
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
