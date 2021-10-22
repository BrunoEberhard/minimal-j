package org.minimalj.test;

import org.minimalj.test.LoginFrameFacade.UserPasswordLoginTestFacade;

public interface ApplicationTestFacade {

	UserPasswordLoginTestFacade getLoginTestFacade();
	
	void logout();

	PageContainerTestFacade getCurrentPageContainerTestFacade();

}
