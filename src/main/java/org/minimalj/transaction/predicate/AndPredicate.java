package org.minimalj.transaction.predicate;

import java.io.Serializable;
import java.util.function.Predicate;

public class AndPredicate<T> implements Predicate<T>, Serializable {
	private static final long serialVersionUID = 1L;

	private final Predicate<? super T> predicate1;
	private final Predicate<? super T> predicate2;
	
	public AndPredicate(Predicate<? super T> predicate1, Predicate<? super T> predicate2) {
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
	public boolean test(T t) {
		return predicate1.test(t) && predicate2.test(t);
	}

}
