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
	
	@Test
	public void testNestedObjects() {
		String s = "{\"tableAction\":{\"table\":\"a41986a7-10dc-43e7-93da-61d8ff15ba14\",\"row\":29},\"locale\":\"de\",\"inputTypes\":false,\"dialogVisible\":false,\"session\":\"453fffb6-01a0-488e-951b-6555bb63f30f\"}";
		JsonReader read = new JsonReader();
		read.read(s);
	}
	
}
