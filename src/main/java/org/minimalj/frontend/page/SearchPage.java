package org.minimalj.frontend.page;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.minimalj.frontend.Frontend.TableActionListener;
import org.minimalj.frontend.page.TablePage.TablePageWithDetail;
import org.minimalj.util.GenericUtils;
import org.minimalj.util.resources.Resources;

public abstract class SearchPage<T, DETAIL> extends TablePageWithDetail<T, DETAIL> implements TableActionListener<T> {

	private final String query;
	
	public SearchPage(String query, Object[] keys) {
		super(keys);
		String separator = System.getProperty("MjSearchQualifierSeparator", ":");
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
	
	@Override
	protected List<T> load() {
		if (query != null) {
			return load(query);
		} else {
			return Collections.emptyList();
		}
	}

	protected abstract List<T> load(String query);

	@Override
	public abstract ObjectPage<DETAIL> createDetailPage(DETAIL initialObject);
	
	@Override
	public String getTitle() {
		return getName() + " / " + query;
	}

	protected String getQualifier() {
		return getName().toLowerCase();
	}
	
	public String getName() {
		Class<?> genericClass = GenericUtils.getGenericClass(getClass());
		return Resources.getString(genericClass);
	}
	
	public static abstract class SimpleSearchPage<T> extends SearchPage<T, T> {

		public SimpleSearchPage(String query, Object[] keys) {
			super(query, keys);
		}
		
		@Override
		protected T load(T searchObject) {
			return searchObject;
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Page handle(SearchPage... searchPages) {
		List<SearchPage> searchPagesWithResult = new ArrayList<>();
		for (SearchPage searchPage : searchPages) {
			if (searchPage.getResultCount() > 0) {
				searchPagesWithResult.add(searchPage);
			}
		}
		if (searchPagesWithResult.size() == 1) {
			SearchPage searchPage = searchPagesWithResult.get(0);
			if (searchPage.getResultCount() == 1) {
				Object singleSearchResult = searchPage.load().get(0);
				Object singleDetail = searchPage.load(singleSearchResult);
				Page detailPage = searchPage.createDetailPage(singleDetail);
				return detailPage;
			} else {
				return searchPage;
			}
		} else {
			return new CombinedSearchPage(searchPagesWithResult);
		}
	}
	
}
