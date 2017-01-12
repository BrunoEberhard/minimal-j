package org.minimalj.security;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;
import org.minimalj.security.permissiontest.TestEntityA;
import org.minimalj.security.permissiontest.TestEntityB;
import org.minimalj.security.permissiontest.pkgrole.TestEntityG;
import org.minimalj.security.permissiontest.pkgrole.TestTransactionU;

public class SecurityTest {

	@Test
	public void testEntityWithoutRole() throws Exception {
		Assert.assertNull("ReadTransaction without Role annotation to class or package should need no role",
				Authorization.getRole(TestEntityA.class));
	}

	@Test
	public void testEntityWithSingleRole() throws Exception {
		Authorization authorization = new Authorization();
		Subject subject = new Subject();
		subject.getRoles().add("RoleA");
		Subject.setCurrent(subject);
		try {
			authorization.check(new TestEntityB());
			Assert.fail("RoleA should not allow access to B");
		} catch (Exception e) {
			// expected
		}
		subject.getRoles().add("RoleB");
		authorization.check(new TestEntityB());
	}
	
	//
	
	public void testNotAuthorized() {
		Assert.assertFalse(Authorization.isAllowed(Collections.emptyList(), TestEntityG.class));
	}

	@Test
	public void testGranted() {
		Assert.assertTrue(Authorization.isAllowed(Collections.singletonList("pkgRole"), TestEntityG.class));
	}

	@Test
	public void testGrantedReadOnPackage() {
		Assert.assertTrue(Authorization.isAllowed(Collections.singletonList("pkgRole"), TestEntityG.class));
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
