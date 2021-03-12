package org.minimalj.frontend.test;

import org.minimalj.frontend.test.FrameTestFacade.UserPasswordLoginTestFacade;
import org.minimalj.frontend.test.FrameTestFacade.PageContainerTestFacade;

public interface ApplicationTestFacade {

	UserPasswordLoginTestFacade getLoginTestFacade();

	PageContainerTestFacade getCurrentWindowTestFacade();

}
