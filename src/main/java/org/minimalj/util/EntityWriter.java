package org.minimalj.util;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.minimalj.model.EnumUtils;
import org.minimalj.model.properties.FlatProperties;


public class EntityWriter {

	private final DataOutputStream dos;
	
	public EntityWriter(OutputStream outputStream) {
		dos = new DataOutputStream(outputStream);
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
			Class<?> fieldClass = field.getType();
			if (fieldClass == Object.class && "id".equals(field.getName())) {
				// id values are serialized as String
				fieldClass = String.class;
				value = value != null ? value.toString() : null;
			}
			if (!writePrimitiv(value, fieldClass)) {
	    		writeIfNotNull(value);	
			}
		}
	}

	private void writeIfNotNull(Object value) throws IOException {
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
			@SuppressWarnings("rawtypes")
			List list = (List) value;
			dos.writeInt(list.size());
			for (int i = 0; i<list.size(); i++) {
				writeIfNotNull(list.get(i));
			}
		} else if (fieldClazz.isArray()) {
			int arrayLength = Array.getLength(value);
			dos.writeInt(arrayLength);
			for (int i = 0; i<arrayLength; i++) {
				writeIfNotNull(Array.get(value, i));
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
		} else if (value instanceof LocalDate) {
			dos.write(1); // ok, year is probably not 0 but still
			write((LocalDate) value);
		} else if (value instanceof LocalTime) {
			dos.write(1);
			write((LocalTime) value);
		} else if (value instanceof LocalDateTime) {
			dos.write(1);
			write((LocalDateTime) value);
		} else if (Enum.class.isAssignableFrom(fieldClazz)) {
			dos.writeByte(((Enum<?>) value).ordinal() + 1);
		} else {
			return false;
		}
		return true;
	}

    private void write(LocalDate value) throws IOException {
    	dos.writeInt(value.getYear());
    	dos.writeByte(value.getMonthValue());
    	dos.writeByte(value.getDayOfMonth());
	}
    
    private void write(LocalTime value) throws IOException {
    	int nano = value.getNano();
    	int second = value.getSecond();
    	int minute = value.getMinute();
    	int hour = value.getHour();
        if (nano == 0) {
            if (second == 0) {
                if (minute == 0) {
                    dos.writeByte(~hour);
                } else {
                    dos.writeByte(hour);
                    dos.writeByte(~minute);
                }
            } else {
                dos.writeByte(hour);
                dos.writeByte(minute);
                dos.writeByte(~second);
            }
        } else {
            dos.writeByte(hour);
            dos.writeByte(minute);
            dos.writeByte(second);
            dos.writeInt(nano);
        }
	}
    
    private void write(LocalDateTime value) throws IOException {
    	write(value.toLocalDate());
    	write(value.toLocalTime());
	}

    private void writeString(String value) throws IOException {
		int chunkSize = 20000;
		int chunks = (value.length() - 1) / chunkSize + 1;
		dos.write(chunks);
		for (int i = 0; i<chunks; i++) {
    		dos.writeUTF(value.substring(chunkSize * i, Math.min(chunkSize * (i + 1), value.length())));
		}
    }

	public void write(Object object) throws IOException {
		if (object != null) {
			writeString(object.getClass().getName());
			writeIfNotNull(object);
		} else {
			dos.write(0);
		}
	}
}
