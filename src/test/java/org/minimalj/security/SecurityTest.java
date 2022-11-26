package org.minimalj.security;

import org.junit.Assert;
import org.junit.Test;
import org.minimalj.backend.repository.ReadEntityTransaction;
import org.minimalj.security.model.User;
import org.minimalj.security.model.UserRole;
import org.minimalj.security.permissiontest.TestEntityA;
import org.minimalj.security.permissiontest.TestEntityB;
import org.minimalj.security.permissiontest.TestEntityBView;
import org.minimalj.security.permissiontest.TestTransaction;
import org.minimalj.security.permissiontest.TestTransactionU;
import org.minimalj.security.permissiontest.pkgrole.TestEntityH;

public class SecurityTest {

	private Subject createTestSubject(String name, String role) {
		User user = new User();
		user.name = name;
		user.roles.add(new UserRole(role));
		return new Subject(user);
	}
	
	@Test
	public void testEntityWithoutRole() throws Exception {
		Assert.assertTrue("ReadTransaction without Role annotation to class or package should need no role",
				Authorization.hasAccess(new Subject("A"), new ReadEntityTransaction<>(TestEntityA.class, 1)));
	}

	@Test
	public void testEntityWithSingleRole() throws Exception {
		Subject subject = createTestSubject("A", "RoleA");
		Assert.assertFalse(Authorization.hasAccess(subject, new ReadEntityTransaction<>(TestEntityB.class, 1)));
		subject = createTestSubject("B", "RoleB");
		Assert.assertTrue(Authorization.hasAccess(subject, new ReadEntityTransaction<>(TestEntityB.class, 1)));
	}

	@Test
	public void testEntityView() throws Exception {
		Subject subject = createTestSubject("A", "RoleA");
		Assert.assertTrue(Authorization.hasAccess(subject, new ReadEntityTransaction<>(TestEntityBView.class, 1)));
		subject = createTestSubject("B", "RoleB");
		Assert.assertFalse(Authorization.hasAccess(subject, new ReadEntityTransaction<>(TestEntityBView.class, 1)));
	}

	//
	
	@Test
	public void testNotAuthorizedEntity() {
		Assert.assertFalse(Authorization.hasAccess(new Subject("A"), new ReadEntityTransaction<>(TestEntityH.class, 1)));
	}

	@Test
	public void testAuthorizedEntity() {
		Assert.assertTrue(Authorization.hasAccess(createTestSubject("B", "ClassRole"), new ReadEntityTransaction<>(TestEntityH.class, 1)));
	}

	@Test
	public void testAuthorizationOnPackageOverruledEntity() {
		Assert.assertFalse(Authorization.hasAccess(createTestSubject("C", "pkgRole"), new ReadEntityTransaction<>(TestEntityH.class, 1)));
	}

	@Test
	public void testAuthorizedTransaction() {
		Assert.assertTrue("Without a Role annotation itself an EntityTransaction depends on the Role of its entity", Authorization.hasAccess(createTestSubject("D", "ClassRole"), new TestTransaction<>()));
	}

	@Test
	public void testNotAuthorizedTransaction() {
		Assert.assertFalse("Without a Role annotation itself an EntityTransaction depends on the Role of its entity", Authorization.hasAccess(createTestSubject("E", "guest"), new TestTransaction<>()));
	}

	@Test
	public void testAuthorizedTransaction2() {
		Assert.assertTrue("An EntityTransaction can overrule the Role of its Entity", Authorization.hasAccess(createTestSubject("F", "transactionRole"), new TestTransactionU<>()));
	}

	@Test
	public void testNotAuthorizedTransaction2() {
		Assert.assertFalse("An EntityTransaction can overrule the Role of its Entity", Authorization.hasAccess(createTestSubject("G", "ClassRole"), new TestTransactionU<>()));
	}
}
