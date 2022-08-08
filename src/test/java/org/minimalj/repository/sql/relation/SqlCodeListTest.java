package org.minimalj.repository.sql.relation;

import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.application.Application;
import org.minimalj.backend.Backend;
import org.minimalj.model.ViewUtil;
import org.minimalj.repository.query.By;
import org.minimalj.test.TestUtil;
import org.minimalj.util.Codes;

public class SqlCodeListTest {
	
	@BeforeClass
	public static void setupRepository() {
		Application.setInstance(new Application() {
			@Override
			public Class<?>[] getEntityClasses() {
				return new Class<?>[] { TestEntity.class };
			}
		});

		Backend.insert(new TestElementCode(1, 101));
		Backend.insert(new TestElementCode(2, 102));
		Backend.insert(new TestElementCode(3, 103));
	}
	
	@AfterClass
	public static void afterClass() {
		TestUtil.shutdown();
	}
	
	@Test
	public void testInsertAndRead() {
		TestEntity entity = new TestEntity("aName");
		
		entity.codes = new ArrayList<>();
		entity.codes.add(Codes.findCode(TestElementCode.class, 1));
		entity.codes.add(Codes.findCode(TestElementCode.class, 3));
		
		Object id = Backend.insert(entity);
		entity = Backend.read(TestEntity.class, id);
		Assert.assertEquals("Both codes should be read", 2, entity.codes.size());
	}

	@Test(expected = RuntimeException.class)
	public void testNewCode() {
		TestEntity entity = new TestEntity("aName");

		entity.codes = new ArrayList<>();
		TestElementCode code4 = new TestElementCode(4, 104);
		entity.codes.add(code4);

		// Ad hoc creation of new code should fail
		// the id of the new code is already set.
		// But only if a list element has no Id the element is created

		Logger logger = Logger.getLogger("SQL");
		Level oldLevel = logger.getLevel();
		logger.setLevel(Level.OFF);
		Backend.insert(entity);
		logger.setLevel(oldLevel);
	}

	@Test
	public void testInsertAndReadView() {
		TestEntity entity = new TestEntity("aName");

		entity.codeViews = new ArrayList<>();
		entity.codeViews.add(ViewUtil.view(Codes.findCode(TestElementCode.class, 1), new TestElementCodeView()));
		entity.codeViews.add(ViewUtil.view(Codes.findCode(TestElementCode.class, 3), new TestElementCodeView()));

		Object id = Backend.insert(entity);
		entity = Backend.read(TestEntity.class, id);
		Assert.assertEquals("Both codes should be read", 2, entity.codeViews.size());
	}
	
	@Test
	public void testDeleteWithRelation() {
		TestElementB b = new TestElementB();
		b.name = "testInRelationB";
		b = Backend.save(b);
		
		TestEntity entity = new TestEntity();
		entity.list = Collections.singletonList(b);
		entity = Backend.save(entity);
		
		Backend.delete(entity);

		assertNull(Backend.read(TestEntity.class, entity.id));
	}

	@Test
	public void testDeleteWithRelationWhereClause() {
		TestElementB b = new TestElementB();
		b.name = "testInRelationB";
		b = Backend.save(b);
		
		TestEntity entity = new TestEntity();
		entity.name = "testDeleteWithRelationWhereClause";
		entity.list = Collections.singletonList(b);
		entity = Backend.save(entity);
		
		// delete by where clause
		Backend.delete(TestEntity.class, By.field(TestEntity.$.name, "testDeleteWithRelationWhereClause"));
		
		assertNull(Backend.read(TestEntity.class, entity.id));
	}

}