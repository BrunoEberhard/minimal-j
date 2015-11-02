package org.minimalj.example.helloworld2;

import java.util.Collections;
import java.util.List;

import org.minimalj.application.Application;
import org.minimalj.frontend.action.Action;

public class GreetingApplication extends Application {

	@Override
	public List<Action> getNavigation() {
		return Collections.singletonList(new UserNameEditor());
	}
	
}
