package org.minimalj.example.ebanking.frontend;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.example.ebanking.model.Account;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.editor.Editor.SimpleEditor;
import org.minimalj.frontend.form.Form;
import org.minimalj.frontend.page.TablePage.TablePageWithDetail;
import org.minimalj.model.Keys;
import org.minimalj.model.annotation.Size;
import org.minimalj.repository.criteria.By;
import org.minimalj.repository.criteria.Criteria;
import org.minimalj.repository.criteria.SearchCriteria;
import org.minimalj.util.CloneHelper;
import org.minimalj.util.StringUtils;

public class AccountTablePage extends TablePageWithDetail<Account, AccountPositionTablePage> {

	private final AccountFilter accountFilter;

	public AccountTablePage() {
		this(new AccountFilter());
	}
	
	public AccountTablePage(AccountFilter accountFilter) {
		super(new Object[]{Account.$.accountNr, Account.$.description});
		this.accountFilter = accountFilter;
	}

	@Override
	public String getTitle() {
		if (accountFilter.active()) {
			return super.getTitle() + " [Filter]";
		} else {
			return super.getTitle();
		}
	}
	
	@Override
	protected List<Account> load() {
		List<Account> accounts = Backend.read(Account.class, By.filter(accountFilter), 100);
		return accounts;
	}

	@Override
	public List<Action> getActions() {
		List<Action> actions = new ArrayList<>();
		actions.add(new AccountFilterEditor());
		return actions;
	}

	public class AccountFilterEditor extends SimpleEditor<AccountFilter> {

		@Override
		protected AccountFilter createObject() {
			return CloneHelper.clone(AccountTablePage.this.accountFilter);
		}

		@Override
		protected Form<AccountFilter> createForm() {
			Form<AccountFilter> form = new Form<AccountFilter>();
			form.line(AccountFilter.$.description);
			return form;
		}
		
		@Override
		protected void finished(AccountFilter filter) {
			Frontend.show(new AccountTablePage(filter));
		}
	}

	public static class AccountFilter implements Filter {
		public static final AccountFilter $ = Keys.of(AccountFilter.class);
		
		@Size(255)
		public String description;

		public boolean active() {
			return !StringUtils.isEmpty(description);
		}
		
		@Override
		public Criteria getCriteria() {
			if (active()) {
				return new SearchCriteria(description);
			} else {
				return null;
			}
		}
	}

	@Override
	protected AccountPositionTablePage createDetailPage(Account account) {
		return new AccountPositionTablePage(account);
	}
	
	@Override
	protected AccountPositionTablePage updateDetailPage(AccountPositionTablePage page, Account account) {
		page.setAccount(account);
		return page;
	}
	
}
