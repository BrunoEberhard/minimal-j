package org.minimalj.security;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;
import org.minimalj.backend.repository.ReadEntityTransaction;
import org.minimalj.security.permissiontest.TestEntityA;
import org.minimalj.security.permissiontest.TestEntityB;
import org.minimalj.security.permissiontest.TestEntityBView;
import org.minimalj.security.permissiontest.pkgrole.TestEntityG;
import org.minimalj.transaction.TransactionAnnotations;

public class SecurityTest {

	@Test
	public void testEntityWithoutRole() throws Exception {
		Assert.assertNull("ReadTransaction without Role annotation to class or package should need no role",
				TransactionAnnotations.getRoles(new ReadEntityTransaction<>(TestEntityA.class, 1)));
	}

	@Test
	public void testEntityWithSingleRole() throws Exception {
		Subject subject = new Subject();
		subject.getRoles().add("RoleA");
		Subject.setCurrent(subject);
		try {
			Authorization.check(new ReadEntityTransaction<>(TestEntityB.class, 1));
			Assert.fail("RoleA should not allow access to TestEntityB");
		} catch (Exception e) {
			// expected
		}
		subject.getRoles().add("RoleB");
		Authorization.check(new ReadEntityTransaction<>(TestEntityB.class, 1));
	}

	@Test
	public void testEntityView() throws Exception {
		Subject subject = new Subject();
		subject.getRoles().add("RoleA");
		Subject.setCurrent(subject);
		Authorization.check(new ReadEntityTransaction<>(TestEntityBView.class, 1));
		subject.getRoles().remove("RoleA");
		subject.getRoles().add("RoleB");
		try {
			Authorization.check(new ReadEntityTransaction<>(TestEntityBView.class, 1));
			Assert.fail("RoleB should not allow access to TestEntityBView");
		} catch (Exception e) {
			// expected
		}
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
