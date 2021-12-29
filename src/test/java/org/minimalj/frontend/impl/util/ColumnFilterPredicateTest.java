package org.minimalj.frontend.impl.util;

import java.time.LocalDate;

import org.junit.Assert;
import org.junit.Test;
import org.minimalj.frontend.impl.util.ColumnFilterPredicate.MinFilterPredicate;
import org.minimalj.util.DateUtils;

public class ColumnFilterPredicateTest {

	@Test
	public void test() {
		MinFilterPredicate predicate = new MinFilterPredicate(LocalDate.class);
	
		predicate.setFilterString("a -");
		Assert.assertFalse(predicate.valid());

		predicate.setFilterString(DateUtils.format(LocalDate.now()) + " -");
		Assert.assertTrue(predicate.valid());

		predicate.setFilterString("a -");
		Assert.assertFalse(predicate.valid());

//		predicate.setFilterString("29g.12.2021 -");
//		Assert.assertFalse(predicate.valid());
	}

}
