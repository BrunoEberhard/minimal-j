import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import org.junit.Test;
import org.minimalj.example.weather.json.JsonReader;
import org.minimalj.example.weather.model.Weather;

public class JsonReaderTest {

	@Test
	public void testReader() throws Exception {
		String out = Files.readAllLines(
			    Paths.get(this.getClass().getResource("zurich.json").toURI()), Charset.defaultCharset()).stream().collect(Collectors.joining());
	
		JsonReader reader = new JsonReader(Weather.class);
		Weather weather = reader.read(out);
		
		System.out.println(weather);
	}
}
