package org.minimalj.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Base64;

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
		Assert.assertEquals(null, FieldUtils.parse("", byte[].class));

		Assert.assertEquals(null, FieldUtils.parse(null, Long.class));
		Assert.assertEquals(null, FieldUtils.parse(null, Integer.class));
		Assert.assertEquals(null, FieldUtils.parse(null, BigDecimal.class));
		Assert.assertEquals(null, FieldUtils.parse(null, LocalDate.class));
		Assert.assertEquals(null, FieldUtils.parse(null, String.class));
		Assert.assertEquals(null, FieldUtils.parse(null, byte[].class));
	}

	@Test
	public void parse_primitives() {
		Assert.assertEquals((Long) 123456789012L, FieldUtils.parse("123456789012", Long.class));
		Assert.assertEquals(Integer.valueOf(123456), FieldUtils.parse("123456", Integer.class));
		Assert.assertEquals(Boolean.TRUE, FieldUtils.parse("true", Boolean.class));
		Assert.assertEquals(null, FieldUtils.parse("", Boolean.class));
		Assert.assertEquals(Boolean.FALSE, FieldUtils.parse("false", Boolean.class));
	}

	@Test
	public void parse_BigDecimal() {
		Assert.assertEquals(new BigDecimal("123456.78"), FieldUtils.parse("123456.78", BigDecimal.class));
	}
	
	@Test
	public void parse_date() {
		Assert.assertEquals(LocalDate.of(2014, 12, 31), FieldUtils.parse("2014-12-31", LocalDate.class));
	}

	@Test
	public void parse_time() {
		Assert.assertEquals(LocalTime.of(23, 59, 58), FieldUtils.parse("23:59:58", LocalTime.class));
	}

	@Test
	public void parse_dateTime() {
		Assert.assertEquals(LocalDateTime.of(2014, 12, 31, 23, 59, 58), FieldUtils.parse("2014-12-31T23:59:58", LocalDateTime.class));
	}

	@Test
	public void parse_base64() {
		byte[] bytes = new byte[] { 1, 2, 3, 4 };
		String s = Base64.getEncoder().encodeToString(bytes);
		Assert.assertArrayEquals(bytes, FieldUtils.parse(s, byte[].class));
	}
}
