package org.minimalj.model.test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.minimalj.application.DevMode;
import org.minimalj.model.EnumUtils;
import org.minimalj.model.PropertyInterface;
import org.minimalj.model.ViewUtil;
import org.minimalj.model.annotation.AnnotationUtil;
import org.minimalj.model.properties.FlatProperties;
import org.minimalj.model.properties.Properties;
import org.minimalj.util.Codes;
import org.minimalj.util.FieldUtils;
import org.minimalj.util.GenericUtils;
import org.minimalj.util.StringUtils;
import org.minimalj.util.resources.Resources;

/**
 * Test some restricitions on model classes.<p>
 * 
 * These tests are called by JUnit tests but also by Persistence.
 * They are fast and its better to see problems at startup of an application.
 */
public class ModelTest {

	private static enum ModelClassType {
		MAIN, // a main class can have fields of a simple class, lists of list_elements
		SIMPLE, // as simple class can have fields of simple classes but no lists
		LIST_ELEMENT, // list element class cannot have a reference to simple classes or contain lists itself
		CODE; 
	}
	
	private final Map<Class<?>, ModelClassType> modelClassTypes = new HashMap<>();
	private Set<Class<?>> testedClasses = new HashSet<Class<?>>();
	
	private final List<String> problems = new ArrayList<String>();
	private final SortedSet<String> missingResources = new TreeSet<String>();
	
	public ModelTest(Class<?>... mainModelClasses) {
		this(Arrays.asList(mainModelClasses));
	}
	
	public ModelTest(Collection<Class<?>> mainModelClasses) {
		for (Class<?> clazz : mainModelClasses) {
			modelClassTypes.put(clazz, ModelClassType.MAIN);
		}
		for (Class<?> clazz : mainModelClasses) {
			testClass(clazz);
		}
	}
	
	public List<String> getProblems() {
		return problems;
	}
	
	public boolean isValid() {
		return problems.isEmpty();
	}

	private void testClass(Class<?> clazz) {
		if (!testedClasses.contains(clazz)) {
			testedClasses.add(clazz);
			testNoSuperclass(clazz);
			testId(clazz);
			testVersion(clazz);
			testConstructor(clazz);
			testFields(clazz);
			problems.addAll(FlatProperties.testProperties(clazz));
			if (DevMode.isActive()) {
				testResources(clazz);
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void testConstructor(Class<?> clazz)  {
		if (Enum.class.isAssignableFrom(clazz)) {
			try {
				EnumUtils.createEnum((Class<Enum>) clazz, "Test");
			} catch (Exception e) {
				problems.add("Not possible to create runtime instance of enum " + clazz.getName() + ". Possibly there is no empty constructor");
			}
		} else {
			try {
				Constructor<?> constructor = clazz.getConstructor();
				if (!Modifier.isPublic(constructor.getModifiers())) {
					problems.add("Constructor of " + clazz.getName() + " not public");
				}
			} catch (NoSuchMethodException e) {
				problems.add(clazz.getName() + " has no public empty constructor");
			}
		}
	}

	private boolean isMain(Class<?> clazz) {
		return modelClassTypes.get(clazz) == ModelClassType.MAIN;
	}
	
	private void testNoSuperclass(Class<?> clazz) {
		if (clazz.getSuperclass() != Object.class && (clazz.getSuperclass() != Enum.class || isMain(clazz))) {
			problems.add(clazz.getName() + ": Domain classes must not extends other classes");
		}
	}
				
	private void testId(Class<?> clazz) {
		ModelClassType modelClassType = modelClassTypes.get(clazz);
		try {
			PropertyInterface property = FlatProperties.getProperty(clazz, "id");
			switch (modelClassType) {
			case CODE:
				if (!FieldUtils.isAllowedId(property.getFieldClazz())) {
					problems.add(clazz.getName() + ": Code ids must be of Integer, String or Object");
				}
				break;
			case MAIN:
				break;
			default:
				problems.add(clazz.getName() + ": is not allowed to have an id field");
				break;
			}
		} catch (IllegalArgumentException e) {
			if (modelClassType == ModelClassType.MAIN || modelClassType == ModelClassType.CODE) {
				problems.add(clazz.getName() + ": Domain classes must have an id field of type int oder long");
			} 
		}
	}

	private void testVersion(Class<?> clazz) {
		try {
			Field fieldVersion = clazz.getField("version");
			if (isMain(clazz)) {
				if (fieldVersion.getType() == Integer.class) {
					problems.add(clazz.getName() + ": Domain classes version must be of primitiv type int");
				}
				if (!FieldUtils.isPublic(fieldVersion)) {
					problems.add(clazz.getName() + ": field version must be public");
				}
			} else {
				problems.add(clazz.getName() + ": Only domain classes are allowed to have an version field");
			}
		} catch (NoSuchFieldException e) {
			// thats ok, version is not mandatory
		} catch (SecurityException e) {
			problems.add(clazz.getName() + " makes SecurityException with the id field");
		}
	}
	
	private void testFields(Class<?> clazz) {
		Field[] fields = clazz.getFields();
		for (Field field : fields) {
			testField(field);
		}
	}

	private void testField(Field field) {
		if (FieldUtils.isPublic(field) && !FieldUtils.isStatic(field) && !FieldUtils.isTransient(field) && !field.getName().equals("id") && !field.getName().equals("version")) {
			testTypeOfField(field);
			testNoMethodsForPublicField(field);
			Class<?> fieldType = field.getType();
			if (fieldType == String.class) {
				testSize(field);
			} else if (List.class.equals(fieldType)) {
				if (!isMain(field.getDeclaringClass())) {
					String messagePrefix = field.getName() + " of " + field.getDeclaringClass().getName();
					problems.add(messagePrefix + ": not allowed. Only main model class or inline fields in these classes may contain lists");
				}
			}
			
		}
	}

	private void testTypeOfField(Field field) {
		Class<?> fieldType = field.getType();
		String messagePrefix = field.getName() + " of " + field.getDeclaringClass().getName();

		if (fieldType == List.class || fieldType == Set.class) {
			if (!FieldUtils.isFinal(field)) {
				problems.add(messagePrefix + " must be final (" + fieldType.getSimpleName() + " Fields must be final)");
			}
			if (fieldType == List.class) {
				testTypeOfListField(field, messagePrefix);
			} else if (fieldType == Set.class) {
				testTypeOfSetField(field, messagePrefix);
			}
		} else {
			testTypeOfField(field, messagePrefix);
		}
	}

	private void testTypeOfListField(Field field, String messagePrefix) {
		Class<?> listType = null;
		try {
			listType = GenericUtils.getGenericClass(field);
		} catch (Exception x) {
			// silent
		}
		if (listType != null) {
			messagePrefix = "Generic of " + messagePrefix;
			testTypeOfListField(listType, messagePrefix);
		} else {
			problems.add("Could not evaluate generic of " + messagePrefix);
		}
	}

	private void testTypeOfSetField(Field field, String messagePrefix) {
		@SuppressWarnings("rawtypes")
		Class setType = null;
		try {
			setType = GenericUtils.getGenericClass(field);
		} catch (Exception x) {
			// silent
		}
		if (setType != null) {
			if (!Enum.class.isAssignableFrom(setType)) {
				problems.add("Set type must be an enum class: " + messagePrefix);
			}
			@SuppressWarnings("unchecked")
			List<?> values = EnumUtils.itemList(setType);
			if (values.size() > 32) {
				problems.add("Set enum must not have more than 32 elements: " + messagePrefix);
			}
		} else {
			problems.add("Could not evaluate generic of " + messagePrefix);
		}
	}
	
	private void testTypeOfField(Field field, String messagePrefix) {
		Class<?> fieldType = field.getType();
		if (FieldUtils.isAllowedPrimitive(fieldType)) {
			return;
		}
		if (fieldType.isPrimitive()) {
			problems.add(messagePrefix + " has invalid Type");
		}
		if (Modifier.isAbstract(fieldType.getModifiers())) {
			problems.add(messagePrefix + " must not be of an abstract Type");
		}
		if (isMain(fieldType) && !ViewUtil.isView(field)) {
			problems.add(messagePrefix + " may not reference the other main model class " + fieldType.getSimpleName());
		}
		if (fieldType.isArray()) {
			problems.add(messagePrefix + " is an array which is not allowed");
		}
		if (Codes.isCode(fieldType)) {
			if (!modelClassTypes.containsKey(fieldType)) {
				modelClassTypes.put(fieldType, ModelClassType.CODE);
				testClass(fieldType);
			}
		}
		if (!ViewUtil.isView(field) && !FieldUtils.isFinal(field)) {
			if (!modelClassTypes.containsKey(fieldType)) {
				modelClassTypes.put(fieldType, ModelClassType.SIMPLE);
				testClass(fieldType);
			}
		}
	}

	private void testTypeOfListField(Class<?> fieldType, String messagePrefix) {
		if (fieldType.isPrimitive()) {
			problems.add(messagePrefix + " has invalid Type");
			return;
		}
		if (Modifier.isAbstract(fieldType.getModifiers())) {
			problems.add(messagePrefix + " must not be of an abstract Type");
			return;
		}
		if (fieldType.isArray()) {
			problems.add(messagePrefix + " is an array which is not allowed");
			return;
		}
		ModelClassType type = modelClassTypes.get(fieldType);
		if (type == null) {
			modelClassTypes.put(fieldType, ModelClassType.LIST_ELEMENT);
			testClass(fieldType);
			return;
		}
		// report problem
		switch (type) {
		case MAIN:
			problems.add(messagePrefix + " is a list of other main objects which is not allowed");
			break;
		case CODE:
			problems.add(messagePrefix + " is a list of codes which is not allowed");
			break;
		case SIMPLE:
		case LIST_ELEMENT:
			// no problem
			break;
		}
	}
	
	private void testSize(Field field) {
		PropertyInterface property = Properties.getProperty(field);
		try {
			AnnotationUtil.getSize(property);
		} catch (IllegalArgumentException x) {
			problems.add("Missing size for: " + property.getDeclaringClass().getName() + "." + property.getFieldPath());
		}
	}
		
	private void testNoMethodsForPublicField(Field field) {
		PropertyInterface property = Properties.getProperty(field);
		if (property != null) {
			if (property.getClass().getSimpleName().startsWith("Method")) {
				problems.add("A public attribute must not have getter or setter methods: " + field.getDeclaringClass().getName() + "." + field.getName());
			}
		} else {
			problems.add("No property for " + field.getName());
		}
	}
	
	private void testResources(Class<?> clazz) {
		for (PropertyInterface property : FlatProperties.getProperties(clazz).values()) {
			if (StringUtils.equals(property.getFieldName(), "id", "version")) continue;
			String resourceText = Resources.getObjectFieldName(Resources.getResourceBundle(), property);
			if (resourceText.startsWith("!!")) {
				missingResources.add(resourceText.substring(2));
			}
		}
	}
	
	public void printMissingResources() {
		for (String key : missingResources) {
			System.out.println(key + " = ");
		}
	}

}
