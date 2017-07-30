package org.minimalj.repository.sql;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.repository.DataSourceFactory;

public class SqlReadWithStringIdTest {
	
	private static SqlRepository repository;
	
	@BeforeClass
	public static void setupRepository() {
		repository = new SqlRepository(DataSourceFactory.embeddedDataSource(), G.class);
	}
	
	@AfterClass
	public static void shutdownRepository() {
	}
	
	@Test
	public void testInsertAndDelete() {
		G g = new G("testName1");
		Object id = repository.insert(g);
		
		G g2 = repository.read(G.class, id.toString());
		Assert.assertNotNull(g2);
	}

}
