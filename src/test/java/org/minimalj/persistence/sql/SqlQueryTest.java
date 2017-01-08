package org.minimalj.persistence.sql;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class SqlQueryTest {

	private static SqlPersistence persistence;

	@BeforeClass
	public static void setupPersistence() {
		persistence = new SqlPersistence(SqlPersistence.embeddedDataSource(), G.class);
	}

	@AfterClass
	public static void shutdownPersistence() {
	}

	@Test
	public void testQuery() {
		G g = new G("testName1");
		persistence.insert(g);

		g = persistence.execute(G.class, "SELECT * FROM G WHERE g LIKE '%N%'");
		Assert.assertNotNull(g);

		g = persistence.execute(G.class, "SELECT * FROM " + persistence.name(G.class) + " WHERE g LIKE '%am%'");
		Assert.assertNotNull(g);

		g = persistence.execute(G.class,
				"SELECT * FROM " + persistence.name(G.class) + " WHERE " + persistence.name(G.$.g) + " LIKE '%est%'");
		Assert.assertNotNull(g);

		g = persistence.execute(G.class,
				"SELECT * FROM " + $(G.class) + " WHERE " + $(G.$.g) + " LIKE '%est%'");
		Assert.assertNotNull(g);
	}
	
	private String $(Object classOrKey) {
		return persistence.name(classOrKey);
	}
	
}
