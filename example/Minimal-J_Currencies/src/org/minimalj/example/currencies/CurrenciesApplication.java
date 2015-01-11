package org.minimalj.example.currencies;

import org.minimalj.application.Application;
import org.minimalj.frontend.page.Page;

public class CurrenciesApplication extends Application {

	@Override
	public Page createDefaultPage() {
		return new CurrencyTablePage();
	}

}
