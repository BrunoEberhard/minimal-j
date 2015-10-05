package org.minimalj.security;

import org.junit.Assert;
import org.junit.Test;
import org.minimalj.security.permissiontest.A;
import org.minimalj.security.permissiontest.B;
import org.minimalj.security.permissiontest.C;
import org.minimalj.security.permissiontest.pkgrole.G;
import org.minimalj.security.permissiontest.pkgrole.H;
import org.minimalj.security.permissiontest.pkgrole.T;
import org.minimalj.security.permissiontest.pkgrole.U;
import org.minimalj.transaction.Role;
import org.minimalj.transaction.persistence.ReadTransaction;
import org.minimalj.transaction.persistence.UpdateTransaction;

public class SubjectTest {

	@Test
	public void testEntityWithoutRole() throws Exception {
		Assert.assertNull("ReadTransaction without Role annotation to class or package should need no role",
				Subject.getRole(new ReadTransaction<>(A.class, null)));
	}

	@Test
	public void testEntityWithSingleRole() throws Exception {
		Role role = Subject.getRole(new ReadTransaction<>(B.class, null));
		Assert.assertNotNull(role);
		Assert.assertEquals("ReadTransaction with a Role annotation on entity should need this role", "RoleB", role.value()[0]);
	}

	@Test
	public void testEntityWithSeveralRoles() throws Exception {
		Role role = Subject.getRole(new UpdateTransaction<>(new C()));
		Assert.assertNotNull(role);
		Assert.assertEquals("UpdateTransaction with a update specific Role should need this role", "UpdateRole", role.value()[0]);
	}

	@Test
	public void testEntityWithoutRoleButPkgRole() throws Exception {
		Role role = Subject.getRole(new UpdateTransaction<>(new G()));
		Assert.assertNotNull(role);
		Assert.assertEquals("UpdateTransaction without a Role on class but one on package-info should need package role", "UpdatePkgRole",
				role.value()[0]);
	}

	@Test
	public void testEntityWithRoleAndPkgRole() throws Exception {
		Role role = Subject.getRole(new UpdateTransaction<>(new H()));
		Assert.assertNotNull(role);
		Assert.assertEquals("UpdateTransaction with a Role in class and one on package should use the one on from the class", "UpdateClassRole",
				role.value()[0]);
	}

	@Test
	public void testTransactionWithoutRoleButPkgRole() throws Exception {
		Role role = Subject.getRole(new T());
		Assert.assertNotNull(role);
		Assert.assertEquals("Custom transaction without role annotation should use the one on package", "pkgRole", role.value()[0]);
	}

	@Test
	public void testTransactionWithRoleAndPkgRole() throws Exception {
		Role role = Subject.getRole(new U());
		Assert.assertNotNull(role);
		Assert.assertEquals("Custom transaction with role annotation should need this role", "transactionRole", role.value()[0]);
	}
}
