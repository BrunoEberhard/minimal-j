package ch.openech.mj.util;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.ReadablePartial;

import ch.openech.mj.model.EnumUtils;
import ch.openech.mj.model.PropertyInterface;
import ch.openech.mj.model.properties.FlatProperties;


public class SerializationOutputStream implements AutoCloseable {

	private final DataOutputStream dos;
	
	public SerializationOutputStream(OutputStream out) {
		dos = new DataOutputStream(out);
	}
	
	public void write(Object object) throws IOException {
		if (object == null) {
			dos.write(0);
		} else {
			dos.write(1);
			writeObject(object);
		}
	}
	
	private void writeObject(Object object) throws IOException {
		Map<String, PropertyInterface> properties = FlatProperties.getProperties(object.getClass());
		List<String> propertyNames = new ArrayList<>(properties.keySet());
		Collections.sort(propertyNames);
		for (String propertyName : propertyNames) {
			PropertyInterface property = properties.get(propertyName);
			Object value = property.getValue(object);
			write(property, value);
		}
	}

	private void write(PropertyInterface property, Object value) throws IOException {
		Class<?> fieldClazz = property.getFieldClazz();
		
		if (value == null) {
    		dos.write(0);
    		return;
		}
		if (fieldClazz == String.class) {
			writeString((String) value);
		} else if (value instanceof Set<?>) {
			Set<?> set = (Set<?>) value;
			Class<?> enumClass = GenericUtils.getGenericClass(property.getType());
			int asInt = EnumUtils.getInt(set, enumClass);
			dos.writeInt(asInt);
		} else if (fieldClazz == Byte.TYPE) {
			dos.write((Byte) value);
		} else if (fieldClazz == Short.TYPE) {
			dos.writeShort((Short) value);
		} else if (fieldClazz == Integer.TYPE) {
			dos.writeInt((Integer) value);
		} else if (fieldClazz == Long.TYPE) {
			dos.writeLong((Long) value);
		} else if (fieldClazz == Boolean.TYPE) {
			dos.write(((boolean) value) ? 1 : 0);
		} else if (fieldClazz == Byte.class) {
			dos.write(1);
			dos.write((Byte) value);
		} else if (fieldClazz == Short.class) {
			dos.write(1);
			dos.writeShort((Short) value);
		} else if (fieldClazz == Integer.class) {
			dos.write(1);
			dos.writeInt((Integer) value);
		} else if (fieldClazz == Long.class) {
			dos.write(1);
			dos.writeLong((Long) value);
		} else if (fieldClazz == Boolean.class) {
			dos.write(1);
			dos.write(((Boolean) value).booleanValue() ? 1 : 0);
		} else if (fieldClazz == List.class) {
			List list = (List) value;
			dos.writeInt(list.size());
			for (int i = 0; i<list.size(); i++) {
				write(list.get(i));
			}
		} else if (ReadablePartial.class.isAssignableFrom(fieldClazz)) {
			writeString(DateUtils.formatPartial((ReadablePartial) value));
		} else if (Enum.class.isAssignableFrom(fieldClazz)) {
			write(((Enum<?>) value).ordinal() + 1);
		} else {
    		dos.write(1);
			writeObject(value);
		}
	}
	
    public void writeString(String value) throws IOException {
		int chunkSize = 20000;
		int chunks = (value.length() - 1) / chunkSize + 1;
		dos.write(chunks);
		for (int i = 0; i<chunks; i++) {
    		dos.writeUTF(value.substring(chunkSize * i, Math.min(chunkSize * (i + 1), value.length())));
		}
    }

	@Override
	public void close() throws Exception {
		dos.close();
	}

	public void writeParameterTypes(Class<?>[] parameterTypes) throws IOException {
		dos.write(parameterTypes.length);
		for (Class<?> clazz : parameterTypes) {
			writeString(clazz.getName());
		}
	}

	public void writeArguments(Object[] args) throws IOException {
		dos.write(args.length);
		for (Object object : args) {
			writeArgument(object);
		}
	}

	public void writeArgument(Object object) throws IOException {
		if (object != null) {
			writeString(object.getClass().getName());
			writeObject(object);
		} else {
			dos.write(0);
		}
	}

}
