package org.minimalj.example.helloworld3;

import java.util.Collections;
import java.util.List;

import org.minimalj.application.Application;
import org.minimalj.frontend.action.Action;

public class GreetingApplication extends Application {

	static User user = new User();
	
	@Override
	public List<Action> getMenu() {
		return Collections.singletonList(new UserNameEditor());
	}
	
}
