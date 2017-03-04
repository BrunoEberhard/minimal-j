package org.minimalj.frontend.page;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.minimalj.application.Configuration;
import org.minimalj.backend.Backend;
import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.TableActionListener;
import org.minimalj.repository.query.Query;
import org.minimalj.repository.query.Query.QueryLimitable;
import org.minimalj.repository.query.Query.QueryOrderable;
import org.minimalj.repository.query.SearchCriteria;
import org.minimalj.util.GenericUtils;
import org.minimalj.util.resources.Resources;

public abstract class SearchPage<T> extends TablePage<T> implements Parts, TableActionListener<T> {

	private final String query;

	private transient Long count;
	
	private Object[] sortKeys;
	private boolean[] sortDirections;
	private int offset;
	private int rows = 50;

	public SearchPage(String query, Object[] keys) {
		super(keys);
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

	@Override
	protected List<T> load() {
		List<T> list;
		if (query != null) {
			 list = load(query, sortKeys, sortDirections, offset, rows);
		} else {
			list = Collections.emptyList();
		}
		return list;
	}
	
	public long getCount() {
		if (count == null) {
			count = count(query);
		}
		return count;
	}

	@SuppressWarnings("unchecked")
	protected Class<T> getClazz() {
		return (Class<T>) GenericUtils.getGenericClass(getClass());
	}
	
	protected List<T> load(String queryString, Object[] sortKeys, boolean[] sortDirections, int offset, int rows) {
		Query query = createQuery(queryString);
		if (sortKeys != null) {
			for (int i = 0; i<sortKeys.length; i++) {
				query = ((QueryOrderable) query).order(sortKeys[i], sortDirections[i]);
			}
		}
		return Backend.find(getClazz(), ((QueryLimitable) query).limit(offset, rows));
	}

	protected long count(String query) {
		return Backend.count(getClazz(), createQuery(query));
	}
	
	protected Query createQuery(String query) {
		return new SearchCriteria(query);
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
	
	@Override
	public void sortingChanged(Object[] keys, boolean[] ascending) {
		this.sortKeys = keys;
		this.sortDirections = ascending;
		refresh();
	}
	
	public String getName() {
		Class<?> genericClass = GenericUtils.getGenericClass(getClass());
		return Resources.getString(genericClass);
	}
	
	@Override
	public int getCurrentPart() {
		return offset / rows;
	}
	
	@Override
	public void setCurrentPart(int number) {
		offset = number * rows;
		refresh();
	}
	
	@Override
	public int getPartCount() {
		return (int) ((getCount() - 1L) / (long) rows) + 1;
	}
	
	//
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Page handle(SearchPage... searchPages) {
		List<SearchPage> searchPagesWithResult = new ArrayList<>();
		for (SearchPage searchPage : searchPages) {
			if (searchPage.getCount() > 0) {
				searchPagesWithResult.add(searchPage);
			}
		}
		if (searchPagesWithResult.size() == 1) {
			SearchPage searchPage = searchPagesWithResult.get(0);
			if (searchPage.getCount() == 1) {
				Object singleSearchResult = searchPage.load().get(0);
				Page detailPage = searchPage.createDetailPage(singleSearchResult);
				return detailPage != null ? detailPage : searchPage;
			} else {
				return searchPage;
			}
		} else {
			return new CombinedSearchPage(searchPagesWithResult);
		}
	}
}