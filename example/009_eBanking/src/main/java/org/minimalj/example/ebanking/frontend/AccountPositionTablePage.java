package org.minimalj.example.ebanking.frontend;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.example.ebanking.model.Account;
import org.minimalj.example.ebanking.model.AccountPosition;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.editor.Editor.SimpleEditor;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.page.TablePage;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Decimal;
import org.minimalj.model.annotation.Size;
import org.minimalj.transaction.criteria.By;
import org.minimalj.transaction.criteria.Criteria;
import org.minimalj.transaction.criteria.Criteria.Filter;
import org.minimalj.util.CloneHelper;

public class AccountPositionTablePage extends TablePage<AccountPosition> {

	private Account account;
	private final AccountPositionFilter filter;
	
	public AccountPositionTablePage(Account account) {
		this(account, new AccountPositionFilter());
	}
	
	public AccountPositionTablePage(Account account, AccountPositionFilter filter) {
		super(new Object[]{AccountPosition.$.valueDate, AccountPosition.$.description, AccountPosition.$.amount});
		this.account = account;
		this.filter = filter;
	}

	@Override
	protected List<AccountPosition> load() {
		return Backend.read(AccountPosition.class, By.field(AccountPosition.$.account, account).and(filter), 1000);
	}

	public void setAccount(Account account) {
		this.account = account;
		refresh();
	}
	
	@Override
	public List<Action> getActions() {
		return Collections.singletonList(new AccountPositionFilterEditor());
	}
	
	public class AccountPositionFilterEditor extends SimpleEditor<AccountPositionFilter> {

		@Override
		protected AccountPositionFilter createObject() {
			return CloneHelper.clone(AccountPositionTablePage.this.filter);
		}

		@Override
		protected Form<AccountPositionFilter> createForm() {
			Form<AccountPositionFilter> form = new Form<AccountPositionFilter>(2);
			form.line(AccountPositionFilter.$.description);
			form.line(AccountPositionFilter.$.minAmount, AccountPositionFilter.$.maxAmount);
			form.line(AccountPositionFilter.$.from, AccountPositionFilter.$.to);
			return form;
		}
		
		@Override
		protected void finished(AccountPositionFilter filter) {
			Frontend.show(new AccountPositionTablePage(account, filter));
		}
	}

	public static class AccountPositionFilter implements Filter {
		public static final AccountPositionFilter $ = Keys.of(AccountPositionFilter.class);
		
		@Size(255)
		public String description;
		
		@Decimal(2)
		public BigDecimal minAmount, maxAmount;
		
		public LocalDate from, to;

		/* first version (keep it for documentation)
		@Override
		public Criteria getCriteria() {
			Criteria p = new Criteria();
			if (!StringUtils.isEmpty(description)) {
				p = p.and(new SearchCriteria("*" + description + "*"));
			}
			if (minAmount != null) {
				p = p.and(new FieldCriteria(AccountPosition.$.amount, FieldOperator.greaterOrEqual, minAmount));
			}
			if (maxAmount != null) {
				p = p.and(new FieldCriteria(AccountPosition.$.amount, FieldOperator.lessOrEqual, maxAmount));
			}
			if (from != null) {
				p = p.and(new FieldCriteria(AccountPosition.$.valueDate, FieldOperator.greaterOrEqual, from));
			}
			if (to != null) {
				p = p.and(new FieldCriteria(AccountPosition.$.valueDate, FieldOperator.lessOrEqual, to));
			}
			return p;
		}
		*/
		
		/* second version (keep it for documentation)
		@Override
		public Criteria getCriteria() {
			Criteria p = new Criteria();
			if (!StringUtils.isEmpty(description)) {
				p = p.and(By.search("*" + description + "*"));
			}
			if (minAmount != null) {
				p = p.and(By.field(AccountPosition.$.amount, FieldOperator.greaterOrEqual, minAmount));
			}
			if (maxAmount != null) {
				p = p.and(By.field(AccountPosition.$.amount, FieldOperator.lessOrEqual, maxAmount));
			}
			if (from != null) {
				p = p.and(By.field(AccountPosition.$.valueDate, FieldOperator.greaterOrEqual, from));
			}
			if (to != null) {
				p = p.and(By.field(AccountPosition.$.valueDate, FieldOperator.lessOrEqual, to));
			}
			return p;
		}
		*/
		
		@Override
		public Criteria getCriteria() {
			Criteria p = new Criteria();
			p = p.and(By.search(description));
			p = p.and(By.range(AccountPosition.$.amount, minAmount, maxAmount));
			p = p.and(By.range(AccountPosition.$.valueDate, from, to));
			return p;
		}
	}
	
}
