package org.minimalj.model;

import junit.framework.Assert;

import org.junit.Test;

public class EnumHackTest {

	@Test
	public void testGenerateEnum() {
		Assert.assertEquals(2, EnumHackTestEnum.values().length);
		EnumHackTestEnum generatedEnum = EnumUtils.createEnum(EnumHackTestEnum.class, "c");
		EnumUtils.createEnum(EnumHackTestEnum.class, "d");
		EnumHackTestEnum generatedEnum_with_same_name = EnumUtils.createEnum(EnumHackTestEnum.class, "c");
		Assert.assertNotSame(generatedEnum, generatedEnum_with_same_name);
	}
	
	@Test
	public void testResources() {
		EnumHackTestEnum e = EnumHackTestEnum.a;
		Assert.assertEquals(EnumUtils.getText(e), "Test");
	}
}
