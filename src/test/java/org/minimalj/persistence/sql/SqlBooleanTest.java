package org.minimalj.persistence.sql;

import org.junit.BeforeClass;
import org.junit.Test;

public class SqlBooleanTest {

	private static SqlPersistence persistence;
	
	@BeforeClass
	public static void setupPersistence() {
		persistence = new SqlPersistence(SqlPersistence.embeddedDataSource(), P.class);
	}
	
	@Test
	public void testValidBooleans() {
		P p = new P();
		p.notEmptyBoolean = true;
		p.optionalBoolean = false;
		
		persistence.insert(p);
	}

	@Test(expected = RuntimeException.class)
	public void testInvalidBooleans() {
		P p = new P();
		
		persistence.insert(p);
	}

}
