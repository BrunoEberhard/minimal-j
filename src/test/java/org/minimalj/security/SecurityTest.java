package org.minimalj.security;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;
import org.minimalj.backend.repository.ReadEntityTransaction;
import org.minimalj.security.permissiontest.TestEntityA;
import org.minimalj.security.permissiontest.TestEntityB;
import org.minimalj.security.permissiontest.pkgrole.TestEntityG;
import org.minimalj.transaction.TransactionUtil;

public class SecurityTest {

	@Test
	public void testEntityWithoutRole() throws Exception {
		Assert.assertNull("ReadTransaction without Role annotation to class or package should need no role",
				TransactionUtil.getRole(new ReadEntityTransaction<TestEntityA>(TestEntityA.class, 1)));
	}

	@Test
	public void testEntityWithSingleRole() throws Exception {
		Subject subject = new Subject();
		subject.getRoles().add("RoleA");
		Subject.setCurrent(subject);
		try {
			Authorization.check(new ReadEntityTransaction<TestEntityB>(TestEntityB.class, 1));
			Assert.fail("RoleA should not allow access to B");
		} catch (Exception e) {
			// expected
		}
		subject.getRoles().add("RoleB");
		Authorization.check(new ReadEntityTransaction<TestEntityB>(TestEntityB.class, 1));
	}
	
	//
	
	public void testNotAuthorized() {
		Assert.assertFalse(Authorization.isAllowed(Collections.emptyList(), new ReadEntityTransaction<TestEntityG>(TestEntityG.class, 1)));
	}

	@Test
	public void testGranted() {
		Assert.assertTrue(Authorization.isAllowed(Collections.singletonList("pkgRole"), new ReadEntityTransaction<TestEntityG>(TestEntityG.class, 1)));
	}

	@Test
	public void testGrantedReadOnPackage() {
		Assert.assertTrue(Authorization.isAllowed(Collections.singletonList("pkgRole"), new ReadEntityTransaction<TestEntityG>(TestEntityG.class, 1)));
	}

}
