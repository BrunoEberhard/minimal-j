package org.minimalj.frontend.swing;

import junit.framework.Assert;

import org.junit.Test;
import org.minimalj.frontend.swing.SwingResourceAction;

public class SwingResourceActionTest {

	@Test
	public void testGetdMnemonic() throws Exception {
		Assert.assertEquals(2, SwingResourceAction.getMnemonicIndex("A&b"));
	}

	@Test
	public void testGetMnemonicIfNoMnemonic() throws Exception {
		Assert.assertEquals(-1, SwingResourceAction.getMnemonicIndex("Ab"));
	}

	@Test
	public void testGetMnemonicForTooShortString() throws Exception {
		Assert.assertEquals(-1, SwingResourceAction.getMnemonicIndex("A"));
	}

	@Test
	public void testGetMnemonicFor2Amp() throws Exception {
		Assert.assertEquals(3, SwingResourceAction.getMnemonicIndex("Ab&c&d"));
	}

	@Test
	public void testGetMnemonicWithEscapedAmp() throws Exception {
		// note: all '&' are counted only as one character
		// This is why the expected values is 5 and not 7
		Assert.assertEquals(5, SwingResourceAction.getMnemonicIndex("Ab'&'c&de"));
	}

}
