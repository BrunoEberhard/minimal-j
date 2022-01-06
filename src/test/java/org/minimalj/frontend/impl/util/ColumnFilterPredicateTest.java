package org.minimalj.frontend.impl.util;

import java.time.LocalDate;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.application.Application;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.impl.json.JsonFrontend;
import org.minimalj.frontend.impl.util.ColumnFilterPredicate.MinFilterPredicate;
import org.minimalj.test.TestUtil;
import org.minimalj.util.DateUtils;

public class ColumnFilterPredicateTest {

	@BeforeClass
	public static void initializeFronted() {
		Application.setInstance(new Application() {});
		Frontend.setInstance(new JsonFrontend());
	}
	
	@AfterClass
	public static void shutdown() {
		TestUtil.shutdown();
	}
	
	@Test
	public void test() {
		MinFilterPredicate predicate = new MinFilterPredicate(LocalDate.class);
	
		predicate.setFilterString("> a");
		Assert.assertFalse(predicate.valid());

		predicate.setFilterString("> " + DateUtils.format(LocalDate.now()));
		Assert.assertTrue(predicate.valid());
	}

}
