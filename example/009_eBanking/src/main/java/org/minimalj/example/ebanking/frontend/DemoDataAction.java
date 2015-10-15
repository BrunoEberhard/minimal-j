package org.minimalj.example.ebanking.frontend;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.minimalj.backend.Backend;
import org.minimalj.example.ebanking.model.Account;
import org.minimalj.example.ebanking.model.AccountPosition;
import org.minimalj.frontend.action.Action;
import org.minimalj.util.mock.MockPrename;

public class DemoDataAction extends Action {

	@Override
	public void action() {
		for (int i = 0; i<5; i++) {
			Account account = account();
			Backend.insert(account);
			for (int j = 0; j<100; j++) {
				AccountPosition position = accountPosition(account);
				Backend.insert(position);
			}
		}
	}

	private Account account() {
		Account account = new Account();
		account.accountNr = "" + (Math.random() * 9000000 + 1000000);
		account.description = MockPrename.getFirstName(Math.random() < 0.5);
		return account;
	}

	private AccountPosition accountPosition(Account account) {
		AccountPosition accountPosition = new AccountPosition();
		accountPosition.account = account;
		accountPosition.amount = new BigDecimal(10 * Math.random()).movePointRight((int)(Math.random() * 5));
		if (Math.random() < 0.4) {
			accountPosition.amount = accountPosition.amount.negate();
		}
		accountPosition.description = MockPrename.getFirstName(Math.random() < 0.5);
		accountPosition.valueDate = LocalDate.now().minusDays((int)(Math.random() * 1000));
		return accountPosition;
	}
	
}
