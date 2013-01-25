package ch.openech.mj.util;

import junit.framework.Assert;

import org.junit.Test;


public class DateUtilsTest {

	@Test
	public void parseCH_01_02_1923() {
		Assert.assertEquals("1923-02-01", DateUtils.parseCH("01.02.1923"));
	}

	@Test
	public void parseCH_1_02_1923() {
		Assert.assertEquals("1923-02-01", DateUtils.parseCH("1.02.1923"));
	}

	@Test
	public void parseCH_1_2_1923() {
		Assert.assertEquals("1923-02-01", DateUtils.parseCH("1.2.1923"));
	}

	@Test
	public void parseCH_1_2_23() {
		Assert.assertEquals("1923-02-01", DateUtils.parseCH("1.2.23"));
	}
	
	@Test
	public void parseCH_7_8_2010() {
		Assert.assertEquals("2010-08-07", DateUtils.parseCH("7.8.2010"));
	}

	@Test
	public void parseCH_10_8_2010() {
		Assert.assertEquals("2010-08-10", DateUtils.parseCH("10.8.2010"));
	}

	@Test
	public void parseCH_10_8_10() {
		Assert.assertEquals("2010-08-10", DateUtils.parseCH("10.8.10"));
	}
	
	@Test
	public void parseCH_1_2_13() {
		Assert.assertEquals("2013-02-01", DateUtils.parseCH("1.2.13"));
	}

	@Test
	public void parseCH_01_02_13() {
		Assert.assertEquals("2013-02-01", DateUtils.parseCH("01.02.13"));
	}

	@Test
	public void parseCH_010213() {
		Assert.assertEquals("2013-02-01", DateUtils.parseCH("010213"));
	}

	@Test
	public void parseCH_010223() {
		Assert.assertEquals("1923-02-01", DateUtils.parseCH("010223"));
	}

	@Test
	public void parseCH_01021923() {
		Assert.assertEquals("1923-02-01", DateUtils.parseCH("01021923"));
	}

	@Test
	public void parseCH_1899() {
		Assert.assertEquals("1899", DateUtils.parseCH("1899"));
	}

	@Test
	public void parseCH_1899_Not_Part() {
		Assert.assertEquals("", DateUtils.parseCH("1899", false));
	}

	@Test
	public void parseCH_1901() {
		Assert.assertEquals("1901-02", DateUtils.parseCH("02.1901"));
	}

	@Test
	public void parseCH_1901_Not_Part() {
		Assert.assertEquals("", DateUtils.parseCH("02.1901", false));
	}
	
	@Test
	public void parseCH_091274() {
		Assert.assertEquals("1974-12-09", DateUtils.parseCH("091274"));
	}
	
	@Test
	public void parseCH_09121974() {
		Assert.assertEquals("1974-12-09", DateUtils.parseCH("09121974"));
	}
}
