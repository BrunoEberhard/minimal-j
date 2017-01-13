package org.minimalj.example.currencies.model;

import java.time.LocalDate;

import org.minimalj.model.annotation.Size;

/**
 * IsoActiveOrHistoricCurrencyInformationType as defined in  
 * http://www.currency-iso.org/dam/downloads/schema.xsd
 * 
 * Note: this entity is not intended to be used with a Minimal-J
 * repository. The model test would fail as there is no id field.
 */
public class IsoCurrencyInformation {
	@Size(140)
	public String ccyNm, ctryNm;

	@Size(3)
	public String ccy;

	public Boolean fund;
	
	@Size(3)
	public Integer ccyNbr;
	
	@Size(1)
	public Integer ccyMnrUnts;
	
	public LocalDate wthdrwlDt;
}
