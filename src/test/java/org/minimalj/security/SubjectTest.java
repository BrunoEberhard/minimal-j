package org.minimalj.security;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.backend.persistence.ReadEntityTransaction;
import org.minimalj.persistence.sql.SqlPersistence;
import org.minimalj.security.permissiontest.TestEntityA;
import org.minimalj.security.permissiontest.TestEntityB;
import org.minimalj.security.permissiontest.TestEntityC;
import org.minimalj.security.permissiontest.pkgrole.G;
import org.minimalj.security.permissiontest.pkgrole.H;

public class SubjectTest {

	private static SqlPersistence persistence;
	
	@BeforeClass
	public static void setupPersistence() {
		persistence = new SqlPersistence(SqlPersistence.embeddedDataSource(), TestEntityA.class, TestEntityB.class, TestEntityC.class, G.class, H.class);
	}
	
	@Test
	public void testEntityWithoutRole() throws Exception {
		Assert.assertNull("ReadTransaction without Role annotation to class or package should need no role",
				Authorization.getRole(new ReadEntityTransaction<>(TestEntityA.class, null)));
	}

	@Test // roles for entities need to be re implemented
	public void testEntityWithSingleRole() throws Exception {
		Subject subject = new Subject();
		subject.getRoles().add("RoleA");
		Subject.setCurrent(subject);
		TestEntityB b = new TestEntityB();
		try {
			persistence.insert(b);
			Assert.fail("RoleA should not allow access to B");
		} catch (Exception e) {
			// expected
		}
		subject.getRoles().add("RoleB");
		persistence.insert(b);
	}

}
