package org.minimalj.util;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.minimalj.model.PropertyInterface;
import org.minimalj.model.properties.FlatProperties;
import org.threeten.bp.temporal.TemporalAccessor;

public class HashUtils {
	private static final Logger logger = Logger.getLogger(HashUtils.class.getName());
	
	public static int getHash(Object object) {
		if (object == null) return 0;
		
		final int prime = 31;
		int result = 1;
		Map<String, PropertyInterface> properties = FlatProperties.getProperties(object.getClass());
		for (Map.Entry<String, PropertyInterface> entry : properties.entrySet()) {
			PropertyInterface property = entry.getValue();
			Object value = property.getValue(object);
			if (value != null) {
				result = prime * result + entry.getKey().hashCode();
				result = prime * result + value.hashCode();
			}
		}
		return result;
	}
	
	public static UUID getUuid(Object object) {
		if (object == null) return null;

		try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(2000)) {
			try (DataOutputStream dos = new DataOutputStream(byteArrayOutputStream)) {
				writeObject(dos, object);
				return type5UUID(byteArrayOutputStream.toByteArray());
			}
		} catch (IOException x) {
			throw new LoggingRuntimeException(x, logger, "UUI generation failed");
		}
	}

	static void writeObject(DataOutputStream dos, Object object) throws IOException {
		Map<String, PropertyInterface> properties = FlatProperties.getProperties(object.getClass());
		for (Map.Entry<String, PropertyInterface> entry : properties.entrySet()) {
			PropertyInterface property = entry.getValue();
			Object value = property.getValue(object);
			if (value == null) continue;
			
			String fieldPath = property.getFieldPath();
			for (int i = 0; i<fieldPath.length(); i++) {
				dos.writeChar(fieldPath.charAt(i));
			}
			dos.writeChar('=');

			if (value instanceof String) {
				String string = (String) value;
				writeString(dos, string);
			} else if (value instanceof Integer) {
				Integer integer = (Integer) value;
				dos.writeInt(integer);
			} else if (value instanceof Boolean) {
				Boolean b = (Boolean) value;
				dos.write(b ? 1 : 0);
			} else if (value instanceof Enum<?>) {
				Enum<?> e = (Enum<?>) value;
				writeString(dos, e.name());
			} else if (value instanceof TemporalAccessor) {
				dos.writeInt(value.hashCode());
			} else if (value instanceof BigDecimal) {
				 BigDecimal bigDecimal = (BigDecimal) value;
				 bigDecimal = bigDecimal.setScale(0);
				 dos.writeInt(bigDecimal.hashCode());
			} else if (value instanceof List<?>) {
				List<?> list = (List<?>) value;
				dos.write((int)'{');
				for (Object o : list) {
					writeObject(dos, o);
					dos.write((int)',');
				}
				dos.write((int)'}');
			} else {
				writeString(dos, getUuid(value).toString());
			}
		}
	}

	static void writeString(DataOutputStream dos, String string) throws IOException {
		for (int i = 0; i<string.length(); i++) {
			dos.writeChar(string.charAt(i));
		}
	}
	
    public static UUID type5UUID(byte[] name) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException nsae) {
            throw new InternalError("SHA not supported");
        }
        byte[] data = md.digest(name);
        
        data[6]  &= 0x0f;  /* clear version        */
        data[6]  |= 0x50;  /* set to version 5 (!) */
        data[8]  &= 0x3f;  /* clear variant        */
        data[8]  |= 0x80;  /* set to IETF variant  */
        
        long msb = 0;
        long lsb = 0;
        for (int i=0; i<8; i++)
            msb = (msb << 8) | (data[i] & 0xff);
        for (int i=8; i<16; i++)
            lsb = (lsb << 8) | (data[i] & 0xff);
        return new UUID(msb, lsb);
    }
	
}
