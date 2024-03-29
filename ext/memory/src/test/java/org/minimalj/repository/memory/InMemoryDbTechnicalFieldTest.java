package org.minimalj.repository.memory;

import java.time.LocalDateTime;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;
import org.minimalj.model.annotation.TechnicalField;
import org.minimalj.model.annotation.TechnicalField.TechnicalFieldType;
import org.minimalj.security.Subject;

public class InMemoryDbTechnicalFieldTest {

	private static InMemoryRepository repository;

	@BeforeClass
	public static void setupRepository() {
		repository = new InMemoryRepository(TestEntity.class);
	}

	@Test
	public void testCreate() {
		TestEntity entity = new TestEntity();
		entity.string = "Testobject";

		Subject subject = new Subject("A");
		Subject.setCurrent(subject);

		LocalDateTime before = LocalDateTime.now();

		Object id = repository.insert(entity);
		entity = repository.read(TestEntity.class, id);
		LocalDateTime after = LocalDateTime.now();

		Assert.assertEquals("A", entity.createUser);
		Assert.assertTrue("Date of create " + entity.createDate + " must not be before start " + before, before.minusSeconds(1).compareTo(entity.createDate) <= 0);
		Assert.assertTrue("Date of create " + entity.createDate + " must not be after now " + after, after.plusSeconds(1).compareTo(entity.createDate) >= 0);
	}

	@Test
	public void testEdit() {
		TestEntity entity = new TestEntity();
		entity.string = "Testobject";

		Subject subject = new Subject("B");
		Subject.setCurrent(subject);

		LocalDateTime before = LocalDateTime.now();

		Object id = repository.insert(entity);
		entity = repository.read(TestEntity.class, id);
		LocalDateTime afterInsert = LocalDateTime.now();

		subject = new Subject("C");
		Subject.setCurrent(subject);

		entity.string = "Changed";
		repository.update(entity);
		entity = repository.read(TestEntity.class, id);
		LocalDateTime afterEdit = LocalDateTime.now();

		// create time / user should not be changed
		Assert.assertEquals("B", entity.createUser);
		Assert.assertTrue(before.compareTo(entity.createDate) <= 0);
		Assert.assertTrue(afterInsert.compareTo(entity.createDate) >= 0);

		Assert.assertEquals("C", entity.editUser);
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
		
		@TechnicalField(TechnicalFieldType.CREATE_USER) @Size(255)
		public String createUser;

		@TechnicalField(TechnicalFieldType.EDIT_DATE)
		public LocalDateTime editDate;
		
		@TechnicalField(TechnicalFieldType.EDIT_USER) @Size(255)
		public String editUser;

	}
	
}
