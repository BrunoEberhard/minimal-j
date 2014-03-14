package ch.openech.mj.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class SerializationTest {

	private <T> T serialize(Class<T> clazz, T object) throws Exception {
		try (ByteArrayOutputStream s = new ByteArrayOutputStream()) {
			SerializationOutputStream sos = new SerializationOutputStream(s);
			sos.write(object);
			s.flush();
			byte[] bytes = s.toByteArray();
			
			try (ByteArrayInputStream in = new ByteArrayInputStream(bytes)) {
				SerializationInputStream sis = new SerializationInputStream(in);
				return (T) sis.read(clazz);
			}
		}
	}

	
}
