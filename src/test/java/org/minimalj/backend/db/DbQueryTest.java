package org.minimalj.backend.db;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class DbQueryTest {

	private static DbPersistence persistence;

	@BeforeClass
	public static void setupDb() {
		persistence = new DbPersistence(DbPersistence.embeddedDataSource(), A.class, G.class, H.class, M.class);
	}

	@AfterClass
	public static void shutdownDb() {
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
