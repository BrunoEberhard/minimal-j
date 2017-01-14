package org.minimalj.transaction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.PACKAGE})
public @interface Isolation {

	Level value();

	public static enum Level {
		NONE(0), READ_UNCOMMITTED(1), READ_COMMITTED(2), REPEATABLE_READ(4), SERIALIZABLE(8);
		
		private final int level;
		
		private Level(int level) {
			this.level = level;
		}
		
		public int getLevel() {
			return level;
		}
	}

}
