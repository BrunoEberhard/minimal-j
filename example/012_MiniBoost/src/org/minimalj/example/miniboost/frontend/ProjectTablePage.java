package org.minimalj.example.miniboost.frontend;

import static org.minimalj.example.miniboost.model.Project.$;

import java.util.List;

import org.minimalj.backend.Backend;
import org.minimalj.example.miniboost.frontend.ProjectTablePage.ProjectCostTablePage;
import org.minimalj.example.miniboost.model.Project;
import org.minimalj.example.miniboost.model.ProjectCost;
import org.minimalj.frontend.page.TablePage;
import org.minimalj.repository.criteria.By; 

public class ProjectTablePage extends TablePage.TablePageWithDetail<Project, ProjectCostTablePage> {

	private static final Object[] keys = {$.matchcode, $.name1, $.crewChief, $.address.country, $.address.city, $.startDate, $.endDate};
	
	public ProjectTablePage() {
		super(keys);
	}

	@Override
	protected List<Project> load() {
		return Backend.read(Project.class, By.all(), 100);
	}

	@Override
	protected ProjectCostTablePage createDetailPage(Project project) {
		return new ProjectCostTablePage(project);
	}
	
	@Override
	protected ProjectCostTablePage updateDetailPage(ProjectCostTablePage page, Project mainObject) {
		return page;
	}
	
	public class ProjectCostTablePage extends TablePage<ProjectCost> {
		private final Project project;
		
		public ProjectCostTablePage(Project project) {
			super(new Object[]{ProjectCost.$.employee.lastname, ProjectCost.$.text, ProjectCost.$.checkIn, ProjectCost.$.checkOut, ProjectCost.$.pause, ProjectCost.$.hours});
			this.project = project;
		}
		
		@Override
		protected List<ProjectCost> load() {
			return Backend.read(ProjectCost.class, By.field(ProjectCost.$.project, project), Integer.MAX_VALUE);
		}
	}

}
