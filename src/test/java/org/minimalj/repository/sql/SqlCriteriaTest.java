package org.minimalj.repository.sql;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.minimalj.repository.query.By;
import org.minimalj.repository.query.Criteria;
import org.minimalj.repository.query.SearchCriteria;

public class SqlCriteriaTest extends SqlTest {

	@Override
	public Class<?>[] getEntityClasses() {
		return new Class<?>[] { G.class };
	}

	@Test
	public void testQuery() {
		repository.insert(new G("abc"));
		repository.insert(new G("abcd"));
		repository.insert(new G("abcxyz"));

		Criteria s1 = By.search("abc");
		Criteria s2 = By.search("xyz");
		List<G> g = repository.find(G.class, s1.and(s2));
		Assert.assertEquals(1, g.size());
		
		s1 = By.search("d");
		s2 = By.search("x");
		g = repository.find(G.class, s1.or(s2));
		Assert.assertEquals(2, g.size());
		
		g = repository.find(G.class, s1.and(s2));
		Assert.assertEquals(0, g.size());

		s1 = By.search("y");
		s2 = By.search("x");
		g = repository.find(G.class, s1.and(s2));
		Assert.assertEquals(1, g.size());

		s1 = By.search("y");
		s2 = By.search("z");
		Criteria s3 = By.search("d");
		SearchCriteria s4 = (SearchCriteria) By.search("y");
		g = repository.find(G.class, s1.and(s2).or(s3.and(s4.negate())));
		Assert.assertEquals(2, g.size());
	}
}
