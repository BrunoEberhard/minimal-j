package ch.openech.mj.model.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Size {

	int value();
	
	public static class SizeImpl implements Size {

		private final int size;
		
		public SizeImpl(int size) {
			this.size = size;
		}

		@Override
		public Class<? extends Annotation> annotationType() {
			return Size.class;
		}

		@Override
		public int value() {
			return size;
		}
	}
	
	
}
