package org.minimalj.backend.db;

import java.util.List;
import java.util.function.Predicate;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.transaction.predicate.By;

public class DbPredicateTest {

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
		persistence.insert(new G("abc"));
		persistence.insert(new G("abcd"));
		persistence.insert(new G("abcxyz"));

		Predicate<G> s1 = By.search("%abc%");
		Predicate<G> s2 = By.search("%xyz%");
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
		Predicate<G> s3 = By.search("%c%");
		Predicate<G> s4 = By.search("%d%");
		g = persistence.read(G.class, s1.and(s2).or(s3.and(s4)), 100);
		Assert.assertEquals(2, g.size());
	}
}
