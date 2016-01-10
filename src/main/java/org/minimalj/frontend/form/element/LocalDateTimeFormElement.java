package org.minimalj.frontend.form.element;

import java.lang.annotation.Annotation;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IComponent;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.model.properties.VirtualProperty;
import org.minimalj.util.ChangeListener;

public class LocalDateTimeFormElement extends AbstractFormElement<LocalDateTime> {

	private final LocalDateFormElement dateFormElement;
	private final LocalTimeFormElement timeFormElement;
	private final IComponent component;
	
	public LocalDateTimeFormElement(PropertyInterface property, boolean editable) {
		super(property);
		dateFormElement = new LocalDateFormElement(new LocalDateProperty(), editable);
		timeFormElement = new LocalTimeFormElement(new LocalTimeProperty(), editable);
		component = Frontend.getInstance().createComponentGroup(dateFormElement.getComponent(), timeFormElement.getComponent());
	}
	
	@Override
	public void setValue(LocalDateTime value) {
		dateFormElement.setValue(value.toLocalDate());
		timeFormElement.setValue(value.toLocalTime());
	}

	@Override
	public LocalDateTime getValue() {
		LocalDateTime value = LocalDateTime.of(dateFormElement.getValue(), timeFormElement.getValue());
		return value;
	}

	@Override
	public void setChangeListener(ChangeListener<FormElement<?>> changeListener) {
		dateFormElement.setChangeListener(changeListener);
		timeFormElement.setChangeListener(changeListener);
	}
	
	@Override
	public IComponent getComponent() {
		return component;
	}

	public class LocalDateProperty extends VirtualProperty {
		@Override
		public String getName() {
			return "date";
		}

		@Override
		public Class<?> getClazz() {
			return LocalDate.class;
		}

		@Override
		public Object getValue(Object object) {
			return LocalDateTimeFormElement.this.getValue().toLocalDate();
		}

		@Override
		public void setValue(Object object, Object value) {
			LocalDateTimeFormElement.this.setValue(LocalDateTime.of((LocalDate) value, LocalDateTimeFormElement.this.getValue().toLocalTime()));
		}
	}
	
	public class LocalTimeProperty extends VirtualProperty {
		@Override
		public String getName() {
			return "time";
		}

		@Override
		public Class<?> getClazz() {
			return LocalTime.class;
		}

		@Override
		public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
			return LocalDateTimeFormElement.this.getProperty().getAnnotation(annotationClass);
		}

		@Override
		public Object getValue(Object object) {
			return LocalDateTimeFormElement.this.getValue().toLocalTime();
		}

		@Override
		public void setValue(Object object, Object value) {
			LocalDateTimeFormElement.this.setValue(LocalDateTime.of(LocalDateTimeFormElement.this.getValue().toLocalDate(), (LocalTime) value));
		}
	}
}
