package org.minimalj.frontend.form.element;

import java.util.Collection;

public class FormElementConstraint {

	public static final int MAX = 1000;

	public final int min, max;

	public FormElementConstraint(int min, int max) {
		this.min = min;
		this.max = max;

		if (min < 0 || min > MAX) {
			throw new IllegalArgumentException("Invalid min height: " + min);
		}
		if (max > MAX) {
			throw new IllegalArgumentException("Invalid max height: " + max);
		}
		if (max < min) {
			throw new IllegalArgumentException("Max height might not be smaller than min height " + min + ">" + max);
		}
	}

	public FormElementConstraint(Collection<FormElementConstraint> constraints) {
		int min = 0;
		int max = 0;
		for (FormElementConstraint c : constraints) {
			min = Math.max(min, c.min);
			max = Math.max(max, c.max);
		}
		this.min = min;
		this.max = max;
	}	 

}

