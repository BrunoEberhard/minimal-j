package org.minimalj.model.test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.minimalj.application.DevMode;
import org.minimalj.model.EnumUtils;
import org.minimalj.model.PropertyInterface;
import org.minimalj.model.ViewUtil;
import org.minimalj.model.annotation.AnnotationUtil;
import org.minimalj.model.annotation.ViewOf;
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

	private final Collection<Class<?>> mainModelClasses;
	private Set<Class<?>> testedClasses = new HashSet<Class<?>>();
	
	private final List<String> problems = new ArrayList<String>();
	
	public ModelTest(Class<?>... mainModelClasses) {
		this(Arrays.asList(mainModelClasses));
	}
	
	public ModelTest(Collection<Class<?>> mainModelClasses) {
		this.mainModelClasses = mainModelClasses;
		test();
	}
	
	private void test() {
		testedClasses.clear();
		for (Class<?> clazz : mainModelClasses) {
			testClass(clazz, false);
		}
	}
	
	public List<String> getProblems() {
		return problems;
	}
	
	public boolean isValid() {
		return problems.isEmpty();
	}

	private void testClass(Class<?> clazz, boolean isListElement) {
		if (!testedClasses.contains(clazz)) {
			testedClasses.add(clazz);
			testNoSuperclass(clazz);
			testId(clazz, isListElement);
			testVersion(clazz);
			testConstructor(clazz);
			testFields(clazz, isListElement);
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
				} else {
					try {
						// this ensures that static initializations are done before check of annotations
						constructor.newInstance();
					} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						problems.add(clazz.getName() + " constructor of empty constructor failed");
					}
				}
			} catch (NoSuchMethodException e) {
				problems.add(clazz.getName() + " has no public empty constructor");
			}
		}
	}

	private void testNoSuperclass(Class<?> clazz) {
		boolean isMainModelClass = mainModelClasses.contains(clazz);
		if (clazz.getSuperclass() != Object.class && (clazz.getSuperclass() != Enum.class || isMainModelClass)) {
			problems.add(clazz.getName() + ": Domain classes must not extends other classes");
		}
	}
				
	private void testId(Class<?> clazz, boolean isListElement) {
		try {
			Field fieldId = clazz.getField("id");
			if (mainModelClasses.contains(clazz) || Codes.isCode(clazz)) {
				if (!FieldUtils.isAllowedId(fieldId.getType())) {
					problems.add(clazz.getName() + ": Domain classes ids must be of Integer, Long, String or UUID");
				}
				if (!FieldUtils.isPublic(fieldId)) {
					problems.add(clazz.getName() + ": field id must be public");
				}
			} else if (isListElement) {
				problems.add(clazz.getName() + ": Only domain classes are allowed to have an id field");
			}
		} catch (NoSuchFieldException e) {
			if (mainModelClasses.contains(clazz)) {
				problems.add(clazz.getName() + ": Domain classes must have an id field of type int oder long");
			} 
		} catch (SecurityException e) {
			problems.add(clazz.getName() + " makes SecurityException with the id field");
		}
	}

	private void testVersion(Class<?> clazz) {
		try {
			Field fieldVersion = clazz.getField("version");
			if (mainModelClasses.contains(clazz)) {
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
	
	private void testFields(Class<?> clazz, boolean isListElement) {
		Field[] fields = clazz.getFields();
		for (Field field : fields) {
			testField(field, isListElement);
		}
	}

	private void testField(Field field, boolean isListElement) {
		if (FieldUtils.isPublic(field) && !FieldUtils.isStatic(field) && !FieldUtils.isTransient(field) && !field.getName().equals("id") && !field.getName().equals("version")) {
			testFieldType(field);
			testNoMethodsForPublicField(field);
			Class<?> fieldType = field.getType();
			if (fieldType == String.class) {
				testSize(field);
			} else if (List.class.equals(fieldType) && isListElement) {
				String messagePrefix = field.getName() + " of " + field.getDeclaringClass().getName();
				problems.add(messagePrefix + ": not allowed. Only main model class or inline fields in these classes may contain lists");
			}
			
		}
	}

	private void testFieldType(Field field) {
		Class<?> fieldType = field.getType();
		String messagePrefix = field.getName() + " of " + field.getDeclaringClass().getName();

		if (fieldType == List.class || fieldType == Set.class) {
			if (!FieldUtils.isFinal(field)) {
				problems.add(messagePrefix + " must be final (" + fieldType.getSimpleName() + " Fields must be final)");
			}
			if (fieldType == List.class) {
				testListFieldType(field, messagePrefix);
			} else if (fieldType == Set.class) {
				testSetFieldType(field, messagePrefix);
			}
		} else if (!ViewUtil.isView(field)) {
			testFieldType(fieldType, messagePrefix, false);
			// auf leeren Konstruktor prüfen?
		}
	}

	private void testListFieldType(Field field, String messagePrefix) {
		Class<?> listType = null;
		try {
			listType = GenericUtils.getGenericClass(field);
		} catch (Exception x) {
			// silent
		}
		if (listType != null) {
			messagePrefix = "Generic of " + messagePrefix;
			testFieldType(listType, messagePrefix, true);
		} else {
			problems.add("Could not evaluate generic of " + messagePrefix);
		}
	}

	private void testSetFieldType(Field field, String messagePrefix) {
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
	
	private void testFieldType(Class<?> fieldType, String messagePrefix, boolean isListElement) {
		if (!FieldUtils.isAllowedPrimitive(fieldType)) {
			if (fieldType.isPrimitive()) {
				problems.add(messagePrefix + " has invalid Type");
			}
			if (Modifier.isAbstract(fieldType.getModifiers())) {
				problems.add(messagePrefix + " must not be of an abstract Type");
			}
			if (mainModelClasses.contains(fieldType) && !ViewOf.class.isAssignableFrom(fieldType)) {
				problems.add(messagePrefix + " may not reference the other main model class " + fieldType.getSimpleName());
			}
			if (fieldType.isArray()) {
				problems.add(messagePrefix + " is an array which is not allowed");
			}
			testClass(fieldType, isListElement);
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
				System.out.println(clazz.getSimpleName() + "." + resourceText.substring(2) + " = ");
			}
		}
	}

}
