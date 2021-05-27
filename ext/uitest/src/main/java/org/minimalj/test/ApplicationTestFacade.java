package org.minimalj.test;

import org.minimalj.test.FrameTestFacade.PageContainerTestFacade;
import org.minimalj.test.FrameTestFacade.UserPasswordLoginTestFacade;

public interface ApplicationTestFacade {

	UserPasswordLoginTestFacade getLoginTestFacade();
	
	void logout();

	PageContainerTestFacade getCurrentWindowTestFacade();

}
