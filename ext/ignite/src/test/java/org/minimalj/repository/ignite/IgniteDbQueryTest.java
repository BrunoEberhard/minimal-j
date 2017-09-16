package org.minimalj.repository.ignite;

import org.apache.ignite.internal.processors.cache.CacheEntryImpl;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.repository.DataSourceFactory;

public class IgniteDbQueryTest {

	private static IgniteRepository repository;

	@BeforeClass
	public static void setupRepository() {
		repository = new IgniteRepository(G.class);
	}

	@AfterClass
	public static void shutdownRepository() {
	}

	@Test
	public void testQuery() {
		G g = new G("testName1");
		repository.insert(g);

		g = repository.execute(G.class, "g = 'testName1'");
		Assert.assertNotNull(g);

		g = repository.execute(G.class,
				repository.name(G.$.g) + " LIKE '%est%'");
		Assert.assertNotNull(g);

		g = repository.execute(G.class,
				$(G.$.g) + " LIKE '%est%'");
		Assert.assertNotNull(g);
	}
	
	private String $(Object classOrKey) {
		return repository.name(classOrKey);
	}
	
}
