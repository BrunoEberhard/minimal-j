package org.minimalj.test;

import java.util.List;

public interface FrameTestFacade {

	public interface LoginFrameFacade {

		public boolean hasSkipLogin();

		public boolean hasClose();

		public void login();

		public void cancel();

		public void close();
	}

	public interface UserPasswordLoginTestFacade extends LoginFrameFacade {

		public void setUser(String name);

		public void setPassword(String password);

	}

	public interface PageContainerTestFacade extends FrameTestFacade {

		public NavigationTestFacade getNavigation();

		public List<PageTestFacade> getPages();

		public ActionTestFacade getBack();

		public ActionTestFacade getForward();

	}

	public interface ActionTestFacade extends Runnable {

		public boolean isEnabled();
	}

	public interface NavigationTestFacade {

		public Runnable get(String resourceName);

	}

	public static abstract class PageTestFacade {
		public final String title;
		
		public PageTestFacade(String title) {
			this.title = title;
		}
		
		public String getTitle() {
			return title;
		}

	}

	public static class TextPageTestFacade extends PageTestFacade {
		public final String text;
		
		public TextPageTestFacade(String title, String text) {
			super(title);
			this.text = text;
		}
		
		public boolean contains(String string) {
			return text.contains(string);
		}
	}

	
	public static abstract class FormPageTestFacade extends PageTestFacade {

		public FormPageTestFacade(String title) {
			super(title);
		}

	}

}
