package org.minimalj.persistence.sql;

import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.persistence.criteria.By;
import org.minimalj.persistence.criteria.FieldOperator;
import org.minimalj.persistence.sql.SqlPersistence;

public class SqlSimpleCriteriaTest {
	
	private static SqlPersistence persistence;
	
	@BeforeClass
	public static void setupPersistence() {
		persistence = new SqlPersistence(SqlPersistence.embeddedDataSource(), A.class, G.class, H.class);
		
		insertA(5);
		insertA(7);
		insertA(8);
	}
	
	private static void insertA(int int1) {
		A a = new A();
		a.int1 = int1;
		persistence.insert(a);
	}
	
	@AfterClass
	public static void shutdownPersistence() {
	}
	
	@Test 
	public void testFindByIntegerField() {
		List<A> list = persistence.getTable(A.class).read(By.field(A.$.int1, 5), 2);
		Assert.assertEquals(1, list.size());
		A a = list.get(0);
		Assert.assertNotNull(a.int1);
		Assert.assertEquals(5, (int) a.int1);
	}
	
	@Test 
	public void testMinimumForField() {
		List<A> list = persistence.getTable(A.class).read(By.field(A.$.int1, FieldOperator.greaterOrEqual, 7), 3);
		Assert.assertEquals(2, list.size());
	}
	
	@Test(expected = IllegalArgumentException.class) 
	public void testOperatorCheck() {
		persistence.getTable(A.class).read(By.field(A.$.e, FieldOperator.greaterOrEqual, 7), 1);
	}

}
