package org.minimalj.util;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.joda.time.ReadablePartial;
import org.minimalj.model.EnumUtils;
import org.minimalj.model.properties.FlatProperties;


public class SerializationOutputStream {

	private final DataOutputStream dos;
	
	public SerializationOutputStream(OutputStream out) {
		dos = new DataOutputStream(out);
	}
	
	private void writeObject(Object object) throws IOException {
		Class<?> clazz = object.getClass();
		Field[] fields = clazz.getDeclaredFields();
		Arrays.sort(fields, new FlatProperties.FieldComparator());
		for (Field field : fields) {
			if (FieldUtils.isTransient(field) || FieldUtils.isStatic(field)) continue;
			Object value = null;
			try {
				field.setAccessible(true);
				value = field.get(object);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
			if (!writePrimitiv(value, field.getType())) {
	    		write(value);	
			}
		}
	}

	public void write(Object value) throws IOException {
		if (value != null) {
    		dos.write(1);
			writeObject(value);
		} else {
    		dos.write(0);
		}
	}

	private boolean writePrimitiv(Object value, Class<?> fieldClazz) throws IOException {
		if (value == null) {
    		dos.write(0);
    		return true;
		}

		if (fieldClazz == String.class) {
			writeString((String) value);
		} else  if (fieldClazz == Byte.TYPE) {
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
		} else if (fieldClazz.isArray()) {
			Object[] objects = (Object[]) value;
			dos.writeInt(objects.length);
			for (int i = 0; i<objects.length; i++) {
				write(objects[i]);
			}
		} else if (value instanceof Set<?>) {
			Set<?> set = (Set<?>) value;
			if (set.isEmpty()) {
				dos.writeInt(0);
			} else {
				Class<?> enumClass = set.iterator().next().getClass();
				int asInt = EnumUtils.getInt(set, enumClass);
				dos.writeInt(asInt);
			}
		} else if (ReadablePartial.class.isAssignableFrom(fieldClazz)) {
			writeString(DateUtils.formatPartial((ReadablePartial) value));
		} else if (Enum.class.isAssignableFrom(fieldClazz)) {
			dos.writeByte(((Enum<?>) value).ordinal() + 1);
		} else {
			return false;
		}
		return true;
	}

    public void writeString(String value) throws IOException {
		int chunkSize = 20000;
		int chunks = (value.length() - 1) / chunkSize + 1;
		dos.write(chunks);
		for (int i = 0; i<chunks; i++) {
    		dos.writeUTF(value.substring(chunkSize * i, Math.min(chunkSize * (i + 1), value.length())));
		}
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
			write(object);
		} else {
			dos.write(0);
		}
	}
	
	public static void main(String... args) {
		// class [Ljava.lang.Object;
		// class java.lang.Object
		System.out.println(new Object().getClass());
	}

}
