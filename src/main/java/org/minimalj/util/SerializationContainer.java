package org.minimalj.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;

public class SerializationContainer implements Serializable {

	private static final long serialVersionUID = 1L;

	private transient Object object;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Serializable wrap(Object object) {
		if (object instanceof List) {
			List list = (List) object;
			for (int i = 0; i<list.size(); i++) {
				list.set(i, wrap(list.get(i)));
			}
			return (Serializable) list;
		} else if (object instanceof Serializable) {
			return (Serializable) object;
		} else {
			SerializationContainer container = new SerializationContainer();
			container.object = object;
			return container;
		}
	}
	
	public static Object[] wrap(Object[] objects) {
		Object[] containers = new Object[objects.length];
		for (int i = 0; i<objects.length; i++) {
			containers[i] = wrap(objects[i]);
		}
		return containers;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Object unwrap(Object container) {
		if (container instanceof List) {
			List list = (List) container;
			for (int i = 0; i<list.size(); i++) {
				list.set(i, unwrap(list.get(i)));
			}
			return list;
		} else if (container instanceof SerializationContainer) {
			return ((SerializationContainer) container).object;
		} else {
			return container;
		}
	}
	
	public static Object[] unwrap(Object[] containers) {
		Object[] objects = new Object[containers.length];
		for (int i = 0; i<containers.length; i++) {
			objects[i] = unwrap(containers[i]);
		}
		return objects;
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		SerializationOutputStream soo = new SerializationOutputStream(out);
		soo.writeArgument(object);
 	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		SerializationInputStream soo = new SerializationInputStream(in);
		object = soo.readArgument();
	}
	
}
