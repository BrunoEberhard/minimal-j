package org.minimalj.util;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

public class CsvReaderTest {

	@Test
	public void testReadFile() throws Exception {
		PushbackReader reader = reader("a,b\na,b");
		List<String> values = CsvReader.readRecord(reader);
		Assert.assertTrue(isAB(values));
		values = CsvReader.readRecord(reader);
		Assert.assertTrue(isAB(values));
	}
	
	@Test
	public void testReadRecord() throws Exception {
		PushbackReader reader = reader("a,b");
		List<String> values = CsvReader.readRecord(reader);
		Assert.assertTrue(isAB(values));
		
		reader = reader("a,\"b\"");
		values = CsvReader.readRecord(reader);
		Assert.assertTrue(isAB(values));
	}
	
	private boolean isAB(List<String> strings) {
		if (strings.size() != 2) return false;
		return strings.get(0).equals("a") && strings.get(1).equals("b");
	}
	
	@Test
	public void testReadField() throws Exception {
		PushbackReader reader = reader("\"ab\"");
		Assert.assertEquals("ab", CsvReader.readField(reader));
		
		reader = reader("\"ab\"\n");
		Assert.assertEquals("ab", CsvReader.readField(reader));

		reader = reader("\"ab\",");
		Assert.assertEquals("ab", CsvReader.readField(reader));
		
		reader = reader("ab");
		Assert.assertEquals("ab", CsvReader.readField(reader));
		
		reader = reader("ab\n");
		Assert.assertEquals("ab", CsvReader.readField(reader));
		
		reader = reader("ab,");
		Assert.assertEquals("ab", CsvReader.readField(reader));
	}
	
	
	@Test
	public void testReadEscaped() throws Exception {
		PushbackReader reader = reader("\"ab\"");
		Assert.assertEquals("ab", CsvReader.readEscaped(reader));
		
		reader = reader("\"ab\"\n");
		Assert.assertEquals("ab", CsvReader.readEscaped(reader));

		reader = reader("\"ab\",");
		Assert.assertEquals("ab", CsvReader.readEscaped(reader));
	}
	
	@Test
	public void testReadNonEscaped() throws Exception {
		PushbackReader reader = reader("ab");
		Assert.assertEquals("ab", CsvReader.readNonEscaped(reader));
		
		reader = reader("ab\n");
		Assert.assertEquals("ab", CsvReader.readNonEscaped(reader));
		
		reader = reader("ab,");
		Assert.assertEquals("ab", CsvReader.readNonEscaped(reader));
	}
	
	private static PushbackReader reader(String s) {
		ByteArrayInputStream bais = new ByteArrayInputStream(s.getBytes());
		return new PushbackReader(new InputStreamReader(bais));
	}
}
