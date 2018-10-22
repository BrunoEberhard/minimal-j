package org.minimalj.frontend.impl.javafx;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class JavaFX extends javafx.application.Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		VBox haupt = new VBox();
		haupt.getChildren().add(new Button("Sali"));
		Scene scene = new Scene(haupt);
		stage.setScene(scene);
		stage.show();
	}

}
