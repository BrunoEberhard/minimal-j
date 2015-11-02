package org.minimalj.example.currencies;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.application.Application;
import org.minimalj.example.currencies.frontend.CurrencyTablePage;
import org.minimalj.example.currencies.frontend.ImportIsoCurrencyInformationAction;
import org.minimalj.example.currencies.model.Currency;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.page.PageAction;

public class CurrenciesApplication extends Application {

	@Override
	public Class<?>[] getEntityClasses() {
		return new Class<?>[] { Currency.class };
	}
	
	@Override
	public List<Action> getNavigation() {
		List<Action> actions = new ArrayList<>();
		
		actions.add(new ImportIsoCurrencyInformationAction());
		actions.add(new PageAction(new CurrencyTablePage()));
		
		return actions;
	}
}
