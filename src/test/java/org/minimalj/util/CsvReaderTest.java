package org.minimalj.util;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.Assert;
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
		if (strings.size() != 2)
			return false;
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

		reader = reader(" ab,");
		Assert.assertEquals("ab", reader.readField());

		reader = reader("  ab,");
		Assert.assertEquals("ab", reader.readField());

		reader = reader("ab ,");
		Assert.assertEquals("ab", reader.readField());

		reader = reader(" \"ab\",");
		Assert.assertEquals("ab", reader.readField());

		reader = reader("\t\"ab\"\t,");
		Assert.assertEquals("ab", reader.readField());
	}

	@Test
	public void testReadEscaped() throws Exception {
		CsvReader reader = reader("\"ab\"");
		Assert.assertEquals("ab", reader.readEscaped());

		reader = reader("\"ab\"\n");
		Assert.assertEquals("ab", reader.readEscaped());

		reader = reader("\"ab\"");
		Assert.assertEquals("ab", reader.readEscaped());

		reader = reader("\"a,b\"");
		Assert.assertEquals("a,b", reader.readEscaped());

		reader = reader("\"a\"\"b\"");
		Assert.assertEquals("a\"b", reader.readEscaped());
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

	@Test
	public void testReadFields() throws Exception {
		CsvReader reader = reader("i,l,bd,s, ld\n142, 123456789012345, 2.1, s, " + LocalDate.of(2012, 3, 4));
		List<CsvReaderTestA> result = reader.readValues(CsvReaderTestA.class);
		checkResult(result);
	}

	private void checkResult(List<CsvReaderTestA> result) {
		Assert.assertEquals(1, result.size());
		Assert.assertEquals((long) 142, (long) result.get(0).i);
		Assert.assertEquals(123456789012345L, (long) result.get(0).l);
		Assert.assertEquals(new BigDecimal("2.1"), result.get(0).bd);
		Assert.assertEquals("s", result.get(0).s);
		Assert.assertEquals(LocalDate.of(2012, 3, 4), result.get(0).ld);
	}

	@Test
	public void testReadWithSpaces() throws Exception {
		CsvReader reader = reader("i, l,  bd,   s,  ld\n    142,   123456789012345,   2.1,   s,   " + LocalDate.of(2012, 3, 4));
		List<CsvReaderTestA> result = reader.readValues(CsvReaderTestA.class);
		checkResult(result);
	}

	@Test
	public void testReadWithTabs() throws Exception {
		CsvReader reader = reader("\ti,\tl\t,\tbd\t,\t\ts,\tld\n\t142,\t123456789012345,\t2.1,\ts,\t" + LocalDate.of(2012, 3, 4));
		List<CsvReaderTestA> result = reader.readValues(CsvReaderTestA.class);
		checkResult(result);
	}

	@Test
	public void testReadWithMissingLastValue() throws Exception {
		CsvReader reader = reader("i,l,bd,s,ld\n 142,123456789012345,2.1,s,");
		reader.readValues(CsvReaderTestA.class);
	}

	@Test
	public void testSkipEmptyLine() throws Exception {
		CsvReader reader = reader("i,l,bd,s,ld\n\n 142,123456789012345,2.1,s,\n\n 142,123456789012345,2.1,s,");
		List<CsvReaderTestA> result = reader.readValues(CsvReaderTestA.class);
		Assert.assertEquals(2, result.size());
	}

	@Test
	public void testSkipCommentLine() throws Exception {
		CsvReader reader = readerWithComment("i,l,bd,s,ld\n#Hello World\n 142,123456789012345,2.1,s,\n#Hello World\n 142,123456789012345,2.1,s,");
		List<CsvReaderTestA> result = reader.readValues(CsvReaderTestA.class);
		Assert.assertEquals(2, result.size());
	}

	@Test
	public void testSetSeparator() throws Exception {
		CsvReader reader = reader("i;l;bd;s;ld\n142;123456789012345;2.1;s;" + LocalDate.of(2012, 3, 4));
		reader.setSeparator(';');
		List<CsvReaderTestA> result = reader.readValues(CsvReaderTestA.class);
		checkResult(result);

		reader = reader("\"a,b\",");
		Assert.assertEquals("a,b", reader.readEscaped());

		reader = reader("\"a;b\",");
		Assert.assertEquals("a;b", reader.readEscaped());
	}

	private static CsvReader reader(String s) {
		ByteArrayInputStream bais = new ByteArrayInputStream(s.getBytes());
		return new CsvReader(bais);
	}

	private static CsvReader readerWithComment(String s) {
		ByteArrayInputStream bais = new ByteArrayInputStream(s.getBytes());
		CsvReader csvReader = new CsvReader(bais);
		csvReader.setCommentStart("#");
		return csvReader;
	}

	public static class CsvReaderTestA {
		public Integer i;
		public Long l;
		public BigDecimal bd;
		public String s;
		public LocalDate ld;
	}
}
