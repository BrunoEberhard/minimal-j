package org.minimalj.resources;

import java.util.Locale;
import java.util.ResourceBundle;

import org.junit.Assert;
import org.junit.Test;

public class ApplicationResourceBundleTest {

	@Test
	public void propertiesOfParent() {
		ResourceTestApplication2 application2 = new ResourceTestApplication2();
		ResourceBundle bundle = application2.getResourceBundle(Locale.getDefault());
		Assert.assertEquals("in2", bundle.getString("Property1and2"));
		Assert.assertEquals("Only1", bundle.getString("PropertyOnly1"));
	}
}
