package org.minimalj.example.timesheet;

import java.util.ArrayList;
import java.util.List;

import org.minimalj.application.Application;
import org.minimalj.example.timesheet.frontend.NewProjectEditor;
import org.minimalj.example.timesheet.frontend.NewWorkEditor;
import org.minimalj.example.timesheet.model.Employee;
import org.minimalj.example.timesheet.model.Project;
import org.minimalj.example.timesheet.model.Work;
import org.minimalj.frontend.action.Action;
import org.minimalj.frontend.action.ActionGroup;
import org.minimalj.frontend.impl.swing.Swing;
import org.minimalj.util.resources.Resources;

public class TimeSheetApplication extends Application {

	@Override
	public Class<?>[] getEntityClasses() {
		return new Class<?>[] { Project.class, Employee.class, Work.class };
	}
	
	@Override
	public List<Action> getNavigation() {
		List<Action> actions = new ArrayList<>();
		ActionGroup employeeActionGroup = new ActionGroup(Resources.getString("Employee"));
		employeeActionGroup.add(new NewWorkEditor());
		employeeActionGroup.add(new NewProjectEditor());
		actions.add(employeeActionGroup);
		
		return actions;
	}
	
	public static void main(String[] args) {
		Swing.start(new TimeSheetApplication());
	}
}
