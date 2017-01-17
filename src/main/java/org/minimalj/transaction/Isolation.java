package org.minimalj.transaction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.Connection;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.PACKAGE})
public @interface Isolation {

	Level value();

	public static enum Level {
		NONE(Connection.TRANSACTION_NONE), READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED),
		READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED), REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ), SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE);
		
		private final int level;
		
		private Level(int level) {
			this.level = level;
		}
		
		public int getLevel() {
			return level;
		}
	}

}
