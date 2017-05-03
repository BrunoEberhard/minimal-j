package org.minimalj.frontend.page;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.page.CombinedSearchPage.SearchResult;
import org.minimalj.model.Keys;

public class CombinedSearchPage extends TablePage<SearchResult> {

	private final List<SearchResult> searchResults;
	
	@SuppressWarnings("rawtypes")
	public CombinedSearchPage(List<SearchPage> searchPages) {
		super(new Object[]{SearchResult.$.pageName, SearchResult.$.resultCount});
		searchResults = new ArrayList<CombinedSearchPage.SearchResult>(searchPages.size());
		for (SearchPage searchPage : searchPages) {
			searchResults.add(new SearchResult(searchPage));
		}
	}

	@Override
	protected List<SearchResult> load() {
		return searchResults;
	}

	@Override
	public void action(SearchResult selectedObject) {
		Frontend.show(selectedObject.searchPage);
	}
	
	@SuppressWarnings("rawtypes")
	public static class SearchResult {
		public static final SearchResult $ = Keys.of(SearchResult.class);
		
		public SearchResult() {
			// needed for Keys
		}
		
		public SearchResult(SearchPage searchPage) {
			this.searchPage = searchPage;
			pageName = searchPage.getName();
			resultCount = searchPage.getCount();
		}
		
		private SearchPage searchPage;
		public String pageName;
		public Long resultCount;
	}
}
