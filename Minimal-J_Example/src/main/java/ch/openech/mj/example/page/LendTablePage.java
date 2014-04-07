package ch.openech.mj.example.page;

import static ch.openech.mj.example.model.Lend.*;

import java.util.List;

import ch.openech.mj.criteria.Criteria;
import ch.openech.mj.example.model.Customer;
import ch.openech.mj.example.model.Lend;
import ch.openech.mj.page.ActionGroup;
import ch.openech.mj.page.PageContext;
import ch.openech.mj.page.RefreshablePage;
import ch.openech.mj.page.TablePage;
import ch.openech.mj.server.DbService;
import ch.openech.mj.server.Services;


public class LendTablePage extends TablePage<Lend> implements RefreshablePage {

	private final String text;
	
	public static final Object[] FIELDS = {
		LEND.book.title, //
		LEND.book.author, //
		LEND.till
	};
	
	public LendTablePage(PageContext context, String text) {
		super(context, FIELDS, text);
		this.text = text;
	}

	@Override
	public String getTitle() {
		return "Ausleihen für " + text;
	}

	@Override
	public ActionGroup getMenu() {
		return null;
	}

	@Override
	protected List<Lend> load(String query) {
		Customer customer = Services.get(DbService.class).read(Customer.class, Long.valueOf(query));
		return Services.get(DbService.class).read(Lend.class, Criteria.equals(Lend.LEND.customer, customer));
	}

	@Override
	protected void clicked(Lend selectedObject, List<Lend> selectedObjects) {
		// TODO
		// show(BookPage.class, selectedObject.book.);
	}
	
}
