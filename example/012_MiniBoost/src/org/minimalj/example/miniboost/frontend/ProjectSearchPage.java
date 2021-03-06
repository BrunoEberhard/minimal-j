package org.minimalj.example.miniboost.frontend;

import static org.minimalj.example.miniboost.model.Project.$;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.example.miniboost.model.Project;
import org.minimalj.frontend.page.ObjectPage;
import org.minimalj.frontend.page.SearchPage;
import org.minimalj.repository.query.By;

public class ProjectSearchPage extends SearchPage<Project> {

	private static final Object[] keys = {$.matchcode, $.address.city, $.startDate};
	
	public ProjectSearchPage(String query) {
		super(query, keys);
	}

	@Override
	protected List<Project> load(String query) {
		return Backend.find(Project.class, By.search(query));
	}

	@Override
	public ObjectPage<Project> createDetailPage(Project owner) {
		return null; // no detail
	}

}
