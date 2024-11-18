package org.minimalj.test;

import org.minimalj.application.Application;
import org.minimalj.test.LoginFrameFacade.UserPasswordLoginTestFacade;

public interface UiTestFacade {

	void start(Application application);
	
	UserPasswordLoginTestFacade getLoginTestFacade();
	
	void logout();

	PageContainerTestFacade getCurrentPageContainerTestFacade();

	public default void login(String user, String password) {
		UserPasswordLoginTestFacade userPasswordLogin = getLoginTestFacade();

		userPasswordLogin.setUser(user);
		userPasswordLogin.setPassword(password);
		userPasswordLogin.login();
	}


}
