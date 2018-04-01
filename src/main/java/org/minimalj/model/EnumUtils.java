package org.minimalj.model;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import org.minimalj.model.Rendering.RenderType;
import org.minimalj.util.LocaleContext;


public class EnumUtils {

	@SuppressWarnings({ "restriction", "unchecked" })
	public static <T extends Enum<T>> T createEnum(Class<T> clazz, String name) {
		try {
			// which one is better? This?
			sun.misc.Unsafe unsafe = getUnsafe();
			T e = (T) unsafe.allocateInstance(clazz);
			
			// or this? (this one doesn't work with cheerpj beta 3)
 			// sun.reflect.ReflectionFactory f = sun.reflect.ReflectionFactory.getReflectionFactory();
			// Constructor c = f.newConstructorForSerialization(clazz);
			// T e = (T) c.newInstance();
			
			// in jdk9: replace this with VarHandle
			unsafe.putObject(e, unsafe.objectFieldOffset(Enum.class.getDeclaredField("name")), name);
			unsafe.putInt(e, unsafe.objectFieldOffset(Enum.class.getDeclaredField("ordinal")), Integer.MAX_VALUE);
			
			return e;
		} catch (Exception x) {
			throw new RuntimeException(x);
		} 
	}
	
	@SuppressWarnings("restriction")
	private static sun.misc.Unsafe getUnsafe() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		Field singleoneInstanceField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
		singleoneInstanceField.setAccessible(true);
		return (sun.misc.Unsafe) singleoneInstanceField.get(null);
	}
	
	public static <T extends Enum<T>> T getDefault(Class<T> enumClass) {
		return EnumUtils.valueList(enumClass).get(0);
	}
	
	public static <T extends Enum<T>> List<T> valueList(Class<T> enumClass) {
		try {
			Method method = enumClass.getMethod("values");
			@SuppressWarnings("unchecked")
			T[] values = (T[]) method.invoke(null);
			return Arrays.asList(values);
		} catch (Exception x) {
			x.printStackTrace();
			throw new RuntimeException(x);
		}
	}

	public static <T extends Enum<T>> String getText(T enumElement) {
		if (enumElement == null) {
			return null;
		}
		
		if (enumElement instanceof Rendering) {
			String text = ((Rendering) enumElement).render(RenderType.PLAIN_TEXT);
			if (text != null) {
				return text;
			}
		}
		
		String bundleName = enumElement.getClass().getName();
		while (true) {
			try {
				ResourceBundle resourceBundle = ResourceBundle.getBundle(bundleName, LocaleContext.getCurrent());
				return resourceBundle.getString(enumElement.name());
			} catch (MissingResourceException mre) {
				int pos = bundleName.lastIndexOf('$');
				if (pos < 0) return enumElement.name();
				bundleName = bundleName.substring(0, pos);
			}
		}
	}

	public static <T extends Enum<T>> String getDescription(T enumElement) {
		if (enumElement == null) {
			return null;
		}
		
		String bundleName = enumElement.getClass().getName();
		try {
			ResourceBundle resourceBundle = ResourceBundle.getBundle(bundleName);
			return resourceBundle.getString(enumElement.name() + ".tooltip");
		} catch (MissingResourceException mre) {
			return null;
		}
	}

	
//	private static <T> Map<Class<T>, List<CodeItem<T>>> itemLists = new HashMap<Class<T>, List<CodeItem<T>>>();
	@SuppressWarnings("rawtypes")
	private static Map itemLists = new HashMap();

	@SuppressWarnings("unchecked")
	public static <T extends Enum<T>> List<CodeItem<T>> itemList(Class<T> enumClass) {
		if (!itemLists.containsKey(enumClass)) {
			List<T> values = valueList(enumClass);
			List<CodeItem<T>> itemList = itemList(values);
			itemLists.put(enumClass, itemList);	
		}
		return (List<CodeItem<T>>) itemLists.get(enumClass);
	}
	
	public static <T extends Enum<T>> List<CodeItem<T>> itemList(List<T> values) {
		List<CodeItem<T>> itemList = new ArrayList<>(values.size());
		for (T value : values) {
			CodeItem<T> item = new CodeItem<T>(value, getText(value), getDescription(value));
			itemList.add(item);
		}
		return itemList;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static int getInt(Set set, Class enumClass) {
		List values = EnumUtils.valueList(enumClass);
		int bitValue = 1;
		int result = 0;
		for (Object v : values) {
			if (set.contains(v)) {
				result += bitValue;
			}
			bitValue = bitValue << 1;
		}
		return result;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void fillSet(int integer, Class enumClass, Set set) {
		List values = EnumUtils.valueList(enumClass);
		int bitValue = 1;
		for (Object v : values) {
			if ((integer & bitValue) != 0) {
				set.add(v);
			}
			bitValue = bitValue << 1;
		}
	}
	
}
