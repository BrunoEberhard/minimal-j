package org.minimalj.frontend.form.element;

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Test;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Decimal;
import org.minimalj.model.annotation.Signed;
import org.minimalj.model.annotation.Size;
import org.minimalj.model.validation.InvalidValues;

public class NumberFormatElementTest {

	// Integer
	
	@Test
	public void testInteger() {
		IntegerFormElement element = new IntegerFormElement(Keys.getProperty(TestNumbers.$.anInteger), true);
		Assert.assertEquals((Integer) 123, element.parse("123"));
		Assert.assertTrue("Values larger than 2^31 should parsed as invalid", InvalidValues.isInvalid(element.parse("3234567890")));
		Assert.assertTrue("Negative values should parsed as invalid", InvalidValues.isInvalid(element.parse("-3")));
	}
	
	@Test
	public void testNegativeInteger() {
		IntegerFormElement element = new IntegerFormElement(Keys.getProperty(TestNumbers.$.aSignedInteger), true);
		Assert.assertEquals((Integer) 123, element.parse("123"));
		Assert.assertEquals(Integer.valueOf(-3), element.parse("-3"));
	}

	@Test
	public void testNegativeIntegerOfSize3() {
		IntegerFormElement element = new IntegerFormElement(Keys.getProperty(TestNumbers.$.aSignedIntegerOfSize3), true);
		Assert.assertEquals((Integer) 123, element.parse("123"));
		Assert.assertEquals(Integer.valueOf(-3), element.parse("-3"));
		Assert.assertTrue("Values larger than size 3 should be invalid", InvalidValues.isInvalid(element.parse("1234")));
	}
	
	// Long
	
	@Test
	public void testLong() {
		LongFormElement element = new LongFormElement(Keys.getProperty(TestNumbers.$.aLong), true);
		Assert.assertEquals((Long) 123L, element.parse("123"));
		Assert.assertEquals((Long) 12345678901234L, element.parse("12345678901234"));
		Assert.assertTrue("Values larger than 2^63 should parsed as invalid", InvalidValues.isInvalid(element.parse("12345678901234567890")));
		Assert.assertTrue("Negative values should parsed as invalid", InvalidValues.isInvalid(element.parse("-3")));
	}
	
	@Test
	public void testNegativeLong() {
		LongFormElement element = new LongFormElement(Keys.getProperty(TestNumbers.$.aSignedLong), true);
		Assert.assertEquals((Long) 123L, element.parse("123"));
		Assert.assertEquals(Long.valueOf(-3), element.parse("-3"));
	}

	@Test
	public void testNegativeLongOfSize3() {
		LongFormElement element = new LongFormElement(Keys.getProperty(TestNumbers.$.aSignedLongOfSize3), true);
		Assert.assertEquals((Long) 123L, element.parse("123"));
		Assert.assertEquals(Long.valueOf(-3), element.parse("-3"));
		Assert.assertTrue("Values larger than size 3 should be invalid", InvalidValues.isInvalid(element.parse("1234")));
	}
	
	// BigDecimal
	
	@Test
	public void testBigDecimal() {
		BigDecimalFormElement element = new BigDecimalFormElement(Keys.getProperty(TestNumbers.$.aBigDecimal), true);
		Assert.assertTrue(BigDecimal.valueOf(123).compareTo(element.parse("123")) == 0);
		Assert.assertTrue("Values larger than 10 digits should be parsed as invalid", InvalidValues.isInvalid(element.parse("12345678901234L")));
		Assert.assertTrue("Negative values should parsed as invalid", InvalidValues.isInvalid(element.parse("-3")));
	}
	
	@Test
	public void testNegativeBigDecimal() {
		BigDecimalFormElement element = new BigDecimalFormElement(Keys.getProperty(TestNumbers.$.aSignedBigDecimal), true);
		Assert.assertTrue(BigDecimal.valueOf(123).compareTo(element.parse("123")) == 0);
		Assert.assertTrue(BigDecimal.valueOf(-1234567890).compareTo(element.parse("-1234567890")) == 0);
	}

	@Test
	public void testNegativeBigDecimalWith2Decimals() {
		BigDecimalFormElement element = new BigDecimalFormElement(Keys.getProperty(TestNumbers.$.aBigDecimalOfSize3With2Decimals), true);
		Assert.assertTrue(BigDecimal.valueOf(123).compareTo(element.parse("123")) == 0);
		Assert.assertTrue(BigDecimal.valueOf(123).compareTo(element.parse("123.")) == 0);
		Assert.assertTrue(BigDecimal.valueOf(123).compareTo(element.parse("123.0")) == 0);
		Assert.assertTrue(BigDecimal.valueOf(12.3).compareTo(element.parse("12.3")) == 0);		
		Assert.assertTrue(BigDecimal.valueOf(0.3).compareTo(element.parse(".3")) == 0);		
		Assert.assertTrue("Too many decimal places should be parsed as invalid", InvalidValues.isInvalid(element.parse("1.234")));
		Assert.assertTrue("Too high precision should be parsed as invalid", InvalidValues.isInvalid(element.parse("12.34")));
		Assert.assertTrue("Too high precision should be parsed as invalid", InvalidValues.isInvalid(element.parse("123.4")));
		Assert.assertTrue("Too high precision should be parsed as invalid", InvalidValues.isInvalid(element.parse("1234.0")));
	}
	
	public static class TestNumbers {

		public static final TestNumbers $ = Keys.of(TestNumbers.class);
		
		public Integer anInteger;
		
		@Signed
		public Integer aSignedInteger;

		@Size(3) @Signed
		public Integer aSignedIntegerOfSize3;

		
		public Long aLong;
		
		@Signed
		public Long aSignedLong;

		@Size(3) @Signed
		public Long aSignedLongOfSize3;


		public BigDecimal aBigDecimal;

		@Signed
		public BigDecimal aSignedBigDecimal;

		@Size(3) @Decimal(2)
		public BigDecimal aBigDecimalOfSize3With2Decimals;
	}
	
}
