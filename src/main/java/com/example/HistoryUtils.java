package com.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class HistoryUtils {

	public static final String HISTORY_FILE = "history.txt";

	public static void showHistoryWindow(ListView<String> listView) {
		Stage historyStage = new Stage();
		historyStage.setTitle("File History");

		VBox historyRoot = new VBox();
		ListView<String> historyListView = new ListView<>();
		Button undoButton = new Button("Undo Selected");
		Button clearHistoryButton = new Button("Clear History"); // 🔹 Нова кнопка

		try (BufferedReader reader = new BufferedReader(new FileReader(HISTORY_FILE))) {
			String line;
			while ((line = reader.readLine()) != null) {
				historyListView.getItems().add(line);
			}
		} catch (IOException e) {
			historyListView.getItems().add("No history found.");
		}

		undoButton.setOnAction(e -> {
			String selectedItem = historyListView.getSelectionModel().getSelectedItem();
			if (selectedItem != null) {
				undoAction(selectedItem, listView, historyListView); // 🔹 Передаємо обидва списки
			}
		});
		clearHistoryButton.setOnAction(e -> {
			clearHistory();
			historyListView.getItems().clear();
		});

		historyRoot.getChildren().addAll(historyListView, undoButton, clearHistoryButton);
		Scene historyScene = new Scene(historyRoot, 500, 400);
		historyStage.setScene(historyScene);
		historyStage.show();
	}

	public static void undoAction(String historyEntry, ListView<String> listView, ListView<String> historyListView) {
		if (historyEntry.contains("DELETE")) {
			String filePath = historyEntry.split(": ")[1];
			File backupFile = new File(filePath + ".bak");
			File restoredFile = new File(filePath);

			if (backupFile.exists()) {
				try {
					Files.move(backupFile.toPath(), restoredFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
					System.out.println("✅ File successfuly updated: " + restoredFile.getAbsolutePath());

					removeHistoryEntry(historyEntry);

					historyListView.getItems().remove(historyEntry);

					listView.refresh();

				} catch (IOException e) {
					System.out.println("❌ Error when you try update file: " + restoredFile.getAbsolutePath());
					e.printStackTrace();
				}
			} else {
				System.out.println("❌ Reserved file doesn't exist: " + backupFile.getAbsolutePath());
			}

		} else if (historyEntry.contains("RENAME")) {
			String[] parts = historyEntry.split(": ")[1].split(" -> ");
			File renamedFile = new File(parts[1]);
			File originalFile = new File(parts[0]);

			if (renamedFile.exists()) {
				boolean renamedBack = renamedFile.renameTo(originalFile);
				if (renamedBack) {
					System.out.println("✅ File successfuly rename: " + originalFile.getAbsolutePath());

					removeHistoryEntry(historyEntry);

					historyListView.getItems().remove(historyEntry);

					listView.refresh();

				} else {
					System.out.println("❌ Error when try to rename file: " + renamedFile.getAbsolutePath());
				}
			}
		}
	}

	public static void removeHistoryEntry(String entryToRemove) {
		File historyFile = new File(HISTORY_FILE);
		List<String> updatedHistory = new ArrayList<>();

		try (BufferedReader reader = new BufferedReader(new FileReader(historyFile))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (!line.equals(entryToRemove)) {
					updatedHistory.add(line);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(historyFile))) {
			for (String line : updatedHistory) {
				writer.write(line);
				writer.newLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void logHistory(String action, String filePath) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(HISTORY_FILE, true))) {
			String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
			writer.write(timestamp + " - " + action + ": " + filePath);
			writer.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void clearHistory() {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(HISTORY_FILE))) {
			writer.write("");
			System.out.println("History was deleted.");
		} catch (IOException e) {
			System.out.println("Error trying deleting history");
			e.printStackTrace();
		}
	}

}
