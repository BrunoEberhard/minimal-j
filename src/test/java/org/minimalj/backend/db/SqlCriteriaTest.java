package org.minimalj.backend.db;

import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.backend.sql.SqlPersistence;
import org.minimalj.transaction.predicate.By;
import org.minimalj.transaction.predicate.Criteria;
import org.minimalj.transaction.predicate.SearchCriteria;

public class SqlCriteriaTest {

	private static SqlPersistence persistence;

	@BeforeClass
	public static void setupPersistence() {
		persistence = new SqlPersistence(SqlPersistence.embeddedDataSource(), A.class, G.class, H.class, M.class);
	}

	@AfterClass
	public static void shutdownPersistence() {
	}

	@Test
	public void testQuery() {
		persistence.insert(new G("abc"));
		persistence.insert(new G("abcd"));
		persistence.insert(new G("abcxyz"));

		Criteria s1 = By.search("%abc%");
		Criteria s2 = By.search("%xyz%");
		List<G> g = persistence.read(G.class, s1.and(s2), 100);
		Assert.assertEquals(1, g.size());
		
		s1 = By.search("%d%");
		s2 = By.search("%x%");
		g = persistence.read(G.class, s1.or(s2), 100);
		Assert.assertEquals(2, g.size());
		
		g = persistence.read(G.class, s1.and(s2), 100);
		Assert.assertEquals(0, g.size());

		s1 = By.search("%y%");
		s2 = By.search("%x%");
		g = persistence.read(G.class, s1.and(s2), 100);
		Assert.assertEquals(1, g.size());

		s1 = By.search("%y%");
		s2 = By.search("%z%");
		Criteria s3 = By.search("%d%");
		SearchCriteria s4 = By.search("%y%");
		g = persistence.read(G.class, s1.and(s2).or(s3.and(s4.negate())), 100);
		Assert.assertEquals(2, g.size());
	}
}
