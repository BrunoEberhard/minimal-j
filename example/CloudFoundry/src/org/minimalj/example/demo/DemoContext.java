package org.minimalj.example.demo;

public class DemoContext {

	private static final ThreadLocal<String> context = new ThreadLocal<>();
	
	public static String getContext() {
		return context.get();
	}
	
	public static void setContext(String context) {
		DemoContext.context.set(context);
	}
	
	
}
