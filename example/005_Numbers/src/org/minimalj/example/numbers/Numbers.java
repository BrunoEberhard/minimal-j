package org.minimalj.example.numbers;

import java.math.BigDecimal;

import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Decimal;
import org.minimalj.model.annotation.Signed;
import org.minimalj.model.annotation.Size;

public class Numbers {
	public static final Numbers $ = Keys.of(Numbers.class);
	
	public Integer anInteger;
	
	@Signed
	public Integer aSignedInteger;

	@Size(3)
	public Integer anIntegerOfSize3;
	
	@Size(3) @Signed
	public Integer aSignedIntegerOfSize3;

	
	public Long aLong;
	
	@Signed
	public Long aSignedLong;

	@Size(13)
	public Long aLongOfSize13;
	
	@Size(13) @Signed
	public Long aSignedLongOfSize13;


	public BigDecimal aBigDecimal;

	@Size(25)
	public BigDecimal aBigDecimalOfSize25;
	
	@Decimal(2)
	public BigDecimal aBigDecimalWith2Decimals;
	
	@Signed
	public BigDecimal aSignedBigDecimal;

	@Signed @Decimal(2)
	public BigDecimal aSignedBigDecimalWith2Decimals;

}