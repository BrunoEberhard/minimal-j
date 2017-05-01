package org.minimalj.example.minimail;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.application.Application;
import org.minimalj.example.minimail.frontend.MailEditor;
import org.minimalj.example.minimail.frontend.MailTable;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.impl.swing.Swing;
import org.minimalj.frontend.page.Page;
import org.minimalj.frontend.page.PageAction;

public class MiniMail extends Application {

	@Override
	public List<Action> getNavigation() {
		List<Action> actions = new ArrayList<Action>();
		actions.add(new PageAction(new MailTable()));
		actions.add(new MailEditor());
		return actions;
	}
	
	@Override
	public Page createDefaultPage() {
		return new MailTable();
	}
	
	public static void main(String[] args) {
		Swing.start(new MiniMail());
	}

}
