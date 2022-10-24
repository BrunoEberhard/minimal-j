package org.minimalj.frontend.page;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.minimalj.frontend.Frontend;
import org.minimalj.frontend.Frontend.IContent;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.impl.json.JsonAction;
import org.minimalj.frontend.impl.json.JsonQueryContent;
import org.minimalj.util.resources.Resources;

public class QueryPage implements Page {

	private final List<Action> quickActions;
	
	public QueryPage() {
		this(Collections.emptyList());
	}

	public QueryPage(List<Action> quickActions) {
		this.quickActions = quickActions;
	}
	
	@Override
	public IContent getContent() {
		IContent content = Frontend.getInstance().createQueryContent();
		if (content instanceof JsonQueryContent && !quickActions.isEmpty()) {
			JsonQueryContent jsonQueryContent = (JsonQueryContent) content;

			List<JsonAction> actionLabels = new ArrayList<>();
			for (Action action : quickActions) {
				actionLabels.add(new JsonAction(action));
			}
			if (!actionLabels.isEmpty()) {
				jsonQueryContent.put("actions", actionLabels);
			}
		}
		return content;
	}
	
	@Override
	public String getTitle() {
		if (Resources.isAvailable(QueryPage.class.getSimpleName())) {
			return Resources.getString(QueryPage.class.getSimpleName(), Resources.OPTIONAL);
		} else {
			return Resources.getString("Application.name");
		}
	}

}
