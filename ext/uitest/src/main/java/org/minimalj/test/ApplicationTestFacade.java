package org.minimalj.test;

import org.minimalj.test.FrameTestFacade.PageContainerTestFacade;
import org.minimalj.test.FrameTestFacade.UserPasswordLoginTestFacade;

public interface ApplicationTestFacade {

	UserPasswordLoginTestFacade getLoginTestFacade();

	PageContainerTestFacade getCurrentWindowTestFacade();

}
