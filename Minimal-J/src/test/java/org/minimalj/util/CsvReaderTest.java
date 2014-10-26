package org.minimalj.util;

import java.io.ByteArrayInputStream;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

public class CsvReaderTest {

	@Test
	public void testReadFile() throws Exception {
		CsvReader reader = reader("a,b\na,b");
		List<String> values = reader.readRecord();
		Assert.assertTrue(isAB(values));
		values = reader.readRecord();
		Assert.assertTrue(isAB(values));
	}
	
	@Test
	public void testReadRecord() throws Exception {
		CsvReader reader = reader("a,b");
		List<String> values = reader.readRecord();
		Assert.assertTrue(isAB(values));
		
		reader = reader("a,\"b\"");
		values = reader.readRecord();
		Assert.assertTrue(isAB(values));
	}
	
	private boolean isAB(List<String> strings) {
		if (strings.size() != 2) return false;
		return strings.get(0).equals("a") && strings.get(1).equals("b");
	}
	
	@Test
	public void testReadField() throws Exception {
		CsvReader reader = reader("\"ab\"");
		Assert.assertEquals("ab", reader.readField());
		
		reader = reader("\"ab\"\n");
		Assert.assertEquals("ab", reader.readField());

		reader = reader("\"ab\",");
		Assert.assertEquals("ab", reader.readField());
		
		reader = reader("ab");
		Assert.assertEquals("ab", reader.readField());
		
		reader = reader("ab\n");
		Assert.assertEquals("ab", reader.readField());
		
		reader = reader("ab,");
		Assert.assertEquals("ab", reader.readField());
	}
	
	
	@Test
	public void testReadEscaped() throws Exception {
		CsvReader reader = reader("\"ab\"");
		Assert.assertEquals("ab", reader.readEscaped());
		
		reader = reader("\"ab\"\n");
		Assert.assertEquals("ab", reader.readEscaped());

		reader = reader("\"ab\",");
		Assert.assertEquals("ab", reader.readEscaped());
	}
	
	@Test
	public void testReadNonEscaped() throws Exception {
		CsvReader reader = reader("ab");
		Assert.assertEquals("ab", reader.readNonEscaped());
		
		reader = reader("ab\n");
		Assert.assertEquals("ab", reader.readNonEscaped());
		
		reader = reader("ab,");
		Assert.assertEquals("ab", reader.readNonEscaped());
	}
	
	private static CsvReader reader(String s) {
		ByteArrayInputStream bais = new ByteArrayInputStream(s.getBytes());
		return new CsvReader(bais);
	}
}
