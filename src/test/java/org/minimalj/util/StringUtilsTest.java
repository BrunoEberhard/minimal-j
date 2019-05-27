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
	public void emptyIfNull() {
		Assert.assertEquals("", StringUtils.emptyIfNull(null));
		Assert.assertEquals("", StringUtils.emptyIfNull(""));
		Assert.assertEquals("a", StringUtils.emptyIfNull("a"));
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
	public void stripHtml() {
		Assert.assertEquals(null, StringUtils.stripHtml(null));
		Assert.assertEquals("", StringUtils.stripHtml(""));
		Assert.assertEquals("s", StringUtils.stripHtml("s"));
		Assert.assertEquals("text bold", StringUtils.stripHtml("text <b>bold</b>"));
		Assert.assertEquals("bold text", StringUtils.stripHtml("<b>bold</b> text"));
		Assert.assertEquals("bold", StringUtils.stripHtml("<b>bold</b>"));
		Assert.assertEquals("bold special bold2", StringUtils.stripHtml("<b>bold <x>special<x> bold2</b>"));
	}

}
