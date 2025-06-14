package com.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import java.util.Date;

import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;

public class App extends Application {

	private static Scene scene;
	private static String selectedExtension = "All";
	private static String selectedSortOption = "Name (A-Z)";
	private VBox root;
	private static final String HISTORY_FILE = "history.txt";
	private static Stack<String> undoStack = new Stack<>();
	private HBox bookmarkBox;
	private static final String BOOKMARKS_FILE = "bookmarks.txt";
	private TextField pathField;

	private ListView<String> listView;
	private final Stack<String> directoryHistory = new Stack<>();
	private HBox searchContainer;
	private VBox fileInfoBox;

	public void start(Stage primaryStage) {
		root = new VBox();
		Label label = new Label("Enter the path to the directory:");
		label.getStyleClass().add("label");
		root.getChildren().add(label);
		Button infoButton = new Button("About");
		Button backButton = new Button("⬅ Back");
		Button addBookmarkButton = new Button("★ Add to Bookmarks");

		Button historyButton = new Button("View History");
		Button pieChartButton = new Button("Show Disk Usage");

		HBox btnContainer = new HBox(infoButton, historyButton, pieChartButton);
		infoButton.getStyleClass().add("button-info");
		pieChartButton.getStyleClass().add("button-chart");
		historyButton.getStyleClass().add("button-history");
		infoButton.setOnAction(event -> {
			new InfoWindow().display();
		});
		HBox labelBox = new HBox(label, btnContainer);
		btnContainer.setSpacing(30);
		labelBox.setSpacing(280);
		root.getChildren().add(labelBox);

		pathField = new TextField();
		Button browseButton = new Button("Browse");
		pathField.getStyleClass().add("search-field");

		HBox browseContainer = new HBox(pathField, browseButton, backButton, addBookmarkButton);
		browseContainer.getStyleClass().add("browse-container");
		browseButton.getStyleClass().add("button-browse");
		root.setPadding(new Insets(15));
		root.setSpacing(15);

		HBox filterBox = new HBox();
		Label filterLabel = new Label("Filter by extension:");
		ComboBox<String> extensionFilter = new ComboBox<>();
		extensionFilter.getItems().addAll("All", ".txt", ".jpg", ".png", ".pdf", ".docx");
		extensionFilter.setValue("All");
		filterBox.getChildren().addAll(filterLabel, extensionFilter);
		filterBox.setSpacing(10);

		HBox sortBox = new HBox();
		Label sortLabel = new Label("Sort by:");
		ComboBox<String> sortOptions = new ComboBox<>();
		sortOptions.getItems().addAll("Name (A-Z)", "Name (Z-A)", "Size (Asc)", "Size (Desc)", "Date (New-Old)",
				"Date (Old-New)");
		sortOptions.setValue("Name (A-Z)");
		sortBox.getChildren().addAll(sortLabel, sortOptions);
		sortBox.setSpacing(10);

		filterBox.getStyleClass().add("filter-box");
		sortBox.getStyleClass().add("sort-box");

		filterLabel.getStyleClass().add("filter-label");
		sortLabel.getStyleClass().add("sort-label");

		extensionFilter.getStyleClass().add("extension-filter");
		sortOptions.getStyleClass().add("sort-options");

		TextField searchField = new TextField();

		searchField.setPromptText("Enter file name...");
		searchField.getStyleClass().add("search-field");

		searchField.setMinWidth(300);
		searchField.setMaxWidth(300);

		Button searchButton = new Button("Search");
		searchButton.getStyleClass().add("button-search");

		searchContainer = new HBox(10);
		searchContainer.getChildren().addAll(searchField, searchButton, sortBox, filterBox);
		searchContainer.setSpacing(10);
		searchContainer.setAlignment(Pos.CENTER_LEFT);
		searchContainer.getStyleClass().add("search-container");

		HBox buttonsFile = new HBox();
		Button deleteButton = new Button("Delete");
		deleteButton.getStyleClass().add("button-delete");
		Button copyButton = new Button("Copy");
		copyButton.getStyleClass().add("button-copy");
		Button moveButton = new Button("Move");
		moveButton.getStyleClass().add("button-move");

		buttonsFile.getChildren().addAll(deleteButton, copyButton, moveButton);

		buttonsFile.setSpacing(10);
		listView = new ListView<>();
		listView.setMinHeight(300);
		listView.setMaxHeight(Double.MAX_VALUE);
		listView.getStyleClass().add("list-view");
		listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		HBox fileControls = new HBox();
		TextField fileNameField = new TextField();
		Button createButton = new Button("Create File");
		Button renameButton = new Button("Rename File");
		Button viewContentButton = new Button("View Content");
		createButton.getStyleClass().add("button-create");
		renameButton.getStyleClass().add("button-rename");

		viewContentButton.getStyleClass().add("button-view");
		fileControls.getChildren().addAll(fileNameField, createButton, renameButton, viewContentButton);
		fileControls.setSpacing(10);

		fileInfoBox = new VBox();
		fileInfoBox.getStyleClass().add("file-info-box");
		root.getChildren().add(fileInfoBox);
		copyButton.setVisible(false);
		moveButton.setVisible(false);

		ContextMenu contextMenu = new ContextMenu();
		MenuItem encryptItem = new MenuItem("Encrypt");
		MenuItem decryptItem = new MenuItem("Decrypt");

		contextMenu.getStyleClass().add("custom-context-menu");
		encryptItem.getStyleClass().add("context-menu-item");
		decryptItem.getStyleClass().add("context-menu-item");

		MenuItem archiveItem = new MenuItem("Archive");
		MenuItem extractItem = new MenuItem("Extract");

		archiveItem.getStyleClass().add("context-menu-item");
		extractItem.getStyleClass().add("context-menu-item");

		contextMenu.getItems().addAll(archiveItem, extractItem);

		bookmarkBox = new HBox();
		bookmarkBox.setSpacing(10);
		bookmarkBox.setPadding(new Insets(10));
		bookmarkBox.getStyleClass().add("bookmark-box");

		Label bookmarksLabel = new Label("📁 Bookmarks:");
		bookmarksLabel.getStyleClass().add("bookmark-title");

		addBookmarkButton.getStyleClass().add("button-bookmark");

		bookmarkBox.getChildren().addAll(bookmarksLabel);
		updateBookmarkBox();

		addBookmarkButton.setOnAction(e -> {
			String path = pathField.getText();
			if (!path.isEmpty()) {
				BookmarkUtils.saveBookmark(path, BOOKMARKS_FILE);
				updateBookmarkBox();
			}
		});
		encryptItem.setOnAction(event -> {
			String selectedItem = listView.getSelectionModel().getSelectedItem();
			if (selectedItem != null) {
				File selectedFile = new File(pathField.getText(), selectedItem);
				if (selectedFile.exists() && selectedFile.isFile()) {
					TextInputDialog dialog = new TextInputDialog();
					dialog.setTitle("Set Password");
					dialog.setHeaderText("Enter a password to encrypt the file:");
					dialog.setContentText("Password:");
					dialog.showAndWait().ifPresent(password -> {
						try {
							SecurityUtils.encryptFile(selectedFile, password);
							updateListView(listView, selectedFile.getParent());
							SecurityUtils.showAlert("Encryption Successful",
									"The file has been encrypted successfully.");
						} catch (Exception ex) {
							SecurityUtils.showAlert("Encryption Error", "An error occurred during encryption.");
							ex.printStackTrace();
						}
					});
				}
			}
		});

		decryptItem.setOnAction(event -> {
			String selectedItem = listView.getSelectionModel().getSelectedItem();
			if (selectedItem != null) {
				File selectedFile = new File(pathField.getText(), selectedItem);
				if (selectedFile.exists() && selectedFile.isFile()) {
					TextInputDialog dialog = new TextInputDialog();
					dialog.setTitle("Enter Password");
					dialog.setHeaderText("Enter the password to decrypt the file:");
					dialog.setContentText("Password:");

					dialog.showAndWait().ifPresent(password -> {
						try {
							SecurityUtils.decryptFile(selectedFile, password);

							Platform.runLater(() -> {
								updateListView(listView, selectedFile.getParent());
								SecurityUtils.showAlert("Decryption Successful",
										"The file has been decrypted successfully.");
							});

						} catch (Exception ex) {
							Platform.runLater(() -> SecurityUtils.showAlert("Decryption Error", ex.getMessage()));
							ex.printStackTrace();
						}
					});
				}
			}
		});

		contextMenu.getItems().addAll(encryptItem, decryptItem);

		listView.setOnMousePressed(event -> {
			if (event.getButton() == MouseButton.SECONDARY) {
				String selectedItem = listView.getSelectionModel().getSelectedItem();
				if (selectedItem != null) {
					listView.setContextMenu(contextMenu);
				}
			} else {
				listView.setContextMenu(null);
			}
		});

		listView.setCellFactory(param -> new ListCell<String>() {
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setText(null);
					setGraphic(null);
				} else {
					File file = new File(pathField.getText(), item);
					String iconPath;

					if (file.isDirectory()) {
						iconPath = "/icons/folder.png";
					} else if (item.matches("(?i).*\\.(doc|docx)")) {
						iconPath = "/icons/word.png";
					} else if (item.matches("(?i).*\\.(xls|xlsx)")) {
						iconPath = "/icons/excel.png";
					} else if (item.matches("(?i).*\\.(ppt|pptx)")) {
						iconPath = "/icons/powerpoint.png";
					} else if (item.matches("(?i).*\\.(pdf)")) {
						iconPath = "/icons/pdf.png";
					} else if (item.matches("(?i).*\\.(zip|rar|7z|tar|gz)")) {
						iconPath = "/icons/archive.png";
					} else if (item.matches("(?i).*\\.(mp4|avi|mov|mkv)")) {
						iconPath = "/icons/video.png";
					} else if (item.matches("(?i).*\\.(mp3|wav|ogg)")) {
						iconPath = "/icons/audio.png";
					} else if (item.matches("(?i).*\\.(jpg|jpeg|png|gif|bmp|svg)")) {
						iconPath = "/icons/image.png";
					} else if (item.matches("(?i).*\\.(txt|md|log|csv)")) {
						iconPath = "/icons/text.png";
					} else if (item.matches("(?i).*\\.(exe)")) {
						iconPath = "/icons/exe.png";
					} else if (item.matches("(?i).*\\.(iso)")) {
						iconPath = "/icons/iso.png";
					} else {
						iconPath = "/icons/file.png";
					}

					ImageView iconView = new ImageView(getClass().getResource(iconPath).toExternalForm());
					iconView.setFitWidth(18);
					iconView.setFitHeight(18);
					setText(item);
					setGraphic(iconView);
				}
			}
		});

		browseButton.setOnAction(e -> {
			DirectoryChooser directoryChooser = new DirectoryChooser();
			File selectedDirectory = directoryChooser.showDialog(primaryStage);
			if (selectedDirectory != null) {
				pathField.setText(selectedDirectory.getAbsolutePath());
				updateListView(listView, selectedDirectory.getAbsolutePath());
				fileInfoBox.getChildren().clear();
			}
		});

		deleteButton.setOnAction(e -> {
			List<String> selectedItems = new ArrayList<>(listView.getSelectionModel().getSelectedItems());
			String selectedPath = pathField.getText();

			if (!selectedItems.isEmpty() && !selectedPath.isEmpty()) {
				for (String selectedItem : selectedItems) {
					File selectedFile = new File(selectedPath, selectedItem);
					if (selectedFile.exists()) {
						try {
							File backupFile = new File(selectedFile.getAbsolutePath() + ".bak");
							Files.copy(selectedFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

							logHistory("DELETE", selectedFile.getAbsolutePath());

							undoStack.push("DELETE|" + selectedFile.getAbsolutePath());

							boolean deleted = FileUtils.deleteFileOrDirectory(selectedFile);

							if (deleted) {
								System.out.println("File deleted successfuly: " + selectedFile.getAbsolutePath());
							} else {
								System.out.println("We cannot delete this file: " + selectedFile.getAbsolutePath());
							}

						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}
				updateListView(listView, selectedPath);
				fileInfoBox.getChildren().clear();
			}
		});

		createButton.setOnAction(e -> {
			String fileName = fileNameField.getText();
			String directoryPath = pathField.getText();
			if (!fileName.isEmpty() && !directoryPath.isEmpty()) {
				File newFile = new File(directoryPath, fileName);
				try {
					boolean created = newFile.createNewFile();
					if (created) {
						updateListView(listView, directoryPath);
					}
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		});

		renameButton.setOnAction(e -> {
			String selectedItem = listView.getSelectionModel().getSelectedItem();
			String selectedPath = pathField.getText();
			String newFileName = fileNameField.getText();
			if (selectedItem != null && !selectedPath.isEmpty() && !newFileName.isEmpty()) {
				File selectedFile = new File(selectedPath, selectedItem);
				File newFile = new File(selectedPath, newFileName);
				try {
					if (selectedFile.exists()) {
						logHistory("RENAME", selectedFile.getAbsolutePath() + " -> " + newFile.getAbsolutePath());

						undoStack.push("RENAME|" + selectedFile.getAbsolutePath() + "|" + newFile.getAbsolutePath());

						boolean renamed = selectedFile.renameTo(newFile);
						if (renamed) {
							updateListView(listView, selectedPath);
						}
					}
				} catch (Error ex) {
					ex.printStackTrace();
				}
			}
		});

		copyButton.setOnAction(e -> {
			List<String> selectedItems = listView.getSelectionModel().getSelectedItems();
			String selectedPath = pathField.getText();

			DirectoryChooser directoryChooser = new DirectoryChooser();
			directoryChooser.setTitle("Select Destination Directory");
			File selectedDirectory = directoryChooser.showDialog(primaryStage);

			if (!selectedItems.isEmpty() && selectedDirectory != null) {
				String destinationPath = selectedDirectory.getAbsolutePath();

				for (String selectedItem : selectedItems) {
					File selectedFile = new File(selectedPath, selectedItem);
					File destinationFile = new File(destinationPath, selectedItem);
					if (selectedFile.exists()) {
						try {
							logHistory("COPY",
									selectedFile.getAbsolutePath() + " -> " + destinationFile.getAbsolutePath());
							if (selectedFile.isDirectory()) {
								FileUtils.copyFolder(selectedFile, destinationFile);
							} else {
								String fileName = selectedItem;
								String extension = "";
								int dotIndex = fileName.lastIndexOf('.');
								if (dotIndex > 0) {
									fileName = selectedItem.substring(0, dotIndex);
									extension = selectedItem.substring(dotIndex);
								}
								int count = 1;
								String newName = fileName + extension;
								File newFile = new File(destinationPath, newName);
								while (newFile.exists()) {
									newName = fileName + " (" + count + ")" + extension;
									newFile = new File(destinationPath, newName);
									count++;
								}
								Files.copy(selectedFile.toPath(), newFile.toPath());
							}
						} catch (IOException ex) {
							ex.printStackTrace();
						}
					}
				}

				pathField.setText(destinationPath);

				updateListView(listView, destinationPath);
			}
		});

		pieChartButton.setOnAction(e -> {
			String path = pathField.getText().trim();
			File folder = new File(path);
			if (!path.isEmpty() && folder.exists() && folder.isDirectory()) {
				DiagramUtils.showDiskUsageChart(path);
			} else {
				SecurityUtils.showAlert("Invalid Path", "Please select a valid directory to view disk usage.");
			}
		});

		moveButton.setOnAction(e -> {
			List<String> selectedItems = listView.getSelectionModel().getSelectedItems();
			String selectedPath = pathField.getText();

			DirectoryChooser directoryChooser = new DirectoryChooser();
			directoryChooser.setTitle("Select Destination Directory");
			File selectedDirectory = directoryChooser.showDialog(primaryStage);

			if (selectedItems != null && !selectedItems.isEmpty() && selectedDirectory != null) {
				String destinationPath = selectedDirectory.getAbsolutePath();

				for (String selectedItem : selectedItems) {
					File selectedFile = new File(selectedPath, selectedItem);
					File destinationFile = new File(destinationPath, selectedItem);

					if (selectedFile.exists()) {
						try {
							logHistory("MOVE",
									selectedFile.getAbsolutePath() + " -> " + destinationFile.getAbsolutePath());
							if (selectedFile.isDirectory()) {
								FileUtils.moveFolder(selectedFile, destinationFile);
							} else {
								if (destinationFile.exists()) {
									String fileName = selectedItem;
									String extension = "";
									int dotIndex = fileName.lastIndexOf('.');
									if (dotIndex > 0) {
										fileName = selectedItem.substring(0, dotIndex);
										extension = selectedItem.substring(dotIndex);
									}
									int count = 1;
									String newName = fileName + " (1)" + extension;
									File newFile = new File(destinationPath, newName);
									while (newFile.exists()) {
										count++;
										newName = fileName + " (" + count + ")" + extension;
										newFile = new File(destinationPath, newName);
									}
									Files.move(selectedFile.toPath(), newFile.toPath(),
											StandardCopyOption.REPLACE_EXISTING);
								} else {
									Files.move(selectedFile.toPath(), destinationFile.toPath(),
											StandardCopyOption.REPLACE_EXISTING);
								}
							}
						} catch (IOException ex) {
							ex.printStackTrace();
						}
					}
				}

				pathField.setText(destinationPath);

				updateListView(listView, selectedPath);
				updateListView(listView, destinationPath);

				fileInfoBox.getChildren().clear();
			}
		});

		backButton.setOnAction(e -> {
			if (!directoryHistory.isEmpty()) {
				String previousPath = directoryHistory.pop();
				pathField.setText(previousPath);
				updateListView(listView, previousPath);
			}
		});

		listView.setOnMouseClicked(e -> {
			if (e.getClickCount() == 2) {
				String selectedItem = listView.getSelectionModel().getSelectedItem();
				String currentPath = pathField.getText();
				if (selectedItem != null && !currentPath.isEmpty()) {
					File selectedFile = new File(currentPath, selectedItem);
					if (selectedFile.isDirectory()) {
						directoryHistory.push(pathField.getText());
						pathField.setText(selectedFile.getAbsolutePath());
						updateListView(listView, selectedFile.getAbsolutePath());
					}
				}
			} else if (e.getClickCount() == 1) {
				String selectedItem = listView.getSelectionModel().getSelectedItem();
				String selectedPath = pathField.getText();
				if (selectedItem != null && !selectedPath.isEmpty()) {
					fileNameField.setText(selectedItem);
					File selectedFile = new File(selectedPath, selectedItem);
					if (selectedFile.exists() && !selectedFile.isDirectory()) {
						fileInfoBox.getChildren().clear();

						try {
							BasicFileAttributes attr = Files.readAttributes(selectedFile.toPath(),
									BasicFileAttributes.class);
							long fileSize = selectedFile.length();
							String creationTime = formatDate(attr.creationTime().toMillis());
							String lastModifiedTime = formatDate(attr.lastModifiedTime().toMillis());

							Label fileInfoLabel = new Label("File Information:");
							Label sizeLabel = new Label("Size: " + fileSize + " bytes");
							Label creationLabel = new Label("Creation Time: " + creationTime);
							Label lastModifiedLabel = new Label("Last Modified Time: " + lastModifiedTime);

							fileInfoLabel.getStyleClass().add("file-info");
							sizeLabel.getStyleClass().add("file-info");
							creationLabel.getStyleClass().add("file-info");
							lastModifiedLabel.getStyleClass().add("file-info");
							fileInfoBox.getChildren().addAll(fileInfoLabel, sizeLabel, creationLabel,
									lastModifiedLabel);
						} catch (IOException ex) {
							ex.printStackTrace();
						}
					}
				}
			}
		});

		listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				copyButton.setVisible(true);
				moveButton.setVisible(true);
				deleteButton.setVisible(true);
			} else {
				copyButton.setVisible(false);
				moveButton.setVisible(false);
				deleteButton.setVisible(false);
			}
		});

		viewContentButton.setOnAction(e -> {
			String selectedItem = listView.getSelectionModel().getSelectedItem();
			String selectedPath = pathField.getText();
			if (selectedItem != null && !selectedPath.isEmpty()) {
				File selectedFile = new File(selectedPath, selectedItem);
				if (selectedFile.exists() && !selectedFile.isDirectory()) {
					openFile(selectedFile);
				}
			}
		});

		archiveItem.setOnAction(event -> {
			List<String> selectedItems = listView.getSelectionModel().getSelectedItems();
			if (!selectedItems.isEmpty()) {
				List<File> selectedFiles = selectedItems.stream()
						.map(item -> new File(pathField.getText(), item))
						.filter(File::exists)
						.collect(Collectors.toList());

				if (!selectedFiles.isEmpty()) {
					ZipUtils.showCustomArchiveDialog(
							selectedFiles,
							listView,
							updatedPath -> updateListView(listView, updatedPath));
				}
			}
		});

		extractItem.setOnAction(event -> {
			String selectedItem = listView.getSelectionModel().getSelectedItem();
			if (selectedItem != null) {
				File selectedFile = new File(pathField.getText(), selectedItem);
				if (selectedFile.exists() && selectedFile.getName().endsWith(".zip")) {
					ZipUtils.showCustomExtractDialog(selectedFile, listView,
							updatedPath -> updateListView(listView, updatedPath));
				}
			}
		});

		searchButton.setOnAction(e -> {
			String query = searchField.getText().trim();
			String directoryPath = pathField.getText();

			if (!directoryPath.isEmpty()) {
				if (query.isEmpty()) {
					updateListView(listView, directoryPath);
				} else {
					FileUtils.searchFiles(directoryPath, query, listView);
				}
			}
		});

		extensionFilter.setOnAction(e -> {
			selectedExtension = extensionFilter.getValue();
			updateListView(listView, pathField.getText());
		});

		sortOptions.setOnAction(e -> {
			selectedSortOption = sortOptions.getValue();
			updateListView(listView, pathField.getText());
		});

		historyButton.setOnAction(e -> showHistoryWindow());

		root.getChildren().addAll(browseContainer, listView, buttonsFile, fileControls, bookmarkBox);

		scene = new Scene(root, 600, 600);
		scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
		primaryStage.setTitle("File Manager");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	private void logHistory(String action, String filePath) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(HISTORY_FILE, true))) {
			String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
			writer.write(timestamp + " - " + action + ": " + filePath);
			writer.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void updateListView(ListView<String> listView, String directoryPath) {
		File directory = new File(directoryPath);
		listView.getItems().clear();

		if (directory.exists() && directory.isDirectory()) {
			File[] files = directory.listFiles();

			if (files != null) {
				List<File> filteredFiles = Arrays.stream(files)
						.filter(file -> !file.getName().endsWith(".bak"))
						.filter(file -> selectedExtension.equals("All") || file.getName().endsWith(selectedExtension))
						.collect(Collectors.toList());

				filteredFiles = sortFiles(filteredFiles);

				if (!root.getChildren().contains(searchContainer)) {
					root.getChildren().add(3, searchContainer);
				}

				if (!filteredFiles.isEmpty()) {
					for (File file : filteredFiles) {
						listView.getItems().add(file.getName());
					}
				} else {
					listView.getItems().add("🛈 No files match your search or filter.");
				}
			}
		} else {
			listView.getItems().add("⚠️ Directory does not exist or is not a directory.");
			root.getChildren().remove(searchContainer);
		}
	}

	private void showHistoryWindow() {
		Stage historyStage = new Stage();
		historyStage.setTitle("File History");

		VBox historyRoot = new VBox();
		ListView<String> historyListView = new ListView<>();
		Button undoButton = new Button("Undo Selected");
		Button clearHistoryButton = new Button("Clear History");

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
				undoAction(selectedItem, listView, historyListView);
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

	private void updateBookmarkBox() {
		List<String> lines = new ArrayList<>();

		bookmarkBox.getChildren().removeIf(node -> node.getStyleClass().contains("bookmark-item-row"));

		try (BufferedReader reader = new BufferedReader(new FileReader(BOOKMARKS_FILE))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (!lines.contains(line)) {
					lines.add(line);
					String bookmarkPath = line;
					File file = new File(bookmarkPath);
					String folderName = file.getName();

					HBox bookmarkItem = new HBox();
					bookmarkItem.setSpacing(5);
					bookmarkItem.setAlignment(Pos.CENTER_LEFT);
					bookmarkItem.getStyleClass().add("bookmark-item-row");

					Label folderLabel = new Label("📂 " + folderName);
					folderLabel.getStyleClass().add("bookmark-label");

					Button deleteBookmarkButton = new Button("❌");
					deleteBookmarkButton.getStyleClass().add("bookmark-delete");
					deleteBookmarkButton.setOnAction(e -> {
						BookmarkUtils.deleteBookmark(bookmarkPath, BOOKMARKS_FILE);
						updateBookmarkBox();
					});

					HBox folderButtonWrapper = new HBox(folderLabel, deleteBookmarkButton);
					folderButtonWrapper.setAlignment(Pos.CENTER_LEFT);
					folderButtonWrapper.setSpacing(5);
					folderButtonWrapper.getStyleClass().add("bookmark-item");
					folderButtonWrapper.setOnMouseClicked(e -> {
						pathField.setText(bookmarkPath);
						updateListView(listView, bookmarkPath);
						fileInfoBox.getChildren().clear();
					});

					bookmarkItem.getChildren().add(folderButtonWrapper);
					bookmarkBox.getChildren().add(bookmarkItem);
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void clearHistory() {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(HISTORY_FILE))) {
			writer.write("");
			System.out.println("History was deleted.");
		} catch (IOException e) {
			System.out.println("Error trying deleting history");
			e.printStackTrace();
		}
	}

	private void undoAction(String historyEntry, ListView<String> fileListView, ListView<String> historyListView) {
		if (historyEntry.contains("DELETE")) {
			String filePath = historyEntry.split(": ")[1];
			File backupFile = new File(filePath + ".bak");
			File restoredFile = new File(filePath);

			if (backupFile.exists()) {
				try {
					Files.move(backupFile.toPath(), restoredFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
					System.out.println("File restored successfully: " + restoredFile.getAbsolutePath());

					removeHistoryEntry(historyEntry);
					historyListView.getItems().remove(historyEntry);

					updateListView(fileListView, restoredFile.getParent());

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else if (historyEntry.contains("RENAME")) {
			String[] parts = historyEntry.split(": ")[1].split(" -> ");
			File renamedFile = new File(parts[1]);
			File originalFile = new File(parts[0]);

			if (renamedFile.exists()) {
				boolean renamedBack = renamedFile.renameTo(originalFile);
				if (renamedBack) {
					removeHistoryEntry(historyEntry);
					historyListView.getItems().remove(historyEntry);

					updateListView(fileListView, originalFile.getParent()); // 🔹 Оновлення списку після Undo
				}
			}
		} else if (historyEntry.contains("MOVE")) {
			String[] parts = historyEntry.split(": ")[1].split(" -> ");
			File movedFile = new File(parts[1]);
			File originalLocation = new File(parts[0]);

			if (movedFile.exists()) {
				try {
					Files.move(movedFile.toPath(), originalLocation.toPath(), StandardCopyOption.REPLACE_EXISTING);
					removeHistoryEntry(historyEntry);
					historyListView.getItems().remove(historyEntry);

					updateListView(fileListView, originalLocation.getParent()); // 🔹 Оновлення списку після Undo
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else if (historyEntry.contains("COPY")) {
			String[] parts = historyEntry.split(": ")[1].split(" -> ");
			File copiedFile = new File(parts[1]);

			if (copiedFile.exists()) {
				boolean deleted = copiedFile.delete();
				if (deleted) {
					removeHistoryEntry(historyEntry);
					historyListView.getItems().remove(historyEntry);

					updateListView(fileListView, copiedFile.getParent());
					System.out.println("File copy successfully deleted: " + copiedFile.getAbsolutePath());
				} else {
					System.out.println("A copy of the file could not be deleted " + copiedFile.getAbsolutePath());
				}
			}
		}
	}

	private void removeHistoryEntry(String entryToRemove) {
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

	private List<File> sortFiles(List<File> files) {
		switch (selectedSortOption) {
			case "Name (A-Z)":
				files.sort(Comparator.comparing(File::getName));
				break;
			case "Name (Z-A)":
				files.sort(Comparator.comparing(File::getName).reversed());
				break;
			case "Size (Asc)":
				files.sort(Comparator.comparingLong(File::length));
				break;
			case "Size (Desc)":
				files.sort(Comparator.comparingLong(File::length).reversed());
				break;
			case "Date (New-Old)":
				files.sort((f1, f2) -> Long.compare(getFileCreationTime(f2), getFileCreationTime(f1)));
				break;
			case "Date (Old-New)":
				files.sort((f1, f2) -> Long.compare(getFileCreationTime(f1), getFileCreationTime(f2)));
				break;
		}
		return files;
	}

	private long getFileCreationTime(File file) {
		try {
			return Files.readAttributes(file.toPath(), BasicFileAttributes.class).creationTime().toMillis();
		} catch (IOException e) {
			return 0;
		}
	}

	private void openFile(File file) {
		try {
			Runtime.getRuntime().exec("cmd /c start \"\" \"" + file.getAbsolutePath() + "\"");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String formatDate(long millis) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(millis);
	}

	public static void main(String[] args) {
		launch(args);
	}
}
