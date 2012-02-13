package ch.openech.mj.edit.value;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Logger;

public class CloneHelper {
	private static Logger logger = Logger.getLogger(CloneHelper.class.getName());

	@SuppressWarnings("unchecked")
	public static <T> T cloneIfPossible(T object) {
		if (object instanceof Cloneable) {
			try {
				Method cloneMethod = object.getClass().getMethod("clone");
				try {
					return (T) cloneMethod.invoke(object);
				} catch (IllegalArgumentException e) {
					logger.severe("Clone() of Object " + object + " seems to have arguments");
				} catch (IllegalAccessException e) {
					logger.severe("Access to clone() of Object " + object + " not possible");
				} catch (InvocationTargetException e) {
					logger.severe("Invocation clone() of Object " + object + " failed");
				}
			} catch (SecurityException e) {
				logger.severe("Access to clone() of Object " + object + " not possible");
			} catch (NoSuchMethodException e) {
				logger.severe("Object " + object + " implements Cloneable but no method clone()");
			}
		}
		return object;
	}
}
