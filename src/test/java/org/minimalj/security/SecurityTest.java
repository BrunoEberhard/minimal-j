package org.minimalj.security;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;
import org.minimalj.backend.repository.ReadEntityTransaction;
import org.minimalj.security.permissiontest.TestEntityA;
import org.minimalj.security.permissiontest.TestEntityB;
import org.minimalj.security.permissiontest.TestEntityBView;
import org.minimalj.security.permissiontest.TestTransaction;
import org.minimalj.security.permissiontest.TestTransactionU;
import org.minimalj.security.permissiontest.pkgrole.TestEntityH;
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
	
	public void testNotAuthorizedEntity() {
		Assert.assertFalse(Authorization.isAllowed(Collections.emptyList(), new ReadEntityTransaction<>(TestEntityH.class, 1)));
	}

	@Test
	public void testAuthorizedEntity() {
		Assert.assertTrue(Authorization.isAllowed(Collections.singletonList("ClassRole"), new ReadEntityTransaction<>(TestEntityH.class, 1)));
	}

	@Test
	public void testAuthorizationOnPackageOverruledEntity() {
		Assert.assertFalse(Authorization.isAllowed(Collections.singletonList("pkgRole"), new ReadEntityTransaction<>(TestEntityH.class, 1)));
	}

	@Test
	public void testAuthorizedTransaction() {
		Assert.assertTrue("Without a Role annotation itself an EntityTransaction depends on the Role of its entity", Authorization.isAllowed(Collections.singletonList("ClassRole"), new TestTransaction<>()));
	}

	@Test
	public void testNotAuthorizedTransaction() {
		Assert.assertFalse("Without a Role annotation itself an EntityTransaction depends on the Role of its entity", Authorization.isAllowed(Collections.singletonList("guest"), new TestTransaction<>()));
	}

	@Test
	public void testAuthorizedTransaction2() {
		Assert.assertTrue("An EntityTransaction can overrule the Role of its Entity", Authorization.isAllowed(Collections.singletonList("transactionRole"), new TestTransactionU<>()));
	}

	@Test
	public void testNotAuthorizedTransaction2() {
		Assert.assertFalse("An EntityTransaction can overrule the Role of its Entity", Authorization.isAllowed(Collections.singletonList("ClassRole"), new TestTransactionU<>()));
	}
}
