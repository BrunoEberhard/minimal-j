package org.minimalj.example.currencies.frontend;

import org.minimalj.backend.Backend;
import org.minimalj.example.currencies.transaction.ImportIsoCurrencyInformation;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.action.Action;

public class ImportIsoCurrencyInformationAction extends Action {

	@Override
	public void action() {
		int inserts = Backend.getInstance().execute(new ImportIsoCurrencyInformation());
		Frontend.getBrowser().showMessage(inserts + " currencies imported");
	}
	
}
