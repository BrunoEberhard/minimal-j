package org.minimalj.example.currencies.transaction;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.example.currencies.backend.IsoCurrencyInformationReader;
import org.minimalj.example.currencies.model.Currency;
import org.minimalj.transaction.Transaction;

public class ImportIsoCurrencyInformation implements Transaction<Integer> {
	private static final long serialVersionUID = 1L;

	@Override
	public Integer execute() {
		IsoCurrencyInformationReader reader = new IsoCurrencyInformationReader();
		List<Currency> currencies = reader.getCurrencies();
		int inserts = 0;
		for (Currency currency : currencies) {
			if (Backend.read(Currency.class, currency.id) == null) {
				Backend.insert(currency);
				inserts++;
			}
		}
		return inserts;
	}
	
}
