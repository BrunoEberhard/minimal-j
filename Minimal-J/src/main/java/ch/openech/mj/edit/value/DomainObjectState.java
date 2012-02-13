package ch.openech.mj.edit.value;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.openech.mj.util.FieldUtils;

public class DomainObjectState {
	private static final DomainObjectState NULL_STATE = new DomainObjectState();
	private final Object object;
	private Map<String, Object> values = new HashMap<String, Object>();

	public DomainObjectState() {
		this.object = null;
	}
	
	public DomainObjectState(Object object) {
		this.object = object;
		Class<?> clazz = object.getClass();
		
		for (Field field : clazz.getFields()) {
			if (FieldUtils.isStatic(field) || !FieldUtils.isPublic(field)) continue;
			
			Object value = get(field, object);
			if (value == null) {
				values.put(field.getName(), NULL_STATE);
			} else if (field.getType().isPrimitive() || field.getType().getName().startsWith("java.lang")) {
				values.put(field.getName(), value);
			} else if (value instanceof List<?>) {
				List<?> list = (List<?>) value;
				List<DomainObjectState> listState = new ArrayList<DomainObjectState>(list.size());
				for (Object o : list) {
					listState.add(new DomainObjectState(o));
				}
				values.put(field.getName(), listState);
			} else {
				values.put(field.getName(), new DomainObjectState(value));
			}
		}
	}
	
	private static Object get(Field field, Object object) {
		try {
			return field.get(object);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return null;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static void set(Field field, Object object, Object value) {
		try {
			field.set(object, value);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	public void restore() {
		if (object == null) return;
		
		Class<?> clazz = object.getClass();
		
		for (Field field : clazz.getFields()) {
			if (FieldUtils.isStatic(field) || !FieldUtils.isPublic(field)) continue;

			Object value = values.get(field.getName());
			if (NULL_STATE.equals(value)) {
				set(field, object, null);
			} else if (field.getType().isPrimitive() || field.getType().getName().startsWith("java.lang")) {
				set(field, object, value);
			} else if (value instanceof List) {
				List<DomainObjectState> listState = (List<DomainObjectState>) value;
				List list = (List)get(field, object);
				list.clear();
				for (DomainObjectState state : listState) {
					state.restore();
					list.add(state.object);
				}
			} else {
				DomainObjectState state = (DomainObjectState) value;
				state.restore();
				if (!FieldUtils.isFinal(field)) {
					set(field, object, state.object);
				}
			}
		}
	}
	
}
