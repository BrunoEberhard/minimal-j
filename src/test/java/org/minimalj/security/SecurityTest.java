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

public class SecurityTest {

	@Test
	public void testEntityWithoutRole() throws Exception {
		Assert.assertTrue("ReadTransaction without Role annotation to class or package should need no role",
				Authorization.hasAccess(new Subject("A"), new ReadEntityTransaction<>(TestEntityA.class, 1)));
	}

	@Test
	public void testEntityWithSingleRole() throws Exception {
		Subject subject = new Subject("A", null, Collections.singletonList("RoleA"));
		Assert.assertFalse(Authorization.hasAccess(subject, new ReadEntityTransaction<>(TestEntityB.class, 1)));
		subject = new Subject("B", null, Collections.singletonList("RoleB"));
		Assert.assertTrue(Authorization.hasAccess(subject, new ReadEntityTransaction<>(TestEntityB.class, 1)));
	}

	@Test
	public void testEntityView() throws Exception {
		Subject subject = new Subject("A", null, Collections.singletonList("RoleA"));
		Assert.assertTrue(Authorization.hasAccess(subject, new ReadEntityTransaction<>(TestEntityBView.class, 1)));
		subject = new Subject("B", null, Collections.singletonList("RoleB"));
		Assert.assertFalse(Authorization.hasAccess(subject, new ReadEntityTransaction<>(TestEntityBView.class, 1)));
	}

	//
	
	@Test
	public void testNotAuthorizedEntity() {
		Assert.assertFalse(Authorization.hasAccess(new Subject("A"), new ReadEntityTransaction<>(TestEntityH.class, 1)));
	}

	private Subject subject(String role) {
		return new Subject("A", null, Collections.singletonList(role));
	}

	@Test
	public void testAuthorizedEntity() {
		Assert.assertTrue(Authorization.hasAccess(subject("ClassRole"), new ReadEntityTransaction<>(TestEntityH.class, 1)));
	}

	@Test
	public void testAuthorizationOnPackageOverruledEntity() {
		Assert.assertFalse(Authorization.hasAccess(subject("pkgRole"), new ReadEntityTransaction<>(TestEntityH.class, 1)));
	}

	@Test
	public void testAuthorizedTransaction() {
		Assert.assertTrue("Without a Role annotation itself an EntityTransaction depends on the Role of its entity", Authorization.hasAccess(subject("ClassRole"), new TestTransaction<>()));
	}

	@Test
	public void testNotAuthorizedTransaction() {
		Assert.assertFalse("Without a Role annotation itself an EntityTransaction depends on the Role of its entity", Authorization.hasAccess(subject("guest"), new TestTransaction<>()));
	}

	@Test
	public void testAuthorizedTransaction2() {
		Assert.assertTrue("An EntityTransaction can overrule the Role of its Entity", Authorization.hasAccess(subject("transactionRole"), new TestTransactionU<>()));
	}

	@Test
	public void testNotAuthorizedTransaction2() {
		Assert.assertFalse("An EntityTransaction can overrule the Role of its Entity", Authorization.hasAccess(subject("ClassRole"), new TestTransactionU<>()));
	}
}
