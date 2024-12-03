package org.minimalj.frontend.page;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.Frontend.SwitchContent;
import org.minimalj.frontend.impl.json.JsonDashboardContent;
import org.minimalj.frontend.impl.json.JsonFrontend;
import org.minimalj.frontend.impl.swing.SwingDashboardContent;
import org.minimalj.frontend.impl.swing.toolkit.SwingFrontend;

public class DashboardPage implements Page {

	private SwitchContent content;
	private final List<Page> dashes = new ArrayList<>();

	public DashboardPage() {
	}

	public void setDashes(List<Page> dashes) {
		this.dashes.clear();
		this.dashes.addAll(dashes);
		if (content != null) {
			content.show(createContent());
		}
	}
	
	@Override
	public IContent getContent() {
		if (content == null) {
			content = Frontend.getInstance().createSwitchContent();
			content.show(createContent());
		}
		return content;
	}
	
	private IContent createContent() {
		if (Frontend.getInstance() instanceof JsonFrontend) {
			return new JsonDashboardContent(dashes);
		} else if (Frontend.getInstance() instanceof SwingFrontend) {
			return new SwingDashboardContent(dashes);
		} else {
			return null;
		}
	}
}
