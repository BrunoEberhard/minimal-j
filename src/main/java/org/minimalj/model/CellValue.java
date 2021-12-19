package org.minimalj.model;

import java.time.LocalTime;
import java.util.Objects;
import java.util.function.Function;

// may implement runnable
@SuppressWarnings("rawtypes")
public class CellValue<T extends Comparable> implements Rendering, Comparable<CellValue<T>> {

	private final T value;
	
	public CellValue() {
		this(null);
		// for Keys
	}
	
	public CellValue(T value) {
		this.value = value;
		if (value instanceof LocalTime) {
			throw new IllegalArgumentException("CellValue does not work with LocalTime because for rendering the information about seconds visible or not is needed");
		}
	}

	protected final T getValue() {
		return value;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public int compareTo(CellValue<T> o) {
		if (getValue() == null) {
			return o.getValue() == null ? 0 : 1;
		} else if (o.getValue() == null) {
			return -1;
		} else {
			return getValue().compareTo(o.getValue());
		}
	}
	
	@Override
	public ColorName getColor() {
		return value instanceof Rendering ? ((Rendering) value).getColor() : null;
	}

	@Override
	public CharSequence render() {
		return Rendering.render(getValue());
	}

	public static class FormatCellValue<T extends Comparable> extends CellValue<T> {
		private final Function<T, CharSequence> formatter;
		private final ColorName color;
		
		public FormatCellValue() {
			this(null, Rendering::render, null);
			// for Keys
		}

		public FormatCellValue(T value, Function<T, CharSequence> formatter) {
			this(value, formatter, null);
		}

		public FormatCellValue(T value, ColorName color) {
			this(value, Rendering::render, color);
		}

		public FormatCellValue(T value, Function<T, CharSequence> formatter, ColorName color) {
			super(value);
			this.formatter = Objects.requireNonNull(formatter);
			this.color = color;
		}
		
		@Override
		public CharSequence render() {
			return formatter.apply(getValue());
		}
		
		@Override
		public ColorName getColor() {
			return color;
		}
	}
	
	
//	public class CellAction<T extends Comparable> extends CellValue {
//		private final Runnable runnable;
//		
//		public CellAction(Comparable value, Runnable runnable) {
//			super(value);
//			this.runnable = runnable;
//		}
//		
//		public Runnable getRunnable() {
//			return runnable;
//		}
//	}
}
