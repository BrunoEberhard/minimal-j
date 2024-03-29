package org.minimalj.example.currencies.frontend;

import org.minimalj.backend.Backend;
import org.minimalj.example.currencies.transaction.ImportIsoCurrencyInformation;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.action.Action;

public class ImportIsoCurrencyInformationAction extends Action {

	@Override
	public void run() {
		int inserts = Backend.execute(new ImportIsoCurrencyInformation());
		Frontend.showMessage(inserts + " currencies imported");
	}
	
}
