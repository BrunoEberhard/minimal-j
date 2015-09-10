package org.minimalj.util;

import org.junit.Assert;
import org.junit.Test;


public class StringUtilsTest {

	@Test
	public void isBlank_A() {
		Assert.assertFalse(StringUtils.isBlank("A"));
		Assert.assertFalse(StringUtils.isBlank(" A"));
		Assert.assertFalse(StringUtils.isBlank(" A "));
		Assert.assertFalse(StringUtils.isBlank(" AA "));
	}

	@Test
	public void isBlank_Blank() {
		Assert.assertTrue(StringUtils.isBlank(null));
		Assert.assertTrue(StringUtils.isBlank(""));
		Assert.assertTrue(StringUtils.isBlank(" "));
		Assert.assertTrue(StringUtils.isBlank("\t"));
		Assert.assertTrue(StringUtils.isBlank("\n"));
	}

	@Test
	public void isEmpty_A() {
		Assert.assertFalse(StringUtils.isBlank("A"));
		Assert.assertFalse(StringUtils.isBlank(" A"));
		Assert.assertFalse(StringUtils.isBlank(" A "));
		Assert.assertFalse(StringUtils.isBlank(" AA "));
	}

	@Test
	public void isEmpty_Blank() {
		Assert.assertTrue(StringUtils.isEmpty(null));
		Assert.assertTrue(StringUtils.isEmpty(""));
		Assert.assertFalse(StringUtils.isEmpty(" "));
		Assert.assertFalse(StringUtils.isEmpty("\t"));
		Assert.assertFalse(StringUtils.isEmpty("\n"));
	}
	
	@Test
	public void upperFirstChar() {
		Assert.assertEquals("Der", StringUtils.upperFirstChar("der"));
		Assert.assertEquals("Der", StringUtils.upperFirstChar("Der"));
		Assert.assertEquals("D", StringUtils.upperFirstChar("d"));
		Assert.assertEquals("D", StringUtils.upperFirstChar("D"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void upperFirstCharEmpty() {
		Assert.assertEquals("", StringUtils.upperFirstChar(""));
	}

	@Test(expected = NullPointerException.class)
	public void upperFirstCharNull() {
		Assert.assertEquals(null, StringUtils.upperFirstChar(null));
	}
	
	@Test
	public void toConstant() {
		Assert.assertEquals("DER", StringUtils.toConstant("der"));
		Assert.assertEquals("_DER", StringUtils.toConstant("Der"));
		Assert.assertEquals("_D_ER", StringUtils.toConstant("DEr"));
		Assert.assertEquals("D_ER", StringUtils.toConstant("dEr"));
		Assert.assertEquals("D_E_ER", StringUtils.toConstant("dEEr"));
		Assert.assertEquals("D__ER", StringUtils.toConstant("d_er"));
		Assert.assertEquals("D___ER", StringUtils.toConstant("d_Er"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void toConstantEmpty() {
		Assert.assertEquals("", StringUtils.toConstant(""));
	}

	@Test(expected = NullPointerException.class)
	public void toConstantNull() {
		Assert.assertEquals(null, StringUtils.toConstant(null));
	}
	
	@Test
	public void isUrl() {
		Assert.assertTrue(StringUtils.isUrl("http://www.notrealaddress.com/"));
		Assert.assertTrue(StringUtils.isUrl("https://www.notrealaddress.com/"));
	}

	@Test
	public void isHtml() {
		Assert.assertFalse(StringUtils.isUrl("<html><body>hi</body></html>"));
		Assert.assertFalse(StringUtils.isUrl("Hi"));
	}
}
