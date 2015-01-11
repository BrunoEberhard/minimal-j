package org.minimalj.example.currencies;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.minimalj.example.currencies.model.Currency;
import org.minimalj.frontend.page.TablePage;

public class CurrencyTablePage extends TablePage<Currency> {
	
	public CurrencyTablePage() {
		super(new Object[]{Currency.$.name, Currency.$.value, Currency.$.date}, null);
	}

	@Override
	protected List<Currency> load(String query) {
		Currency c = new Currency();
		c.name = "EUR";
		c.value = new BigDecimal("0.834");
		c.date = LocalDate.now();
		return Collections.singletonList(c);
	}

	@Override
	protected void clicked(Currency selectedObject, List<Currency> selectedObjects) {
	}		

}
