package org.minimalj.repository.sql;

import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.repository.DataSourceFactory;
import org.minimalj.repository.query.By;
import org.minimalj.repository.query.FieldOperator;

public class SqlSimpleCriteriaTest {
	
	private static SqlRepository repository;
	
	@BeforeClass
	public static void setupRepository() {
		repository = new SqlRepository(DataSourceFactory.embeddedDataSource(), A.class, G.class, H.class);
		
		insertA(5);
		insertA(7);
		insertA(8);

		repository.insert(new G("a"));
		repository.insert(new G("b"));
		repository.insert(new G(null));
	}
	
	private static void insertA(int int1) {
		A a = new A();
		a.int1 = int1;
		repository.insert(a);
	}
	
	@AfterClass
	public static void shutdownRepository() {
	}
	
	@Test 
	public void testFindByIntegerField() {
		List<A> list = repository.find(A.class, By.field(A.$.int1, 5).limit(2));
		Assert.assertEquals(1, list.size());
		A a = list.get(0);
		Assert.assertNotNull(a.int1);
		Assert.assertEquals(5, (int) a.int1);
	}
	
	@Test 
	public void testMinimumForField() {
		List<A> list = repository.find(A.class, By.field(A.$.int1, FieldOperator.greaterOrEqual, 7).limit(3));
		Assert.assertEquals(2, list.size());
	}
	
	@Test(expected = IllegalArgumentException.class) 
	public void testOperatorCheck() {
		repository.find(A.class, By.field(A.$.e, FieldOperator.greaterOrEqual, 7));
	}

	@Test
	public void testFindByNull() {
		List<G> g = repository.find(G.class, By.field(G.$.g, null));
		Assert.assertEquals(1, g.size());
	}

	@Test
	public void testFindByNotNull() {
		List<G> g = repository.find(G.class, By.field(G.$.g, FieldOperator.notEqual, null));
		Assert.assertEquals(2, g.size());
	}
}
