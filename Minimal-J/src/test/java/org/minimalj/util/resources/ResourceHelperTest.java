package org.minimalj.util.resources;

import junit.framework.Assert;

import org.junit.Test;

public class ResourceHelperTest {

	@Test
	public void testGetdMnemonic() throws Exception {
		Assert.assertEquals(2, ResourceHelper.getMnemonicIndex("A&b"));
	}

	@Test
	public void testGetMnemonicIfNoMnemonic() throws Exception {
		Assert.assertEquals(-1, ResourceHelper.getMnemonicIndex("Ab"));
	}

	@Test
	public void testGetMnemonicForTooShortString() throws Exception {
		Assert.assertEquals(-1, ResourceHelper.getMnemonicIndex("A"));
	}

	@Test
	public void testGetMnemonicFor2Amp() throws Exception {
		Assert.assertEquals(3, ResourceHelper.getMnemonicIndex("Ab&c&d"));
	}

	@Test
	public void testGetMnemonicWithEscapedAmp() throws Exception {
		// note: all '&' are counted only as one character
		// This is why the expected values is 5 and not 7
		Assert.assertEquals(5, ResourceHelper.getMnemonicIndex("Ab'&'c&de"));
	}

}
