package org.minimalj.util;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.minimalj.model.EnumUtils;
import org.minimalj.model.properties.FlatProperties;


public class EntityReader {
	private static final Logger logger = Logger.getLogger(EntityReader.class.getName());

	private final DataInputStream dis;
	
	public EntityReader(InputStream inputStream) {
		dis = new DataInputStream(inputStream);
	}
	
	public Object readValue(Class<?> clazz) throws IOException {
		if (clazz == Byte.TYPE) {
			return dis.readByte();
		} else if (clazz == Short.TYPE) {
			return dis.readShort();
		} else if (clazz == Integer.TYPE) {
			return dis.readInt();
		} else if (clazz == Long.TYPE) {
			return dis.readLong();
		} else if (clazz == Boolean.TYPE) {
			return dis.read() != 0;
		} else if (Enum.class.isAssignableFrom(clazz)) {
			byte b = dis.readByte();
			if (b == 0) return null;
			return EnumUtils.valueList((Class) clazz).get(b - 1);
		} else if (List.class.isAssignableFrom(clazz)) {
			throw new IllegalArgumentException();
		}
		
		int b = dis.read();
		if (b == 0) {
			return null;
		}
		if (clazz == String.class) {
			return readString(b);
		} else if (clazz == Byte.class) {
			return (byte) dis.read();
		} else if (clazz == Short.class) {
			return dis.readShort();
		} else if (clazz == Integer.class) {
			return dis.readInt();
		} else if (clazz == Long.class) {
			return dis.readLong();
		} else if (clazz == Boolean.class) {
			return dis.read() != 0;
		} else if (LocalDate.class.isAssignableFrom(clazz)) {
			return readLocalDate(dis);
		} else if (LocalTime.class.isAssignableFrom(clazz)) {
			return readLocalTime(dis);
		} else if (LocalDateTime.class.isAssignableFrom(clazz)) {
			return readLocalDateTime(dis);
		} else if (clazz.isArray()) {
			int length = dis.readInt();
			Object objects = Array.newInstance(clazz, length);
			for (int i = 0; i<length; i++) {
				Array.set(objects, i, readValue(clazz));
			}
			return objects;
		} else {
			return readEntity(clazz);
		}
	}

	private static LocalDate readLocalDate(DataInputStream dis) throws IOException {
		int year = dis.readInt();
		int month = dis.readByte();
		int dayOfMonth = dis.readByte();
		return LocalDate.of(year, month, dayOfMonth);
	}

	private static LocalTime readLocalTime(DataInputStream in) throws IOException {
        int hour = in.readByte();
        int minute = 0;
        int second = 0;
        int nano = 0;
        if (hour < 0) {
            hour = ~hour;
        } else {
            minute = in.readByte();
            if (minute < 0) {
                minute = ~minute;
            } else {
                second = in.readByte();
                if (second < 0) {
                    second = ~second;
                } else {
                    nano = in.readInt();
                }
            }
        }
        return LocalTime.of(hour, minute, second, nano);
	}
	
	private static LocalDateTime readLocalDateTime(DataInputStream in) throws IOException {
        LocalDate date = readLocalDate(in);
        LocalTime time = readLocalTime(in);
        return LocalDateTime.of(date, time);
	}
	
	private Object readEntity(Class<?> clazz) throws IOException {
		Object object = CloneHelper.newInstance(clazz);
		while (clazz != null) {
			Field[] fields = clazz.getDeclaredFields();
			Arrays.sort(fields, new FlatProperties.FieldComparator());
			for (Field field : fields) {
				if (FieldUtils.isTransient(field) || FieldUtils.isStatic(field))
					continue;
				
				if (!FieldUtils.isFinal(field) && field.getType() == Object.class && "id".equals(field.getName())) {
					Object value = readValue(String.class);
					try {
						field.setAccessible(true);
						field.set(object, value);
					} catch (IllegalArgumentException | IllegalAccessException e) {
						e.printStackTrace();
					}
				} else {
					readField(object, field);
				}
			}
			clazz = clazz.getSuperclass();
		}
		return object;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void readField(Object object, Field field) throws IOException {
		Class<?> fieldClazz = field.getType();
		
		Object value;
		Object originalValue;
		try {
			field.setAccessible(true);
			originalValue = value = field.get(object);
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
		
		if (value instanceof Set) {
			int asInt = dis.readInt();
			EnumUtils.fillSet(asInt, fieldClazz, (Set) value);
		} else if (List.class.isAssignableFrom(fieldClazz)) {
			int size = dis.readInt();
			if (value == null) {
				value = new ArrayList<>(size);
			} else {
				if (value instanceof ArrayList)
					((ArrayList) value).ensureCapacity(size);
			}
			List list = (List) value;
			Class<?> listClass = GenericUtils.getGenericClass(field);
			for (int i = 0; i<size; i++) {
				list.add(readValue(listClass));
			}
		} else {
			Object readValue = readValue(fieldClazz);
			if (value != null && !fieldClazz.isPrimitive()) {
				CloneHelper.deepCopy(readValue, value);
			} else {
				value = readValue;
			}
		}

		if (originalValue != value) {
			try {
				field.set(object, value);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

    private String readString() throws IOException {
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

	public Object read() throws IOException {
		String className = readString();
		if (className != null) {
			try {
				return readValue(Class.forName(className));
			} catch (ClassNotFoundException e) {
				throw new LoggingRuntimeException(e, logger, "readArguments failed");
			}
		} else {
			return null;
		}
	}

}
