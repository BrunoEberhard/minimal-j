package ch.openech.mj.model.test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.ReadablePartial;

import ch.openech.mj.edit.value.Properties;
import ch.openech.mj.model.EnumUtils;
import ch.openech.mj.model.PropertyInterface;
import ch.openech.mj.model.annotation.AnnotationUtil;
import ch.openech.mj.util.FieldUtils;
import ch.openech.mj.util.GenericUtils;

public class ModelTest {

	private Set<Class<?>> testedClasses = new HashSet<Class<?>>();
	
	private final List<String> problems = new ArrayList<String>();
	
	public void test(Class<?> clazz) {
		testedClasses.clear();
		testDomainClassCheckRecursion(clazz);
	}
	
	public List<String> getProblems() {
		return problems;
	}

	private void testDomainClassCheckRecursion(Class<?> clazz) {
		if (!testedClasses.contains(clazz)) {
			testedClasses.add(clazz);
			testConstructor(clazz);
			testFields(clazz);
		}
	}

	private void testConstructor(Class<?> clazz)  {
		if (Enum.class.isAssignableFrom(clazz)) {
			try {
				EnumUtils.createEnum((Class<Enum>) clazz, "Test");
			} catch (Exception e) {
				problems.add("Not possible to create runtime instance of enum " + clazz.getName() + ". Possibly ther is no empty constructor");
			}
		} else {
			try {
				if (!Modifier.isPublic(clazz.getConstructor().getModifiers())) {
					problems.add("Constructor of " + clazz.getName() + " not public");
				}
			} catch (NoSuchMethodException e) {
				problems.add(clazz.getName() + " has no public empty constructor");
			}
		}
	}
	
	private void testFields(Class<?> clazz) {
		Field[] fields = clazz.getFields();
		for (Field field : fields) {
			testField(field);
		}
	}

	private void testField(Field field) {
		if (FieldUtils.isPublic(field) && !FieldUtils.isStatic(field) && !FieldUtils.isTransient(field)) {
			testFieldType(field);
			testNoMethodsForPublicField(field);
			if (String.class.equals(field.getType())) {
				testSize(field);
			}
		}
	}

	private void testFieldType(Field field) {
		Class<?> fieldType = field.getType();
		String messagePrefix = field.getName() + " of " + field.getDeclaringClass().getName();

		if (fieldType == List.class) {
			if (!FieldUtils.isFinal(field)) {
				problems.add(messagePrefix + " must be final (List Fields must be final)");
			}
			testListFieldType(field, messagePrefix);
		} else {
			testFieldType(fieldType, messagePrefix);
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
			testFieldType(listType, messagePrefix);
		} else {
			problems.add("Could not evaluate generic of " + messagePrefix);
		}
	}
	
	private void testFieldType(Class<?> fieldType, String messagePrefix) {
		if (!isAllowedPrimitive(fieldType)) {
			if (fieldType.isPrimitive()) {
				problems.add(messagePrefix + " has invalid Type");
			}
			if (Modifier.isAbstract(fieldType.getModifiers())) {
				problems.add(messagePrefix + " must not be of an abstract Type");
			}
			testDomainClassCheckRecursion(fieldType);
		}
	}

	private static boolean isAllowedPrimitive(Class<?> fieldType) {
		if (String.class == fieldType) return true;
		if (Integer.class == fieldType) return true;
		if (Boolean.class == fieldType) return true;
		if (BigDecimal.class == fieldType) return true;
		if (LocalDate.class == fieldType) return true;
		if (LocalTime.class == fieldType) return true;
		if (LocalDateTime.class == fieldType) return true;
		if (ReadablePartial.class == fieldType) return true;
		return false;
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
}
