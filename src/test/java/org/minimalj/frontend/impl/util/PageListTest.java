package org.minimalj.frontend.impl.util;

import org.junit.Assert;
import org.junit.Test;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.page.Page;

public class PageListTest {

	@Test
	public void putTest() {
		PageList list = new PageList();
		TestPage testPage = new TestPage();
		list.put("putTest", testPage);
		Assert.assertTrue(list.contains(testPage));
	}
	
	@Test
	public void getIdTest() {
		PageList list = new PageList();
		list.put("page0", new TestPage());
		list.put("page1", new TestPage());
		list.put("page2", new TestPage());
		Assert.assertTrue(list.getId(1).equals("page1"));
	}
	
	@Test
	public void clearTest() {
		PageList list = new PageList();
		TestPage testPage0 = new TestPage();
		list.put("page0", testPage0);
		TestPage testPage1 = new TestPage();
		list.put("page1", testPage1);
		TestPage testPage2 = new TestPage();
		list.put("page2", testPage2);
		list.clear();
		Assert.assertFalse(list.contains(testPage0));
		Assert.assertFalse(list.contains(testPage1));
		Assert.assertFalse(list.contains(testPage2));
	}
	
	@Test
	public void removeAllFromByPageTest() {
		PageList list = new PageList();
		list.put("page0", new TestPage());
		TestPage testPage1 = new TestPage();
		list.put("page1", testPage1);
		TestPage testPage2 = new TestPage();
		list.put("page2", testPage2);
		list.removeAllFrom(testPage1);
		Assert.assertFalse(list.contains(testPage1));
		Assert.assertFalse(list.contains(testPage2));
	}
	
	@Test
	public void removeAllFromByIdTest() {
		PageList list = new PageList();
		list.put("page0", new TestPage());
		TestPage testPage1 = new TestPage();
		list.put("page1", testPage1);
		TestPage testPage2 = new TestPage();
		list.put("page2", testPage2);
		list.removeAllFrom("page1");
		Assert.assertFalse(list.contains(testPage1));
		Assert.assertFalse(list.contains(testPage2));
	}

	private static class TestPage implements Page {
		@Override
		public IContent getContent() {
			return null;
		}
	}
}
