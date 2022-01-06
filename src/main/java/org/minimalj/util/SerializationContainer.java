package org.minimalj.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SerializationContainer implements Serializable {

	private static final long serialVersionUID = 1L;

	private transient Object object;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Serializable wrap(Object object) {
		if (object instanceof List) {
			List list = (List) object;
			ArrayList arrayList = new ArrayList<>(list.size());
			for (int i = 0; i<list.size(); i++) {
				arrayList.add(wrap(list.get(i)));
			}
			return arrayList;
		} else if (object instanceof Serializable) {
			return (Serializable) object;
		} else {
			SerializationContainer container = new SerializationContainer();
			container.object = object;
			return container;
		}
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
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		EntityWriter writer = new EntityWriter(out);
		writer.write(object);
 	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		EntityReader reader = new EntityReader(in);
		object = reader.read();
	}
	
}
