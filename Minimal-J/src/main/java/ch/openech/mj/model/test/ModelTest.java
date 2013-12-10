package ch.openech.mj.model.test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.ReadablePartial;

import ch.openech.mj.application.DevMode;
import ch.openech.mj.model.EnumUtils;
import ch.openech.mj.model.PropertyInterface;
import ch.openech.mj.model.annotation.AnnotationUtil;
import ch.openech.mj.model.properties.FlatProperties;
import ch.openech.mj.model.properties.Properties;
import ch.openech.mj.resources.Resources;
import ch.openech.mj.util.FieldUtils;
import ch.openech.mj.util.GenericUtils;

/**
 * Test some restricitions on model classes.<p>
 * 
 * These tests are called by JUnit tests but also by DbPersistence.
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
			testDomainClassCheckRecursion(clazz);
		}
	}
	
	public List<String> getProblems() {
		return problems;
	}

	private void testDomainClassCheckRecursion(Class<?> clazz) {
		if (!testedClasses.contains(clazz)) {
			testedClasses.add(clazz);
			testConstructor(clazz);
			testFields(clazz);
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
		} else if (fieldType == Set.class) {
			if (!FieldUtils.isFinal(field)) {
				problems.add(messagePrefix + " must be final (Set Fields must be final)");
			}
			testSetFieldType(field, messagePrefix);
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

	private void testSetFieldType(Field field, String messagePrefix) {
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
			List values = EnumUtils.itemList(setType);
			if (values.size() > 32) {
				problems.add("Set enum must not have more than 32 elements: " + messagePrefix);
			}
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
			if (mainModelClasses.contains(fieldType)) {
				problems.add(messagePrefix + " may not reference the other main model class " + fieldType.getSimpleName());
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
	
	private void testResources(Class<?> clazz) {
		for (PropertyInterface property : FlatProperties.getProperties(clazz).values()) {
			String resourceText = Resources.getObjectFieldName(Resources.getResourceBundle(), property);
			if (resourceText.startsWith("!!")) {
				System.out.println(clazz.getSimpleName() + "." + resourceText.substring(2) + " = ");
			}
		}
	}

}
