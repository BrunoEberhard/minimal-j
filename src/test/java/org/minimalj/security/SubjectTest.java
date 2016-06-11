package org.minimalj.security;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.minimalj.backend.sql.SqlPersistence;
import org.minimalj.security.permissiontest.A;
import org.minimalj.security.permissiontest.B;
import org.minimalj.security.permissiontest.C;
import org.minimalj.security.permissiontest.pkgrole.G;
import org.minimalj.security.permissiontest.pkgrole.H;
import org.minimalj.transaction.Role;
import org.minimalj.transaction.persistence.ReadEntityTransaction;
import org.minimalj.transaction.persistence.UpdateTransaction;

public class SubjectTest {

	private static SqlPersistence persistence;
	
	@BeforeClass
	public static void setupPersistence() {
		System.setProperty("derby.database.sqlAuthorization", "TRUE");
		persistence = new SqlPersistence(SqlPersistence.embeddedDataSource(), A.class, B.class, C.class, G.class, H.class);
	}
	
	@Test @Ignore
	public void testEntityWithoutRole() throws Exception {
		Assert.assertNull("ReadTransaction without Role annotation to class or package should need no role",
				Subject.getRole(new ReadEntityTransaction<>(A.class, null)));
	}

	@Test @Ignore // roles for entities need to be re implemented
	public void testEntityWithSingleRole() throws Exception {
		Subject subject = new Subject();
		subject.getRoles().add("RoleA");
		B b = new B();
		try {
			persistence.insert(b);
			Assert.fail("RoleA should not allow access to B");
		} catch (Exception e) {
			// expected
		}
		subject.getRoles().add("RoleB");
		persistence.insert(b);
	}

	@Test @Ignore
	public void testEntityWithSeveralRoles() throws Exception {
		Role role = Subject.getRole(new UpdateTransaction<>(new C()));
		Assert.assertNotNull(role);
		Assert.assertEquals("UpdateTransaction with a update specific Role should need this role", "UpdateRole", role.value()[0]);
	}

	@Test @Ignore
	public void testEntityWithoutRoleButPkgRole() throws Exception {
		Role role = Subject.getRole(new UpdateTransaction<>(new G()));
		Assert.assertNotNull(role);
		Assert.assertEquals("UpdateTransaction without a Role on class but one on package-info should need package role", "UpdatePkgRole",
				role.value()[0]);
	}

	@Test @Ignore
	public void testEntityWithRoleAndPkgRole() throws Exception {
		Role role = Subject.getRole(new UpdateTransaction<>(new H()));
		Assert.assertNotNull(role);
		Assert.assertEquals("UpdateTransaction with a Role in class and one on package should use the one on from the class", "UpdateClassRole",
				role.value()[0]);
	}

}
