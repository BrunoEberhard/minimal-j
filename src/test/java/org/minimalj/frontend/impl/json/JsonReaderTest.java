package org.minimalj.frontend.impl.json;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test to verify how JsonReader would work with Uinames service (not used at
 * the moment but the JsonRead should be able to do this)
 *
 */
@SuppressWarnings("unchecked")
public class JsonReaderTest {

	@Test
	public void testUiname() throws Exception {
		Map<String, Object> data = (Map<String, Object>) JsonReader
				.read("{\"name\":\"Emma\",\"surname\":\"Moser\",\"gender\":\"female\",\"region\":\"Switzerland\"}");
		Assert.assertEquals("Moser", data.get("surname"));
	}

	@Test
	public void testUinames() throws Exception {
		List<Map<String, Object>> data = (List<Map<String, Object>>) JsonReader
				.read("[{\"name\":\"Emma\",\"surname\":\"Moser\",\"gender\":\"female\",\"region\":\"Switzerland\"}]");
		Assert.assertEquals(1, data.size());
		Assert.assertEquals("Moser", data.get(0).get("surname"));
	}

	public static class TestUinames {
		public String name, surname, gender, region;
	}

	@Test
	public void testMaxDouble() {
		String s = "{\"value\":" + Double.MAX_VALUE + "}";
		Map<String, Object> result = (Map<String, Object>) JsonReader.read(s);
		Assert.assertEquals(Double.MAX_VALUE, result.get("value"));
	}
	
	@Test
	public void testMinDouble() {
		String s = "{\"value\":" + Double.MIN_VALUE + "}";
		Map<String, Object> result = (Map<String, Object>) JsonReader.read(s);
		Assert.assertEquals(Double.MIN_VALUE, result.get("value"));
	}

	@Test
	public void testInteger() {
		String s = "{\"value\":42}";
		Map<String, Object> result = (Map<String, Object>) JsonReader.read(s);
		Assert.assertEquals(42L, result.get("value"));
	}
	
	@Test
	public void testMaxLong() {
		String s = "{\"value\":" + Long.MAX_VALUE + "}";
		Map<String, Object> result = (Map<String, Object>) JsonReader.read(s);
		Assert.assertEquals(Long.MAX_VALUE, result.get("value"));
	}
	
	@Test
	public void testEscaping() {
		String complicatedString = "Happy \"get\" Lucky";
		Map<String, Object> input = Collections.singletonMap("test", complicatedString);
		String s = new JsonWriter().write(input);
		System.out.println(s);
		Map<String, Object> result = (Map<String, Object>) JsonReader.read(s);
		Assert.assertEquals(complicatedString, result.get("test"));
	}
}
