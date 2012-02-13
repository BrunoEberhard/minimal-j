package ch.openech.mj.util;

import junit.framework.Assert;

import org.junit.Test;

import ch.openech.mj.util.StringUtils;


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

}
