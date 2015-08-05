package org.minimalj.model.validation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.Assert;
import org.junit.Test;

public class InvalidValuesTest {

	@Test
	public void testIntegerInvalidValue() throws Exception {
		Integer value = InvalidValues.createInvalidInteger("Test1");
		Assert.assertTrue(InvalidValues.isInvalid(value));
		Assert.assertEquals("Test1", InvalidValues.getInvalidValue(value));
	}

	@Test
	public void testLongInvalidValue() throws Exception {
		Long value = InvalidValues.createInvalidLong("Test2");
		Assert.assertTrue(InvalidValues.isInvalid(value));
		Assert.assertEquals("Test2", InvalidValues.getInvalidValue(value));
	}

	@Test
	public void testBigDecimalInvalidValue() throws Exception {
		BigDecimal value = InvalidValues.createInvalidBigDecimal("Test3");
		Assert.assertTrue(InvalidValues.isInvalid(value));
		Assert.assertEquals("Test3", InvalidValues.getInvalidValue(value));
	}

	@Test
	public void testLocalDateInvalidValue() throws Exception {
		LocalDate value = InvalidValues.createInvalidLocalDate("Test5");
		Assert.assertTrue(InvalidValues.isInvalid(value));
		Assert.assertEquals("Test5", InvalidValues.getInvalidValue(value));
	}

	@Test
	public void testLocalTimeInvalidValue() throws Exception {
		LocalTime value = InvalidValues.createInvalidLocalTime("Test5");
		Assert.assertTrue(InvalidValues.isInvalid(value));
		Assert.assertEquals("Test5", InvalidValues.getInvalidValue(value));
	}

	@Test
	public void testLocalDateTimeInvalidValue() throws Exception {
		LocalDateTime value = InvalidValues.createInvalidLocalDateTime("Test6");
		Assert.assertTrue(InvalidValues.isInvalid(value));
		Assert.assertEquals("Test6", InvalidValues.getInvalidValue(value));
	}

	@Test
	public void testEnumInvalidValue() throws Exception {
		TestEnum value = InvalidValues.createInvalidEnum(TestEnum.class, "Test7");
		Assert.assertTrue(InvalidValues.isInvalid(value));
		Assert.assertEquals("Test7", InvalidValues.getInvalidValue(value));
	}

	@Test
	public void testEnumInvalidValues() throws Exception {
		TestEnum value = InvalidValues.createInvalidEnum(TestEnum.class, "Test8a");
		Assert.assertTrue(InvalidValues.isInvalid(value));
		Assert.assertEquals("Test8a", InvalidValues.getInvalidValue(value));

		TestEnum value2 = InvalidValues.createInvalidEnum(TestEnum.class, "Test8b");
		Assert.assertTrue(InvalidValues.isInvalid(value2));
		Assert.assertEquals("Test8b", InvalidValues.getInvalidValue(value2));
	}
	
	public static enum TestEnum {
		A;
	}
	
	@Test
	public void testIntegerInvalidValues() throws Exception {
		Integer[] values = new Integer[20];
		for (int i = 0; i<20; i++) {
			values[i] = InvalidValues.createInvalidInteger("Test9_" + i);
		}
		for (int i = 0; i<20; i++) {
			Assert.assertTrue(InvalidValues.isInvalid(values[i]));
			Assert.assertEquals("Test9_" + i, InvalidValues.getInvalidValue(values[i]));
		}
	}

}
