package ch.openech.mj.util;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.ReadablePartial;

import ch.openech.mj.edit.value.CloneHelper;
import ch.openech.mj.model.EnumUtils;
import ch.openech.mj.model.properties.FlatProperties;


public class SerializationInputStream {
	private static final Logger logger = Logger.getLogger(SerializationInputStream.class.getName());

	private final DataInputStream dis;
	
	public SerializationInputStream(InputStream out) {
		dis = new DataInputStream(out);
	}
	
	public Object read(Class<?> fieldClazz) throws IOException {
		if (fieldClazz == Byte.TYPE) {
			return new Byte(dis.readByte());
		} else if (fieldClazz == Short.TYPE) {
			return new Short(dis.readShort());
		} else if (fieldClazz == Integer.TYPE) {
			return new Integer(dis.readInt());
		} else if (fieldClazz == Long.TYPE) {
			return new Long(dis.readLong());
		} else if (fieldClazz == Boolean.TYPE) {
			return Boolean.valueOf(dis.read() != 0);
		} else if (Enum.class.isAssignableFrom(fieldClazz)) {
			byte b = dis.readByte();
			if (b == 0) return null;
			return EnumUtils.valueList((Class) fieldClazz).get(b - 1);
		}
		
		int b = dis.read();
		if (b == 0) {
			return null;
		}
		if (fieldClazz == String.class) {
			return readString(b);
		} else if (fieldClazz == Byte.class) {
			return (byte) dis.read();
		} else if (fieldClazz == Short.class) {
			return (short) dis.readShort();
		} else if (fieldClazz == Integer.class) {
			return (int) dis.readInt();
		} else if (fieldClazz == Long.class) {
			return (long) dis.readLong();
		} else if (fieldClazz == Boolean.class) {
			return dis.read() != 0;
		} else if (LocalDate.class.isAssignableFrom(fieldClazz)) {
			return new LocalDate(readString(1));
		} else if (LocalTime.class.isAssignableFrom(fieldClazz)) {
			return new LocalTime(readString(1));
		} else if (LocalDateTime.class.isAssignableFrom(fieldClazz)) {
			return new LocalDateTime(readString(1));
		} else if (ReadablePartial.class.isAssignableFrom(fieldClazz)) {
			return DateUtils.parsePartial(readString(1));
		} else if (fieldClazz.isArray()) {
			int length = dis.readInt();
			Object[] objects = (Object[]) Array.newInstance(fieldClazz, length);
			for (int i = 0; i<length; i++) {
				objects[i] = read(fieldClazz);
			}
			return objects;
		} else {
			return readObject(fieldClazz);
		}
	}

	public Object readObject(Class<?> fieldClazz) throws IOException {
		Object object = CloneHelper.newInstance(fieldClazz);
		Field[] fields = fieldClazz.getDeclaredFields();
		Arrays.sort(fields, new FlatProperties.FieldComparator());
		for (Field field : fields) {
			if (FieldUtils.isTransient(field) || FieldUtils.isStatic(field)) continue;
			if (!FieldUtils.isFinal(field)) {
				Object value = read(field.getType());
				try {
					field.setAccessible(true);
					field.set(object, value);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			} else {
				read(object, field);
			}
		}
		return object;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void read(Object object, Field field) throws IOException {
		Class<?> fieldClazz = field.getType();
		
		Object value = null;
		try {
			value = field.get(object);
		} catch (Exception x) {
			x.printStackTrace();
		}
		
		if (value instanceof Set) {
			int asInt = dis.readInt();
			EnumUtils.fillSet(asInt, fieldClazz, (Set) value);
		} else if (value instanceof List) {
			int size = dis.readInt();
			List list = (List) value;
			Class<?> listClass = GenericUtils.getGenericClass(field.getGenericType());
			if (list instanceof ArrayList) ((ArrayList) list).ensureCapacity(size);
			for (int i = 0; i<size; i++) {
				list.add(read(listClass));
			}
		} else {
			Object readValue = read(fieldClazz);
			CloneHelper.deepCopy(readValue, value);
		}
	}

    public String readString() throws IOException {
    	int chunks = dis.read();
    	return readString(chunks);
    }

    private String readString(int chunks) throws IOException {
    	if (chunks == 0) return null;
		int chunkSize = 20000;
		StringBuilder s = new StringBuilder((chunks + 1) * chunkSize);
		for (int i = 0; i<chunks; i++) {
			s.append(dis.readUTF());
		}
		return s.toString();
    }

	public Class<?>[] readParameterTypes() throws IOException {
		int count = dis.read();
		Class<?>[] result = new Class[count];
		for (int i = 0; i<count; i++) {
			String className = readString();
			try {
				result[i] = Class.forName(className);
			} catch (ClassNotFoundException e) {
				throw new LoggingRuntimeException(e, logger, "Class loading failed");
			}
		}
		return result;
	}

	public Object[] readArguments() throws IOException {
		int count = dis.read();
		Object[] result = new Object[count];
		for (int i = 0; i<count; i++) {
			result[i] = readArgument();
		}
		return result;
	}

	public Object readArgument() throws IOException {
		String className = readString();
		if (className != null) {
			try {
				return read(Class.forName(className));
			} catch (ClassNotFoundException e) {
				throw new LoggingRuntimeException(e, logger, "readArguments failed");
			}
		} else {
			return null;
		}
	}

}
