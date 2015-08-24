package org.minimalj.example.currencies.frontend;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.example.currencies.model.Currency;
import org.minimalj.frontend.page.TablePage;
import org.minimalj.transaction.criteria.Criteria;

public class CurrencyTablePage extends TablePage<Currency> {
	
	public CurrencyTablePage() {
		super(new Object[]{Currency.$.number, Currency.$.id, Currency.$.name, Currency.$.fund, Currency.$.minorUnits});
	}

	@Override
	protected List<Currency> load() {
		return Backend.persistence().read(Currency.class, Criteria.all(), 1000);
	}

}
