package org.minimalj.repository.sql;

import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.repository.DataSourceFactory;
import org.minimalj.repository.criteria.By;
import org.minimalj.repository.criteria.Criteria;
import org.minimalj.repository.criteria.SearchCriteria;
import org.minimalj.repository.sql.SqlRepository;

public class SqlCriteriaTest {

	private static SqlRepository respository;

	@BeforeClass
	public static void setupRepository() {
		respository = new SqlRepository(DataSourceFactory.embeddedDataSource(), G.class);
	}

	@AfterClass
	public static void shutdownRepository() {
	}

	@Test
	public void testQuery() {
		respository.insert(new G("abc"));
		respository.insert(new G("abcd"));
		respository.insert(new G("abcxyz"));

		Criteria s1 = By.search("abc");
		Criteria s2 = By.search("xyz");
		List<G> g = respository.read(G.class, s1.and(s2), 100);
		Assert.assertEquals(1, g.size());
		
		s1 = By.search("d");
		s2 = By.search("x");
		g = respository.read(G.class, s1.or(s2), 100);
		Assert.assertEquals(2, g.size());
		
		g = respository.read(G.class, s1.and(s2), 100);
		Assert.assertEquals(0, g.size());

		s1 = By.search("y");
		s2 = By.search("x");
		g = respository.read(G.class, s1.and(s2), 100);
		Assert.assertEquals(1, g.size());

		s1 = By.search("y");
		s2 = By.search("z");
		Criteria s3 = By.search("d");
		SearchCriteria s4 = By.search("y");
		g = respository.read(G.class, s1.and(s2).or(s3.and(s4.negate())), 100);
		Assert.assertEquals(2, g.size());
	}
}
