package org.minimalj.example.helloworld2;

import java.util.Collections;
import java.util.List;

import org.minimalj.application.Application;
import org.minimalj.frontend.toolkit.Action;

public class GreetingApplication extends Application {

	@Override
	public List<Action> getMenu() {
		return Collections.singletonList(new UserNameEditor());
	}
	
}
