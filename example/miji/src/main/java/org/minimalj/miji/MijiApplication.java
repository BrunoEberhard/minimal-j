package org.minimalj.miji;

import java.util.List;

import org.minimalj.application.Application;
import org.minimalj.backend.Backend;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.action.ActionGroup;
import org.minimalj.frontend.page.Page;
import org.minimalj.miji.backend.MijiRestBackend;
import org.minimalj.miji.frontend.IssueTablePage;

public class MijiApplication extends Application {

	@Override
	public Page createDefaultPage() {
		return new IssueTablePage();
	}
	
	@Override
	public List<Action> getNavigation() {
		ActionGroup actionGroup = new ActionGroup(null);
		
		ActionGroup items = actionGroup.addGroup("Items");
		items.add(new IssueTablePage());
		
		return actionGroup.getItems();
	}
	
	public boolean isLoginRequired() {
		return true;
	}
	
	public static void main(String[] args) {
		Backend.setInstance(new MijiRestBackend());
		// Configuration.set("MjAuthentication", MijiAuthentication.class.getName());
		// Configuration.set("MjBackend", MijiRestBackend.class.getName());
		// Swing.start(new MijiApplication());
		Application.main(args);
	}
	
}
