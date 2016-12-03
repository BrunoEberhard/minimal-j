package org.minimalj.persistence.sql;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class SqlPessimisticLockingTest {
	
	private static SqlPersistence persistence;
	
	@BeforeClass
	public static void setupPersistence() {
		persistence = new SqlPersistence(SqlPersistence.embeddedDataSource(), Q.class);
	}
	
	@AfterClass
	public static void shutdownPersistence() {
	}
	
	@Test
	public void testPessimisticLockingOk() {
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
	public void testPessimisticLockingFail() {
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

}
