package org.minimalj.repository.sql;

import java.time.LocalDateTime;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.application.Application;
import org.minimalj.backend.Backend;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;
import org.minimalj.model.annotation.TechnicalField;
import org.minimalj.model.annotation.TechnicalField.TechnicalFieldType;
import org.minimalj.security.Subject;
import org.minimalj.security.model.User;
import org.minimalj.test.TestUtil;

public class SqlTechnicalFieldTest {

	@BeforeClass
	public static void initializeFronted() {
		// User is a Code. Codes need an initialized Application
		Application.setInstance(new Application() {
			@Override
			public Class<?>[] getEntityClasses() {
				return new Class<?>[] { TestEntity.class, User.class };
			}
		});
	}
	
	@AfterClass
	public static void shutdown() {
		TestUtil.shutdown();
	}
	
	private User createTestUser(String name) {
		User user = new User();
		user.name = name;
		user.id = Backend.insert(user);
		return user;
	}
	
	@Test
	public void testCreate() {
		TestEntity entity = new TestEntity();
		entity.string = "Testobject";

		User testUserA = createTestUser("A");
				
		Subject subject = new Subject(testUserA);
		Subject.setCurrent(subject);

		LocalDateTime before = LocalDateTime.now();

		Object id = Backend.insert(entity);
		entity = Backend.read(TestEntity.class, id);
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
		
		User testUserB = createTestUser("B");
		
		User testUserC = createTestUser("C");

		Subject subject = new Subject(testUserB);
		Subject.setCurrent(subject);

		Object id = Backend.insert(entity);
		entity = Backend.read(TestEntity.class, id);
		LocalDateTime afterInsert = LocalDateTime.now();
		LocalDateTime createDate = entity.createDate;

		subject = new Subject(testUserC);
		Subject.setCurrent(subject);

		entity.string = "Changed";
		Backend.update(entity);
		entity = Backend.read(TestEntity.class, id);
		LocalDateTime afterEdit = LocalDateTime.now();

		Assert.assertEquals(testUserB.id, entity.createUser.id);
		Assert.assertEquals("B", entity.createUserName);
		Assert.assertTrue("Date of create " + createDate + "must not be changed to " + entity.createDate, createDate.compareTo(entity.createDate) == 0);

		Assert.assertEquals(testUserC.id, entity.editUser.id);
		Assert.assertEquals("C", entity.editUserName);
		Assert.assertTrue("Date of edit " + entity.editDate + " must not be after start " + afterInsert, afterInsert.minusSeconds(1).compareTo(entity.editDate) <= 0);
		Assert.assertTrue("Date of edit " + entity.editDate + " must not be before start " + afterEdit, afterEdit.plusSeconds(1).compareTo(entity.editDate) >= 0);
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
		public User createUser;

		@TechnicalField(TechnicalFieldType.EDIT_DATE)
		public LocalDateTime editDate;

		@TechnicalField(TechnicalFieldType.EDIT_USER)
		@Size(255)
		public String editUserName;

		@TechnicalField(TechnicalFieldType.EDIT_USER)
		public User editUser;
	}

}
