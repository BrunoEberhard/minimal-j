package org.minimalj.util;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.Assert;
import org.junit.Test;

public class FieldUtilsTest {

	@Test
	public void parse_empty_string() {
		Assert.assertEquals(null, FieldUtils.parse("", Long.class));
		Assert.assertEquals(null, FieldUtils.parse("", Integer.class));
		Assert.assertEquals(null, FieldUtils.parse("", BigDecimal.class));
		Assert.assertEquals(null, FieldUtils.parse("", LocalDate.class));
		Assert.assertEquals("", FieldUtils.parse("", String.class));

		Assert.assertEquals(null, FieldUtils.parse(null, Long.class));
		Assert.assertEquals(null, FieldUtils.parse(null, Integer.class));
		Assert.assertEquals(null, FieldUtils.parse(null, BigDecimal.class));
		Assert.assertEquals(null, FieldUtils.parse(null, LocalDate.class));
		Assert.assertEquals(null, FieldUtils.parse(null, String.class));
	}

	@Test
	public void parse() {
		Assert.assertEquals((Long) 123456789012L, FieldUtils.parse("123456789012", Long.class));
		Assert.assertEquals(123456, FieldUtils.parse("123456", Integer.class));
		Assert.assertEquals(Boolean.TRUE, FieldUtils.parse("true", Boolean.class));
		Assert.assertEquals(null, FieldUtils.parse("", Boolean.class));
		Assert.assertEquals(Boolean.FALSE, FieldUtils.parse("false", Boolean.class));
		Assert.assertEquals(new BigDecimal("123456.78"), FieldUtils.parse("123456.78", BigDecimal.class));
	}

}
