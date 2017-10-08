package org.minimalj.repository.ignite;

import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.repository.ignite.IgniteRepository;
import org.minimalj.repository.query.By;
import org.minimalj.repository.query.Criteria;
import org.minimalj.repository.query.SearchCriteria;

public class IgniteDbCriteriaTest {

	private static IgniteRepository respository;

	@BeforeClass
	public static void setupRepository() {
		respository = new IgniteRepository(G.class);
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
		List<G> g = respository.find(G.class, s1.and(s2));
		Assert.assertEquals(1, g.size());
		
		s1 = By.search("d");
		s2 = By.search("x");
		g = respository.find(G.class, s1.or(s2));
		Assert.assertEquals(2, g.size());
		
		g = respository.find(G.class, s1.and(s2));
		Assert.assertEquals(0, g.size());

		s1 = By.search("y");
		s2 = By.search("x");
		g = respository.find(G.class, s1.and(s2));
		Assert.assertEquals(1, g.size());

		s1 = By.search("y");
		s2 = By.search("z");
		Criteria s3 = By.search("d");
		SearchCriteria s4 = By.search("y");
		g = respository.find(G.class, s1.and(s2).or(s3.and(s4.negate())));
		Assert.assertEquals(2, g.size());
	}
}
