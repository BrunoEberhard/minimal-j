package org.minimalj.frontend.impl.javafx;

import com.jfoenix.controls.JFXDrawer;
import com.jfoenix.controls.JFXDrawer.DrawerDirection;
import com.jfoenix.controls.JFXHamburger;
import com.jfoenix.controls.JFXRippler;
import com.jfoenix.controls.JFXRippler.RipplerMask;
import com.jfoenix.controls.JFXToolbar;
import com.jfoenix.transitions.hamburger.HamburgerBackArrowBasicTransition;

import javafx.animation.Transition;
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
		JFXToolbar toolbar = new JFXToolbar();

		JFXHamburger hamburger = new JFXHamburger();
		hamburger.setAnimation(new HamburgerBackArrowBasicTransition());

		JFXRippler rippler = new JFXRippler(hamburger, RipplerMask.CIRCLE);
		toolbar.getLeftItems().add(hamburger);

		toolbar.getLeftItems().add(new javafx.scene.control.Label("Hallo"));

		JFXDrawer drawer = new JFXDrawer();
		drawer.setDirection(DrawerDirection.LEFT);
		drawer.setDefaultDrawerSize(200);

		drawer.setSidePane(new Button("Sali"));

		// init the title hamburger icon
		drawer.setOnDrawerOpening(e -> {
			final Transition animation = hamburger.getAnimation();
			animation.setRate(1);
			animation.play();
		});
		drawer.setOnDrawerClosing(e -> {
			final Transition animation = hamburger.getAnimation();
			animation.setRate(-1);
			animation.play();
		});
		hamburger.setOnMouseClicked(e -> {
			if (drawer.isClosed() || drawer.isClosing()) {
				drawer.open();
			} else {
				drawer.close();
			}
		});

		VBox haupt = new VBox();
		haupt.getChildren().add(toolbar);
		haupt.getChildren().add(drawer);

		Scene scene = new Scene(haupt);
		stage.setScene(scene);
		stage.show();
	}

}
