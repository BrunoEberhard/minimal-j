package org.minimalj.repository.sql.view;

import org.minimalj.model.Keys;
import org.minimalj.model.View;

public class TestView implements View<TestEntity>{

	public static final TestView $ = Keys.of(TestView.class);

	public TestView() {
		// needed for reflection constructor
	}

	public TestView(String name) {
		this.name = name;
	}

	public Object id;

	public String name;

	public String doubleName;
	
}
