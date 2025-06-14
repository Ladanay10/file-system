package com.example;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class InfoWindow {
	public void display() {
		Stage infoStage = new Stage();
		VBox infoLayout = new VBox();
		infoLayout.setPadding(new Insets(20));
		infoLayout.setSpacing(10);
		infoLayout.setAlignment(Pos.CENTER);

		Label infoLabel = new Label("Created by Ladanay Andriy KI-402.\nVersion of application - v1.0");
		Button closeButton = new Button("Close");

		closeButton.getStyleClass().add("button-close");

		closeButton.setOnAction(event -> infoStage.close());
		infoLayout.getChildren().addAll(infoLabel, closeButton);

		Scene infoScene = new Scene(infoLayout, 300, 150);
		infoStage.setScene(infoScene);
		infoStage.setTitle("About");
		infoStage.show();
	}
}
