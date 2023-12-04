package org.minimalj.repository.sql;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;

// just to check that a field named position is no problem
public class SqlColumnPositionTest extends SqlTest {
	
	@Override
	public Class<?>[] getEntityClasses() {
		return new Class<?>[] { TestEntity.class };
	}
	
	@Test
	public void test() {
		TestEntity entity = new TestEntity();
		TestElement element = new TestElement();
		element.name = "Name";
		element.position = 42;
		entity.elements.add(element);
		Object id = repository.insert(entity);
		
		TestEntity readEntity = repository.read(TestEntity.class, id);
		assertEquals((Integer) 42, readEntity.elements.get(0).position);
		assertEquals("Name", readEntity.elements.get(0).name);
	}

	public static class TestEntity {
		public static final TestEntity $ = Keys.of(TestEntity.class);
		
		public Object id;
		
		public List<TestElement> elements = new ArrayList<>();
	}

	public static class TestElement {
		public static final TestElement $ = Keys.of(TestElement.class);
		
		public Integer position;
		
		@Size(255)
		public String name;
	}

}
