package org.minimalj.thymeleaf.sandbox;

import org.junit.Assert;
import org.junit.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

public class ThymeVariableTest {

	
	public static class A {
		public String text;
		public B b;
	}

	public static class B {
		public String text;
	}

	@Test
	public void testEmpty() {
		TemplateEngine templEngine = new TemplateEngine();
		Context context = new Context();
		A a = new A();
		a.b = new B();
		a.b.text = "Test";
		context.setVariable("a", a);
		String output = templEngine.process("abcdef [[${a.b.text}]]", context);
		Assert.assertEquals("abcdef Test", output);
	}
}
