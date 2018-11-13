package org.minimalj.frontend.editor;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.minimalj.TestApplication;
import org.minimalj.application.Application;
import org.minimalj.model.annotation.NotEmpty;
import org.minimalj.model.annotation.Size;
import org.minimalj.model.validation.InvalidValues;
import org.minimalj.model.validation.Validation;
import org.minimalj.model.validation.ValidationMessage;

public class ValidatorTest {

	@BeforeClass
	public static void initApplication() {
		Application.setInstance(TestApplication.INSTANCE);
	}

	@Test
	public void testValid() {
		TestClassA a = new TestClassA();
		a.s = "String";
		List<ValidationMessage> messages = Validator.validate(a);
		Assert.assertTrue("There should be not validation message", messages.size() == 0);
	}

	@Test
	public void testEmpty() {
		TestClassA a = new TestClassA();
		List<ValidationMessage> messages = Validator.validate(a);
		Assert.assertTrue("There should be a validation message about not empty", messages.size() == 1);
	}

	@Test
	public void testStringSize() {
		TestClassA a = new TestClassA();
		a.s = "123456789012345678901";
		List<ValidationMessage> messages = Validator.validate(a);
		Assert.assertTrue("There should be a validation message about size", messages.size() == 1);
	}

	@Test
	public void testInvalidString() {
		TestClassA a = new TestClassA();
		a.s = InvalidValues.createInvalidString("invalid");
		List<ValidationMessage> messages = Validator.validate(a);
		Assert.assertTrue("There should be a validation message about invalid value", messages.size() == 1);
	}

	@Test
	public void testInnterEmpty() {
		TestClassB b = new TestClassB();
		b.a = new TestClassA();
		List<ValidationMessage> messages = Validator.validate(b);
		Assert.assertTrue("There should be a validation message about not empty", messages.size() == 1);
	}

	@Test
	public void testInnerStringSize() {
		TestClassB b = new TestClassB();
		b.a = new TestClassA();
		b.a.s = "123456789012345678901";
		List<ValidationMessage> messages = Validator.validate(b);
		Assert.assertTrue("There should be a validation message about size", messages.size() == 1);
	}

	@Test
	public void testInnerInvalidString() {
		TestClassB b = new TestClassB();
		b.a = new TestClassA();
		b.a.s = InvalidValues.createInvalidString("invalid");
		List<ValidationMessage> messages = Validator.validate(b);
		Assert.assertTrue("There should be a validation message about invalid value", messages.size() == 1);
	}

	@Test
	public void testInnerValidation() {
		TestClassB b = new TestClassB();
		b.a = new TestClassA();
		b.a.s = "x";
		List<ValidationMessage> messages = Validator.validate(b);
		Assert.assertTrue("There should be a validation message about validation", messages.size() == 1);
	}

	@Test
	public void testListWithValidElement() {
		TestClassB b = new TestClassB();
		TestClassA a = new TestClassA();
		a.s = "a string";
		b.list.add(a);
		List<ValidationMessage> messages = Validator.validate(b);
		Assert.assertTrue("There should be no validation message for a valid element", messages.isEmpty());
	}

	@Test
	public void testListWithOneElementWithInvalidString() {
		TestClassB b = new TestClassB();
		TestClassA a = new TestClassA();
		a.s = InvalidValues.createInvalidString("invalid");
		b.list.add(a);
		List<ValidationMessage> messages = Validator.validate(b);
		Assert.assertTrue("There should be a validation message for a invalid element", messages.size() == 1);
	}

	@Test
	public void testListWithOneInvalidElement() {
		TestClassB b = new TestClassB();
		TestClassA a = new TestClassA();
		a.s = "x";
		b.list.add(a);
		List<ValidationMessage> messages = Validator.validate(b);
		Assert.assertTrue("There should be a validation message for a invalid element", messages.size() == 1);
	}


	@Test
	public void testListWithTwoInvalidElement() {
		TestClassB b = new TestClassB();
		TestClassA a = new TestClassA();
		a.s = InvalidValues.createInvalidString("invalid 1");
		b.list.add(a);
		a = new TestClassA();
		a.s = InvalidValues.createInvalidString("invalid 2");
		b.list.add(a);
		List<ValidationMessage> messages = Validator.validate(b);
		Assert.assertTrue("There should be a validation message for each invalid element", messages.size() == 2);
	}

	public static class TestClassA implements Validation {
		@NotEmpty
		@Size(20)
		public String s;

		@Override
		public List<ValidationMessage> validate() {
			if ("x".equals(s)) {
				return Validation.message("a", "Not valid");
			} else {
				return null;
			}
		}
	}

	public static class TestClassB {
		public TestClassA a;
		public List<TestClassA> list = new ArrayList<>();
	}

}
