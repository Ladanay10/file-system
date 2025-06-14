package com.example;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.ListView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class ZipUtils {
	public static void createZipArchive(File[] files, File outputZip) {
		try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outputZip))) {
			for (File file : files) {
				zipFile(file, file.getName(), zos);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void zipFile(File file, String fileName, ZipOutputStream zos) throws IOException {
		if (file.isDirectory()) {
			for (File childFile : file.listFiles()) {
				zipFile(childFile, fileName + "/" + childFile.getName(), zos);
			}
		} else {
			try (FileInputStream fis = new FileInputStream(file)) {
				ZipEntry zipEntry = new ZipEntry(fileName);
				zos.putNextEntry(zipEntry);
				byte[] buffer = new byte[1024];
				int length;
				while ((length = fis.read(buffer)) >= 0) {
					zos.write(buffer, 0, length);
				}
			}
		}
	}

	public static void extractZipArchive(File zipFile, File destinationDir) {
		try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				File newFile = new File(destinationDir, entry.getName());
				if (entry.isDirectory()) {
					newFile.mkdirs();
				} else {
					new File(newFile.getParent()).mkdirs();
					try (FileOutputStream fos = new FileOutputStream(newFile)) {
						byte[] buffer = new byte[1024];
						int length;
						while ((length = zis.read(buffer)) >= 0) {
							fos.write(buffer, 0, length);
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void showCustomArchiveDialog(List<File> filesToArchive, ListView<String> listView,
			Consumer<String> onUpdate) {
		Stage dialogStage = new Stage();
		dialogStage.setTitle("Create Archive");

		VBox dialogVBox = new VBox(15);
		dialogVBox.setPadding(new Insets(20));
		dialogVBox.setAlignment(Pos.CENTER);

		Label nameLabel = new Label("Enter archive name:");
		nameLabel.getStyleClass().add("dialog-label");

		TextField nameField = new TextField("archive.zip");
		nameField.getStyleClass().add("dialog-textfield");

		Button createButton = new Button("Create Archive");
		createButton.getStyleClass().add("dialog-button");

		createButton.setOnAction(e -> {
			String archiveName = nameField.getText().trim();
			if (!archiveName.endsWith(".zip")) {
				archiveName += ".zip";
			}

			File destination = new File(filesToArchive.get(0).getParent(), archiveName);
			ZipUtils.createZipArchive(filesToArchive.toArray(new File[0]), destination);

			onUpdate.accept(filesToArchive.get(0).getParent());
			dialogStage.close();
		});

		dialogVBox.getChildren().addAll(nameLabel, nameField, createButton);
		Scene scene = new Scene(dialogVBox, 350, 160);
		scene.getStylesheets().add(App.class.getResource("/css/styles.css").toExternalForm()); // 🔗 підключення стилів
		dialogStage.setScene(scene);
		dialogStage.showAndWait();
	};

	public static void showCustomExtractDialog(File zipFile, ListView<String> listView, Consumer<String> onUpdate) {
		Stage dialogStage = new Stage();
		dialogStage.setTitle("Extract Archive");

		VBox dialogVBox = new VBox(10);
		dialogVBox.setPadding(new Insets(20));
		dialogVBox.setAlignment(Pos.CENTER);

		Label label = new Label("Choose destination folder:");
		label.getStyleClass().add("dialog-label");

		TextField destinationField = new TextField(zipFile.getParent());
		destinationField.setPrefWidth(250);
		destinationField.getStyleClass().add("dialog-textfield");

		Button browseButton = new Button("Browse...");
		browseButton.getStyleClass().add("dialog-button");
		browseButton.setOnAction(e -> {
			DirectoryChooser chooser = new DirectoryChooser();
			chooser.setTitle("Select Destination Folder");
			File selected = chooser.showDialog(dialogStage);
			if (selected != null) {
				destinationField.setText(selected.getAbsolutePath());
			}
		});

		Button extractButton = new Button("Extract");
		extractButton.getStyleClass().add("dialog-button");
		extractButton.setOnAction(e -> {
			File destination = new File(destinationField.getText());
			if (destination.exists() && destination.isDirectory()) {
				ZipUtils.extractZipArchive(zipFile, destination);
				onUpdate.accept(destination.getAbsolutePath());
				dialogStage.close();
			}
		});

		HBox browseBox = new HBox(10, destinationField, browseButton);
		browseBox.setAlignment(Pos.CENTER);

		dialogVBox.getChildren().addAll(label, browseBox, extractButton);

		Scene scene = new Scene(dialogVBox, 400, 160);
		scene.getStylesheets().add(App.class.getResource("/css/styles.css").toExternalForm()); // підключення CSS
		dialogStage.setScene(scene);
		dialogStage.showAndWait();
	}

}
