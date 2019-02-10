package org.minimalj.frontend.impl.swing.component;

import org.junit.Assert;
import org.junit.Test;
import org.minimalj.frontend.impl.swing.toolkit.SwingFrontend;

public class FxHtmlContentTest {

	@Test
	public void testClassName() {
		Assert.assertEquals(SwingFrontend.FX_HTML_CLASS, FxHtmlContent.class.getName());
	}
}