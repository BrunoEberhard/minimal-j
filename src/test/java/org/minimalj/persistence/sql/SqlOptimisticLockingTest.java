package org.minimalj.persistence.sql;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class SqlOptimisticLockingTest {
	
	private static SqlPersistence persistence;
	
	@BeforeClass
	public static void setupPersistence() {
		persistence = new SqlPersistence(SqlPersistence.embeddedDataSource(), Q.class, R.class);
	}
	
	@AfterClass
	public static void shutdownPersistence() {
	}
	
	@Test
	public void testOptimisticLockingOk() {
		Q q = new Q();
		q.string = "A";
		Object id = persistence.insert(q);
		q = persistence.read(Q.class, id);
		
		q.string = "B";
		persistence.update(q);
		q = persistence.read(Q.class, id);
		
		q.string = "C";
		persistence.update(q);
	}

	@Test(expected = Exception.class)
	public void testOptimisticLockingFail() {
		Q q = new Q();
		q.string = "A";
		Object id = persistence.insert(q);
		q = persistence.read(Q.class, id);
		
		q.string = "B";
		persistence.update(q);
		// here the read is forgotten
		
		// this tries to update an old version of q
		q.string = "C";
		persistence.update(q);
	}
	
	@Test
	public void testHistorizedOptimisticLockingOk() {
		R r = new R();
		r.string = "A";
		Object id = persistence.insert(r);
		r = persistence.read(R.class, id);
		
		r.string = "B";
		persistence.update(r);
		r = persistence.read(R.class, id);
		
		r.string = "C";
		persistence.update(r);
	}

	@Test(expected = Exception.class)
	public void testHistorizedOptimisticLockingFail() {
		R r = new R();
		r.string = "A";
		Object id = persistence.insert(r);
		r = persistence.read(R.class, id);
		
		r.string = "B";
		persistence.update(r);
		// here the read is forgotten
		
		// this tries to update an old version of r
		r.string = "C";
		persistence.update(r);
	}

}
