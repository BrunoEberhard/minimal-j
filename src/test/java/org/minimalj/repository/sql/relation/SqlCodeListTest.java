package org.minimalj.repository.sql.relation;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.TestApplication;
import org.minimalj.application.Application;
import org.minimalj.backend.Backend;
import org.minimalj.model.ViewUtil;
import org.minimalj.util.Codes;

public class SqlCodeListTest {
	
	@BeforeClass
	public static void setupRepository() {
		Application.setInstance(TestApplication.INSTANCE);

		TestApplication.INSTANCE.setCurrentApplication(new Application() {
			@Override
			public Class<?>[] getEntityClasses() {
				return new Class<?>[] { TestEntity.class };
			}
		});

		Backend.insert(new TestElementCode(1, 101));
		Backend.insert(new TestElementCode(2, 102));
		Backend.insert(new TestElementCode(3, 103));
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

}