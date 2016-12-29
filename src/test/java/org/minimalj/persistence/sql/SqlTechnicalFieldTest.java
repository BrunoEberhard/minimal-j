package org.minimalj.persistence.sql;

import java.time.LocalDateTime;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.security.Subject;

public class SqlTechnicalFieldTest {

	private static SqlPersistence persistence;

	@BeforeClass
	public static void setupPersistence() {
		persistence = new SqlPersistence(SqlPersistence.embeddedDataSource(), S.class, U.class);
	}

	@Test
	public void testCreate() {
		S s = new S();
		s.string = "Testobject";

		Subject subject = new Subject();
		subject.setName("A");
		Subject.setCurrent(subject);

		LocalDateTime before = LocalDateTime.now();

		Object id = persistence.insert(s);
		s = persistence.read(S.class, id);
		LocalDateTime after = LocalDateTime.now();

		Assert.assertEquals("A", s.createUser);
		Assert.assertTrue(before.compareTo(s.createDate) <= 0);
		Assert.assertTrue(after.compareTo(s.createDate) >= 0);
	}

	@Test
	public void testEdit() {
		S s = new S();
		s.string = "Testobject";

		Subject subject = new Subject();
		subject.setName("B");
		Subject.setCurrent(subject);

		LocalDateTime before = LocalDateTime.now();

		Object id = persistence.insert(s);
		s = persistence.read(S.class, id);
		LocalDateTime afterInsert = LocalDateTime.now();

		subject = new Subject();
		subject.setName("C");
		Subject.setCurrent(subject);

		s.string = "Changed";
		persistence.update(s);
		s = persistence.read(S.class, id);
		LocalDateTime afterEdit = LocalDateTime.now();

		// create time / user should not be changed
		Assert.assertEquals("B", s.createUser);
		Assert.assertTrue(before.compareTo(s.createDate) <= 0);
		Assert.assertTrue(afterInsert.compareTo(s.createDate) >= 0);

		Assert.assertEquals("C", s.editUser);
		Assert.assertTrue(afterInsert.compareTo(s.editDate) <= 0);
		Assert.assertTrue(afterEdit.compareTo(s.editDate) >= 0);
	}

	@Test
	public void testCreateHistorized() {
		U u = new U();
		u.string = "Testobject";

		Subject subject = new Subject();
		subject.setName("A");
		Subject.setCurrent(subject);

		LocalDateTime before = LocalDateTime.now();

		Object id = persistence.insert(u);
		u = persistence.read(U.class, id);
		LocalDateTime after = LocalDateTime.now();

		Assert.assertEquals("A", u.createUser);
		Assert.assertTrue(before.compareTo(u.createDate) <= 0);
		Assert.assertTrue(after.compareTo(u.createDate) >= 0);
	}

	@Test
	public void testEditHistorized() {
		U u = new U();
		u.string = "Testobject";

		Subject subject = new Subject();
		subject.setName("B");
		Subject.setCurrent(subject);

		LocalDateTime before = LocalDateTime.now();

		Object id = persistence.insert(u);
		u = persistence.read(U.class, id);
		LocalDateTime afterInsert = LocalDateTime.now();

		subject = new Subject();
		subject.setName("C");
		Subject.setCurrent(subject);

		u.string = "Changed";
		persistence.update(u);
		u = persistence.read(U.class, id);
		LocalDateTime afterEdit = LocalDateTime.now();

		// create time / user should not be changed
		Assert.assertEquals("B", u.createUser);
		Assert.assertTrue(before.compareTo(u.createDate) <= 0);
		Assert.assertTrue(afterInsert.compareTo(u.createDate) >= 0);

		Assert.assertEquals("C", u.editUser);
		Assert.assertTrue(afterInsert.compareTo(u.editDate) <= 0);
		Assert.assertTrue(afterEdit.compareTo(u.editDate) >= 0);
	}

}
