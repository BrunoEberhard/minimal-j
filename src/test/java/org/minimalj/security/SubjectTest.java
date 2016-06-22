package org.minimalj.security;

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.backend.sql.SqlPersistence;
import org.minimalj.security.permissiontest.A;
import org.minimalj.security.permissiontest.B;
import org.minimalj.security.permissiontest.C;
import org.minimalj.security.permissiontest.pkgrole.G;
import org.minimalj.security.permissiontest.pkgrole.H;
import org.minimalj.transaction.persistence.ReadEntityTransaction;

public class SubjectTest {

	private static SqlPersistence persistence;
	
	@BeforeClass
	public static void setupPersistence() {
		persistence = new SqlPersistence(SqlPersistence.embeddedDataSource(), A.class, B.class, C.class, G.class, H.class);
	}
	
	@Test
	public void testEntityWithoutRole() throws Exception {
		Assert.assertNull("ReadTransaction without Role annotation to class or package should need no role",
				Authorization.getRole(new ReadEntityTransaction<>(A.class, null)));
	}

	@Test // roles for entities need to be re implemented
	public void testEntityWithSingleRole() throws Exception {
		Authorization authorization = new Authorization() {
			@Override
			protected List<String> retrieveRoles(UserPassword userPassword) {
				return Collections.emptyList();
			}
		};
		persistence.setAuthorization(authorization);
		Subject subject = new Subject();
		subject.getRoles().add("RoleA");
		Subject.setCurrent(subject);
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

}
