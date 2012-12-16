package ch.openech.mj.model.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import ch.openech.mj.db.model.Constants;
import ch.openech.mj.db.model.PropertyInterface;
import ch.openech.mj.util.FieldUtils;

public class AnnotationUtil {
	
	public static int getSize(PropertyInterface property) {

		Size size = property.getAnnotation(Size.class);
		if (size != null) {
			return size.value();
		}
		
		Sizes sizes = property.getDeclaringClass().getAnnotation(Sizes.class);
		if (sizes != null) {
			Class<?> sizeClass = sizes.value();
			for (Field field : sizeClass.getFields()) {
				if (field.getType() != Integer.TYPE) continue;
				if (!FieldUtils.isStatic(field)) continue;
				if (!field.getName().equals(property.getFieldName())) continue;
				try {
					return field.getInt(null);
				} catch (Exception x) {
					throw new RuntimeException(x);
				}
			}
		}
		throw new IllegalArgumentException("Size not specified for " + property.getFieldName() + " on " + property.getDeclaringClass());
	}

	public static int getDecimal(PropertyInterface property) {
		Decimal decimal = property.getAnnotation(Decimal.class);
		if (decimal != null) {
			return decimal.value();
		} else {
			return 0;
		}
	}

	public static <T extends Annotation> T get(Class<T> annotationClass, Class<?> clazz, Object key) {
		PropertyInterface property = Constants.getProperty(key);
		return property.getAnnotation(annotationClass);
	}
		
	public static boolean isNegative(PropertyInterface property) {
		return property.getAnnotation(Negative.class) != null;
	}
	
	public static boolean isPartialDate(PropertyInterface property) {
		return property.getAnnotation(PartialDate.class) != null;
	}

}
