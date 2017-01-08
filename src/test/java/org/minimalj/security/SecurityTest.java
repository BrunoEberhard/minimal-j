package org.minimalj.security;

import java.util.Collections;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.persistence.sql.SqlPersistence;
import org.minimalj.security.permissiontest.TestEntityA;
import org.minimalj.security.permissiontest.TestEntityB;
import org.minimalj.security.permissiontest.TestEntityC;
import org.minimalj.security.permissiontest.pkgrole.TestEntityG;
import org.minimalj.security.permissiontest.pkgrole.TestEntityH;
import org.minimalj.security.permissiontest.pkgrole.TestTransactionU;

public class SecurityTest {

	private static SqlPersistence persistence;
	
	@BeforeClass
	public static void setupPersistence() {
		persistence = new SqlPersistence(SqlPersistence.embeddedDataSource(), TestEntityA.class, TestEntityB.class, TestEntityC.class, TestEntityG.class, TestEntityH.class);
	}
	
	@Test
	public void testEntityWithoutRole() throws Exception {
		Assert.assertNull("ReadTransaction without Role annotation to class or package should need no role",
				Authorization.getRole(TestEntityA.class));
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
	
	//
	
	@Test(expected = Exception.class)
	public void testNotAuthorized() {
		Authorization.checkAuthorization(Collections.emptyList(), TestEntityG.class);
	}

	@Test
	public void testGranted() {
		Authorization.checkAuthorization(Collections.singletonList("pkgRole"), TestEntityG.class);
	}

	@Test
	public void testGrantedReadOnPackage() {
		Authorization.checkAuthorization(Collections.singletonList("pkgRole"), TestEntityG.class);
	}

	//
	
	@Test
	public void testTransactionWithoutRole() {
		Assert.assertNull(Authorization.getRole(TestEntityA.class));
	}

	@Test
	public void testTransactionWithRole() {
		Assert.assertArrayEquals(new String[]{"transactionRole"}, Authorization.getRole(TestTransactionU.class).value());
	}

}
