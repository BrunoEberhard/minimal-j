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
		repository = new SqlRepository(DataSourceFactory.embeddedDataSource(), TestEntity.class, TestEntityHistorized.class);
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
		Assert.assertTrue(before.compareTo(entity.createDate) <= 0);
		Assert.assertTrue(after.compareTo(entity.createDate) >= 0);
	}

	@Test
	public void testEdit() {
		TestEntity entity = new TestEntity();
		entity.string = "Testobject";

		Subject subject = new Subject();
		subject.setName("B");
		Subject.setCurrent(subject);

		LocalDateTime before = LocalDateTime.now();

		Object id = repository.insert(entity);
		entity = repository.read(TestEntity.class, id);
		LocalDateTime afterInsert = LocalDateTime.now();

		subject = new Subject();
		subject.setName("C");
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
		Assert.assertTrue(afterInsert.compareTo(entity.editDate) <= 0);
		Assert.assertTrue(afterEdit.compareTo(entity.editDate) >= 0);
	}

	@Test
	public void testCreateHistorized() {
		TestEntityHistorized entity = new TestEntityHistorized();
		entity.string = "Testobject";

		Subject subject = new Subject();
		subject.setName("A");
		Subject.setCurrent(subject);

		LocalDateTime before = LocalDateTime.now();

		Object id = repository.insert(entity);
		entity = repository.read(TestEntityHistorized.class, id);
		LocalDateTime after = LocalDateTime.now();

		Assert.assertEquals("A", entity.createUser);
		Assert.assertTrue(before.compareTo(entity.createDate) <= 0);
		Assert.assertTrue(after.compareTo(entity.createDate) >= 0);
	}

	@Test
	public void testEditHistorized() {
		TestEntityHistorized entity = new TestEntityHistorized();
		entity.string = "Testobject";

		Subject subject = new Subject();
		subject.setName("B");
		Subject.setCurrent(subject);

		LocalDateTime before = LocalDateTime.now();

		Object id = repository.insert(entity);
		entity = repository.read(TestEntityHistorized.class, id);
		LocalDateTime afterInsert = LocalDateTime.now();

		subject = new Subject();
		subject.setName("C");
		Subject.setCurrent(subject);

		entity.string = "Changed";
		repository.update(entity);
		entity = repository.read(TestEntityHistorized.class, id);
		LocalDateTime afterEdit = LocalDateTime.now();

		// create time / user should not be changed
		Assert.assertEquals("B", entity.createUser);
		Assert.assertTrue(before.compareTo(entity.createDate) <= 0);
		Assert.assertTrue(afterInsert.compareTo(entity.createDate) >= 0);

		Assert.assertEquals("C", entity.editUser);
		Assert.assertTrue(afterInsert.compareTo(entity.editDate) <= 0);
		Assert.assertTrue(afterEdit.compareTo(entity.editDate) >= 0);
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

	public static class TestEntityHistorized {
		public static final TestEntityHistorized $ = Keys.of(TestEntityHistorized.class);
		
		public Object id;
		public int version;
		public boolean historized;

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
