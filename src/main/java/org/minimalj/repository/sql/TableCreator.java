package org.minimalj.repository.sql;

import org.minimalj.application.Application;
import org.minimalj.model.test.ModelTest;
import org.minimalj.repository.Repository;

public class TableCreator {

	public static void main(String[] args) throws Exception {
		Application.initApplication(args);
		Application application = Application.getInstance();
		
		ModelTest.exitIfProblems();
		
		SqlRepository repository = (SqlRepository) Repository.create(application);
		repository.createTables();
		repository.createCodes();
	}

}
