package org.minimalj.example.numbers;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.application.Application;
import org.minimalj.frontend.action.Action;

public class NumbersApplication extends Application {

	public NumbersApplication() {
	}

	@Override
	public List<Action> getMenu() {
		List<Action> items = new ArrayList<>();
		items.add(new NumbersEditor());
		return items;
	}

}
