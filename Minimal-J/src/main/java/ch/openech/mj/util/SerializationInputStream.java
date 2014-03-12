package ch.openech.mj.util;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.ReadablePartial;

import ch.openech.mj.edit.value.CloneHelper;
import ch.openech.mj.model.EnumUtils;
import ch.openech.mj.model.PropertyInterface;
import ch.openech.mj.model.Reference;
import ch.openech.mj.model.properties.FlatProperties;


public class SerializationInputStream implements AutoCloseable {
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
		} else if (Enum.class.isAssignableFrom(fieldClazz)) {
			return EnumUtils.valueList((Class) fieldClazz).get(b - 1);
		} else {
			return readObject(fieldClazz);
		}
	}

	public Object readObject(Class<?> fieldClazz) throws IOException {
		Object object = CloneHelper.newInstance(fieldClazz);
		Map<String, PropertyInterface> properties = FlatProperties.getProperties(object.getClass());
		List<String> propertyNames = new ArrayList<>(properties.keySet());
		Collections.sort(propertyNames);
		for (String propertyName : propertyNames) {
			PropertyInterface property = properties.get(propertyName);
			if (!property.isFinal()) {
				Object value = read(property.getFieldClazz());
				property.setValue(object, value);
			} else {
				read(object, property);
			}
		}
		return object;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void read(Object object, PropertyInterface property) throws IOException {
		Class<?> fieldClazz = property.getFieldClazz();
		
		if (fieldClazz == Set.class) {
			int asInt = dis.readInt();
			EnumUtils.fillSet(asInt, fieldClazz, (Set) property.getValue(object));
		} else if (fieldClazz == List.class) {
			int size = dis.readInt();
			List list = (List) property.getValue(object);
			Class<?> listClass = GenericUtils.getGenericClass(property.getType());
			if (list instanceof ArrayList) ((ArrayList) list).ensureCapacity(size);
			for (int i = 0; i<size; i++) {
				list.add(read(listClass));
			}
		} else if (fieldClazz == Reference.class) {
			
		} else {
			System.out.println("Ignored: " + fieldClazz.getName());
//			throw new IllegalArgumentException(fieldClazz.getName());
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

	@Override
	public void close() throws Exception {
		dis.close();
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
