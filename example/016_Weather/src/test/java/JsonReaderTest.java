import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.minimalj.example.weather.model.Weather;
import org.minimalj.rest.EntityJsonReader;

public class JsonReaderTest {

	@Test
	public void testReader() throws Exception {
		String out = Files.readAllLines(
			    Paths.get(this.getClass().getResource("zurich.json").toURI()), Charset.defaultCharset()).stream().collect(Collectors.joining());
	
		Weather weather = EntityJsonReader.read(Weather.class, out);
		
		Assert.assertEquals("Zurich", weather.city.name);
	}
}
