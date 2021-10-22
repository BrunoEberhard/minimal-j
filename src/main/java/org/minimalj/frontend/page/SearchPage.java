package org.minimalj.frontend.page;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

import org.minimalj.application.Configuration;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.Frontend.ITable;
import org.minimalj.frontend.Frontend.TableActionListener;
import org.minimalj.util.GenericUtils;
import org.minimalj.util.resources.Resources;

public abstract class SearchPage<T> extends Page implements TableActionListener<T> {

	private final String query;
	private final Object[] keys;

	private SoftReference<List<T>> list;
	
	public SearchPage(String query, Object[] keys) {
		this.keys = keys;
		
		String separator = Configuration.get("MjSearchQualifierSeparator", ":");
		int pos = query.indexOf(separator);
		if (pos > 0 && pos < query.length()-1) {
			String searchQualifier = query.substring(0, pos).toLowerCase();
			if (getQualifier().startsWith(searchQualifier)) {
				this.query = query.substring(pos+1);
			} else {
				this.query = null;
			}
		} else {
			this.query = query;
		}
	}

	protected abstract List<T> load(String query);
	
	@Override
	public IContent getContent() {
		ITable<T> table = Frontend.getInstance().createTable(keys, false, this);
		table.setObjects(getList());
		return table;
	}
	
	public long getCount() {
		return getList().size();
	}

	private List<T> getList() {
		List<T> list = this.list != null ? this.list.get() : null;
		if (list == null) {
			list = load(query);
		}
		this.list = new SoftReference<>(list);
		return list;
	}

	@SuppressWarnings("unchecked")
	protected Class<T> getClazz() {
		return (Class<T>) GenericUtils.getGenericClass(getClass());
	}
	
	protected abstract Page createDetailPage(T mainObject);
	
	@Override
	public String getTitle() {
		return getName() + " / " + query;
	}

	protected String getQualifier() {
		return getName().toLowerCase();
	}
	
	@Override
	public void action(T selectedObject) {
		Page detailPage = createDetailPage(selectedObject);
		if (detailPage != null) {
			Frontend.show(detailPage);
		}
	}
		
	public String getName() {
		Class<?> genericClass = GenericUtils.getGenericClass(getClass());
		return Resources.getString(genericClass);
	}
	
	//
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void handle(SearchPage... searchPages) {
		List<SearchPage> searchPagesWithResult = new ArrayList<>();
		for (SearchPage searchPage : searchPages) {
			if (searchPage.getCount() > 0) {
				searchPagesWithResult.add(searchPage);
			}
		}
		if (searchPagesWithResult.size() == 1) {
			SearchPage searchPage = searchPagesWithResult.get(0);
			if (searchPage.getCount() == 1) {
				Object singleSearchResult = searchPage.getList().get(0);
				Page detailPage = searchPage.createDetailPage(singleSearchResult);
				Frontend.show(detailPage != null ? detailPage : searchPage);
			} else {
				Frontend.show(searchPage);
			}
		} else {
			Frontend.show(new CombinedSearchPage(searchPagesWithResult));
		}
	}
}