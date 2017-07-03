package org.minimalj.frontend.impl.json;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test to verify how JsonReader would work with Uinames service (not used at
 * the moment but the JsonRead should be able to do this)
 *
 */
public class JsonReaderTest {

	@Test
	public void testUiname() throws Exception {
		JsonReader reader = new JsonReader();
		Map<String, Object> data = (Map<String, Object>) reader
				.read("{\"name\":\"Emma\",\"surname\":\"Moser\",\"gender\":\"female\",\"region\":\"Switzerland\"}");
		Assert.assertEquals("Moser", data.get("surname"));
	}

	@Test
	public void testUinames() throws Exception {
		JsonReader reader = new JsonReader();
		List<Map<String, Object>> data = (List<Map<String, Object>>) reader
				.read("[{\"name\":\"Emma\",\"surname\":\"Moser\",\"gender\":\"female\",\"region\":\"Switzerland\"}]");
		Assert.assertEquals(1, data.size());
		Assert.assertEquals("Moser", data.get(0).get("surname"));
	}

	public static class TestUinames {
		public String name, surname, gender, region;
	}
}
