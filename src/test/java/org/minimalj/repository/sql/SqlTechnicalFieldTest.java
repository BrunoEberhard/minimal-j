package org.minimalj.repository.sql;

import java.time.LocalDateTime;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;
import org.minimalj.model.annotation.TechnicalField;
import org.minimalj.model.annotation.TechnicalField.TechnicalFieldType;
import org.minimalj.repository.DataSourceFactory;
import org.minimalj.security.Subject;

public class SqlTechnicalFieldTest {

	private static SqlRepository repository;

	@BeforeClass
	public static void setupRepository() {
		repository = new SqlRepository(DataSourceFactory.embeddedDataSource(), TestEntity.class);
	}

	@Test
	public void testCreate() {
		TestEntity entity = new TestEntity();
		entity.string = "Testobject";

		Subject subject = new Subject();
		subject.setName("A");
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

		Subject subject = new Subject();
		subject.setName("B");
		Subject.setCurrent(subject);

		Object id = repository.insert(entity);
		entity = repository.read(TestEntity.class, id);
		LocalDateTime afterInsert = LocalDateTime.now();
		LocalDateTime createDate = entity.createDate;

		subject = new Subject();
		subject.setName("C");
		Subject.setCurrent(subject);

		entity.string = "Changed";
		repository.update(entity);
		entity = repository.read(TestEntity.class, id);
		LocalDateTime afterEdit = LocalDateTime.now();

		Assert.assertEquals("B", entity.createUser);
		Assert.assertTrue("Date of create " + createDate + "must not be changed to " + entity.createDate, createDate.compareTo(entity.createDate) == 0);

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

		@TechnicalField(TechnicalFieldType.CREATE_USER)
		@Size(255)
		public String createUser;

		@TechnicalField(TechnicalFieldType.EDIT_DATE)
		public LocalDateTime editDate;

		@TechnicalField(TechnicalFieldType.EDIT_USER)
		@Size(255)
		public String editUser;

	}

}
