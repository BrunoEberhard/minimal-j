package org.minimalj.transaction.predicate;

import java.io.Serializable;
import java.util.function.Predicate;

public abstract class PersistencePredicate<T> implements Predicate<T> {

	@Override
	public Predicate<T> and(Predicate<? super T> other) {
		if (other != null) {
			return new AndPredicate<T>(this, other);
		} else {
			return this;
		}
	}

	@Override
	public Predicate<T> or(Predicate<? super T> other) {
		if (other != null) {
			return new OrPredicate<T>(this, other);
		} else {
			return this;
		}
	}
	
	public abstract int getLevel();
	
	public static abstract class CombinedPredicate<T> extends PersistencePredicate<T> implements Serializable {
		private static final long serialVersionUID = 1L;

		private final Predicate<? super T> predicate1;
		private final Predicate<? super T> predicate2;
		
		public CombinedPredicate(Predicate<? super T> predicate1, Predicate<? super T> predicate2) {
			this.predicate1 = predicate1;
			this.predicate2 = predicate2;
		}

		public Predicate<? super T> getPredicate1() {
			return predicate1;
		}
		
		public Predicate<? super T> getPredicate2() {
			return predicate2;
		}
		
		@Override
		public Predicate<T> negate() {
			throw new IllegalArgumentException("negate not supported for " + this.getClass().getSimpleName());
		}
	}
	
	public static class OrPredicate<T> extends CombinedPredicate<T> implements Serializable {
		private static final long serialVersionUID = 1L;

		public OrPredicate(Predicate<? super T> predicate1, Predicate<? super T> predicate2) {
			super(predicate1, predicate2);
		}

		@Override
		public int getLevel() {
			return -1;
		}

		@Override
		public boolean test(T t) {
			return getPredicate1().test(t) || getPredicate2().test(t);
		}
	}	
	
	public static class AndPredicate<T> extends CombinedPredicate<T> implements Serializable {
		private static final long serialVersionUID = 1L;

		public AndPredicate(Predicate<? super T> predicate1, Predicate<? super T> predicate2) {
			super(predicate1, predicate2);
		}

		@Override
		public int getLevel() {
			return 1;
		}

		@Override
		public boolean test(T t) {
			return getPredicate1().test(t) && getPredicate2().test(t);
		}
	}	
}
