package org.minimalj.frontend.page;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.impl.json.JsonDashboardContent;
import org.minimalj.frontend.impl.json.JsonFrontend;
import org.minimalj.frontend.impl.swing.SwingDashboardContent;
import org.minimalj.frontend.impl.swing.toolkit.SwingFrontend;

public class DashboardPage implements Page {

	private final List<Page> dashes = new ArrayList<>();

	public DashboardPage() {
	}

	public void addDash(Page page) {
		dashes.add(page);
	}

	@Override
	public IContent getContent() {
		if (Frontend.getInstance() instanceof JsonFrontend) {
			return new JsonDashboardContent(dashes);
		} else if (Frontend.getInstance() instanceof SwingFrontend) {
			return new SwingDashboardContent(dashes);
		} else {
			return null;
		}
	}
}
