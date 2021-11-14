package org.minimalj.test;

import org.minimalj.test.LoginFrameFacade.UserPasswordLoginTestFacade;

public interface UiTestFacade {

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
