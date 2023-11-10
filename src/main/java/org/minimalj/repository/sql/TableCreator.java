package org.minimalj.repository.sql;

import org.minimalj.application.Application;
import org.minimalj.application.Configuration;
import org.minimalj.model.test.ModelTest;

public class TableCreator {

	public static void main(String[] args) throws Exception {
		Application.initApplication(args);
		Application application = Application.getInstance();
		
		ModelTest.exitIfProblems();
		
		Configuration.set("schemaPreparation", SchemaPreparation.create.name());
		application.createRepository();
	}

}
