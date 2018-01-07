package org.minimalj.rest;

import java.math.BigDecimal;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

public class EntityJsonTest {

	@Test
	public void test() {
		TestClass input = new TestClass();
		input.id = UUID.randomUUID().toString();
		input.s = "test";
		input.integer = 42;
		input.i = 4711;
		input.l = 424242424242L;
		input.b = true;
		input.b2 = null;
		input.primitivBoolean = false;
		input.bigDecimal = new BigDecimal("47.11");
		input.testEnum = TestEnum.B;
		input.testEnums.add(TestEnum.A);
		input.testEnums.add(TestEnum.D);
		String json = EntityJsonWriter.write(input);
		TestClass output = EntityJsonReader.read(TestClass.class, json);
		Assert.assertEquals(input.id, output.id);
		Assert.assertEquals(input.s, output.s);
		Assert.assertEquals(input.integer, output.integer);
		Assert.assertEquals(input.i, output.i);
		Assert.assertEquals(input.l, output.l);
		Assert.assertEquals(input.b, output.b);
		Assert.assertEquals(input.b2, output.b2);
		Assert.assertEquals(input.primitivBoolean, output.primitivBoolean);
		Assert.assertTrue(input.bigDecimal.compareTo(output.bigDecimal) == 0);
		Assert.assertEquals(input.testEnum, output.testEnum);
		Assert.assertEquals(input.testEnums, output.testEnums);
	}
	
	public static class TestClass {
		
		public Object id;
		public String s;
		public Integer integer;
		public int i;
		public Long l;
		public Boolean b;
		public Boolean b2;
		public boolean primitivBoolean;
		public BigDecimal bigDecimal;
		public TestEnum testEnum;
		public Set<TestEnum> testEnums = new TreeSet<>();
	}
	
	public static enum TestEnum {
		A, B, C, D;
	}
}
