package ch.openech.test.db;

import java.sql.SQLException;
import java.util.List;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.openech.mj.criteria.Criteria;
import ch.openech.mj.criteria.CriteriaOperator;
import ch.openech.mj.db.DbPersistence;

public class DbSimpleCriteriaTest {
	
	private static DbPersistence persistence;
	
	@BeforeClass
	public static void setupDb() throws SQLException {
		persistence = new DbPersistence(DbPersistence.embeddedDataSource(), A.class, G.class, H.class);
		
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
	public static void shutdownDb() throws SQLException {
	}
	
	@Test 
	public void testFindByIntegerField() throws SQLException {
		List<A> list = persistence.getTable(A.class).read(new Criteria.SimpleCriteria(A.A.int1, 5));
		Assert.assertEquals(1, list.size());
		A a = list.get(0);
		Assert.assertNotNull(a.int1);
		Assert.assertEquals(5, (int) a.int1);
	}
	
	@Test 
	public void testMinimumForField() throws SQLException {
		List<A> list = persistence.getTable(A.class).read(new Criteria.SimpleCriteria(A.A.int1, CriteriaOperator.greaterOrEqual, 7));
		Assert.assertEquals(2, list.size());
	}
	
	@Test(expected = IllegalArgumentException.class) 
	public void testOperatorCheck() throws SQLException {
		persistence.getTable(A.class).read(new Criteria.SimpleCriteria(A.A.e, CriteriaOperator.greaterOrEqual, 7));
	}

}
