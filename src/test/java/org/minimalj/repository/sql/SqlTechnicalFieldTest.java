package org.minimalj.repository.sql;

import java.time.LocalDateTime;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;
import org.minimalj.model.annotation.TechnicalField;
import org.minimalj.model.annotation.TechnicalField.TechnicalFieldType;
import org.minimalj.security.Subject;

public class SqlTechnicalFieldTest extends SqlTest {

	@Override
	public Class<?>[] getEntityClasses() {
		return new Class<?>[] { TestEntity.class, TestUser.class };
	}

	@Test
	public void testCreate() {
		TestEntity entity = new TestEntity();
		entity.string = "Testobject";

		TestUser testUserA = new TestUser("A");
		testUserA.id = (Integer) repository.insert(testUserA);
				
		Subject subject = new Subject(testUserA, testUserA.login, null, Collections.emptyList());
		Subject.setCurrent(subject);

		LocalDateTime before = LocalDateTime.now();

		Object id = repository.insert(entity);
		entity = repository.read(TestEntity.class, id);
		LocalDateTime after = LocalDateTime.now();

		Assert.assertEquals(testUserA.id, entity.createUser.id);
		Assert.assertEquals("A", entity.createUserName);
		Assert.assertTrue("Date of create " + entity.createDate + " must not be before start " + before, before.minusSeconds(1).compareTo(entity.createDate) <= 0);
		Assert.assertTrue("Date of create " + entity.createDate + " must not be after now " + after, after.plusSeconds(1).compareTo(entity.createDate) >= 0);
	}

	@Test
	public void testEdit() {
		TestEntity entity = new TestEntity();
		entity.string = "Testobject";
		
		TestUser testUserB = new TestUser("B");
		testUserB.id = (Integer) repository.insert(testUserB);
		
		TestUser testUserC = new TestUser("C");
		testUserC.id = (Integer) repository.insert(testUserC);

		Subject subject = new Subject(testUserB, testUserB.login, null, Collections.emptyList());
		Subject.setCurrent(subject);

		Object id = repository.insert(entity);
		entity = repository.read(TestEntity.class, id);
		LocalDateTime afterInsert = LocalDateTime.now();
		LocalDateTime createDate = entity.createDate;

		subject = new Subject(testUserC, testUserC.login, null, Collections.emptyList());
		Subject.setCurrent(subject);

		entity.string = "Changed";
		repository.update(entity);
		entity = repository.read(TestEntity.class, id);
		LocalDateTime afterEdit = LocalDateTime.now();

		Assert.assertEquals(testUserB.id, entity.createUser.id);
		Assert.assertEquals("B", entity.createUserName);
		Assert.assertTrue("Date of create " + createDate + "must not be changed to " + entity.createDate, createDate.compareTo(entity.createDate) == 0);

		Assert.assertEquals(testUserC.id, entity.editUser.id);
		Assert.assertEquals("C", entity.editUserName);
		Assert.assertTrue("Date of edit " + entity.editDate + " must not be after start " + afterInsert, afterInsert.minusSeconds(1).compareTo(entity.editDate) <= 0);
		Assert.assertTrue("Date of edit " + entity.editDate + " must not be before start " + afterEdit, afterEdit.plusSeconds(1).compareTo(entity.editDate) >= 0);
	}

	public static class TestUser {
		public static final TestUser $ = Keys.of(TestUser.class);

		public TestUser() {
			
		}
		
		public TestUser(String login) {
			this.login = login;
		}

		public Integer id;
		
		@Size(255)
		public String login;

	}
	
	public static class TestEntity {
		public static final TestEntity $ = Keys.of(TestEntity.class);

		public Object id;
		public int version;

		@Size(255)
		public String string;

		@TechnicalField(TechnicalFieldType.CREATE_DATE)
		public LocalDateTime createDate;

		@TechnicalField(TechnicalFieldType.CREATE_USER)
		@Size(255)
		public String createUserName;
		
		@TechnicalField(TechnicalFieldType.CREATE_USER)
		public TestUser createUser;

		@TechnicalField(TechnicalFieldType.EDIT_DATE)
		public LocalDateTime editDate;

		@TechnicalField(TechnicalFieldType.EDIT_USER)
		@Size(255)
		public String editUserName;

		@TechnicalField(TechnicalFieldType.EDIT_USER)
		public TestUser editUser;
	}

}
