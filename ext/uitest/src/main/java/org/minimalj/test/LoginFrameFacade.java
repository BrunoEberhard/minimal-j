package org.minimalj.test;

public interface LoginFrameFacade {

	public boolean hasSkipLogin();

	public boolean hasClose();

	public void login();

	public void cancel();

	public void close();
	
	public interface UserPasswordLoginTestFacade extends LoginFrameFacade {

		public void setUser(String name);

		public void setPassword(String password);

	}

}