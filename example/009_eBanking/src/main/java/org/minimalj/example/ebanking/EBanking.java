package org.minimalj.example.ebanking;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.application.Application;
import org.minimalj.example.ebanking.frontend.AccountTablePage;
import org.minimalj.example.ebanking.frontend.DemoDataAction;
import org.minimalj.example.ebanking.model.Account;
import org.minimalj.example.ebanking.model.AccountPosition;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.action.ActionGroup;
import org.minimalj.frontend.page.PageAction;

// not yet complete...
public class EBanking extends Application {

	@Override
	public List<Action> getNavigation() {
		List<Action> actions = new ArrayList<>();

		ActionGroup customerActions = new ActionGroup("Konto");
		actions.add(customerActions);
		customerActions.add(new PageAction(new AccountTablePage()));

		ActionGroup articleActions = new ActionGroup("Überweisung");
		actions.add(articleActions);

		ActionGroup demoActions = new ActionGroup("Demo");
		actions.add(demoActions);
		demoActions.add(new DemoDataAction());

		return actions;
	}

	@Override
	public Class<?>[] getEntityClasses() {
		return new Class<?>[] { Account.class, AccountPosition.class };
	}
}
