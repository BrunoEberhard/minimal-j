package org.minimalj.util;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.application.Application;
import org.minimalj.backend.Backend;
import org.minimalj.model.Code;

public class CodesSelfReferenceTest {

	@BeforeClass
	public static void initApplication() {
		Application.setInstance(new Application() {
			@Override
			public Class<?>[] getEntityClasses() {
				return new Class<?>[] { TestSelfReferenceCode.class };
			}
		});
	}
	
	@Test
	public void testSelfReference() {
		TestSelfReferenceCode code = new TestSelfReferenceCode();
		code.id = (Integer) Backend.insert(code);
		code.reference = code;
		Backend.update(code);
	
		TestSelfReferenceCode readCode = Backend.read(TestSelfReferenceCode.class, code.id);
		Assert.assertEquals(readCode.id, readCode.reference.id);
		
		Codes.getCache().invalidateCodeCache(TestSelfReferenceCode.class);
		readCode = Codes.get(TestSelfReferenceCode.class, code.id);
		Assert.assertEquals(readCode.id, readCode.reference.id);
	}
	
	public static class TestSelfReferenceCode implements Code {
	
		public Integer id;
	
		public TestSelfReferenceCode reference;
	}
		
}
