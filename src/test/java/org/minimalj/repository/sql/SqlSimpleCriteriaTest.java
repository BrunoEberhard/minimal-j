package org.minimalj.repository.sql;

import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.repository.DataSourceFactory;
import org.minimalj.repository.criteria.By;
import org.minimalj.repository.criteria.FieldOperator;

public class SqlSimpleCriteriaTest {
	
	private static SqlRepository repository;
	
	@BeforeClass
	public static void setupRepository() {
		repository = new SqlRepository(DataSourceFactory.embeddedDataSource(), A.class, G.class, H.class);
		
		insertA(5);
		insertA(7);
		insertA(8);
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
		List<A> list = repository.getTable(A.class).read(By.field(A.$.int1, 5), 2);
		Assert.assertEquals(1, list.size());
		A a = list.get(0);
		Assert.assertNotNull(a.int1);
		Assert.assertEquals(5, (int) a.int1);
	}
	
	@Test 
	public void testMinimumForField() {
		List<A> list = repository.getTable(A.class).read(By.field(A.$.int1, FieldOperator.greaterOrEqual, 7), 3);
		Assert.assertEquals(2, list.size());
	}
	
	@Test(expected = IllegalArgumentException.class) 
	public void testOperatorCheck() {
		repository.getTable(A.class).read(By.field(A.$.e, FieldOperator.greaterOrEqual, 7), 1);
	}

}
