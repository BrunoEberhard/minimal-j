package org.minimalj.test.headless;

import org.minimalj.application.Application;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.impl.json.JsonFrontend;
import org.minimalj.test.LoginFrameFacade.UserPasswordLoginTestFacade;
import org.minimalj.test.PageContainerTestFacade;
import org.minimalj.test.PageContainerTestFacade.DialogTestFacade;
import org.minimalj.test.UiTestFacade;
import org.minimalj.util.resources.Resources;

public class HeadlessTestFacade implements UiTestFacade {
	
	@Override
	public void start(Application application) {
		Frontend.setInstance(new HeadlessFrontend());
		Application.setInstance(application);
		Frontend.getInstance().login(null);
		JsonFrontend.setUseInputTypes(false);
	}
	
	@Override
	public UserPasswordLoginTestFacade getLoginTestFacade() {
		return new HeadlessLoginTestFacade(getCurrentPageContainerTestFacade().getDialog());
	}

	@Override
	public void logout() {
		Frontend.getInstance().login(null);
	}

	@Override
	public PageContainerTestFacade getCurrentPageContainerTestFacade() {
		return ((HeadlessFrontend) Frontend.getInstance()).getPageManager();
	}
	
	private class HeadlessLoginTestFacade implements UserPasswordLoginTestFacade {
		private final DialogTestFacade dialog;
		
		public HeadlessLoginTestFacade(DialogTestFacade dialog) {
			this.dialog = dialog;
			// Assertions.assertEquals(Resources.getString("Login.title"), dialog.getTitle());
		}

		@Override
		public boolean hasSkipLogin() {
			return dialog.getAction(Resources.getString("SkipLoginAction")) != null;
		}

		@Override
		public boolean hasClose() {
			return false;
		}

		@Override
		public void login() {
			dialog.getAction(Resources.getString("LoginAction")).run();
		}

		@Override
		public void cancel() {
			//
		}

		@Override
		public void close() {
			//
		}

		@Override
		public void setUser(String user) {
			dialog.getForm().getElement(Resources.getString("UserPassword.user")).setText(user);
			
		}

		@Override
		public void setPassword(String password) {
			dialog.getForm().getElement(Resources.getString("UserPassword.password")).setText(password);
		}
	}

}
