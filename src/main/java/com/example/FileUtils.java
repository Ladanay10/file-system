package com.example;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.scene.control.ListView;

public class FileUtils {

	public static void deleteFolder(File folder) {
		File[] files = folder.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					deleteFolder(file);
				} else {
					file.delete();
				}
			}
		}
		folder.delete();
	}

	public static void copyFolder(File source, File dest) throws IOException {
		if (source.isDirectory()) {
			if (!dest.exists()) {
				dest.mkdir();
			}

			String[] files = source.list();
			for (String file : files) {
				File srcFile = new File(source, file);
				File destFile = new File(dest, file);
				if (destFile.exists()) {
					int count = 1;
					String fileName = file;
					String extension = "";
					int dotIndex = file.lastIndexOf('.');
					if (dotIndex > 0) {
						fileName = file.substring(0, dotIndex);
						extension = file.substring(dotIndex);
					}
					while (destFile.exists()) {
						destFile = new File(dest.getParent(), fileName + " (" + count + ")" + extension);
						count++;
					}
				}
				if (srcFile.isDirectory()) {
					copyFolder(srcFile, destFile);
				} else {
					Files.copy(srcFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				}
			}
		}
	}

	public static void moveFolder(File source, File dest) throws IOException {
		if (source.isDirectory()) {
			if (!dest.exists()) {
				dest.mkdir();
			}

			String[] files = source.list();
			for (String file : files) {
				File srcFile = new File(source, file);
				File destFile = new File(dest, file);
				moveFolder(srcFile, destFile);
			}
			source.delete();
		} else {
			Files.move(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
	}

	public static boolean deleteFileOrDirectory(File file) {
		if (file.isDirectory()) {
			File[] contents = file.listFiles();
			if (contents != null) {
				for (File f : contents) {
					deleteFileOrDirectory(f);
				}
			}
		}
		return file.delete();
	}

	public static void searchFiles(String directoryPath, String query, ListView<String> listView) {
		try (Stream<Path> paths = Files.walk(Paths.get(directoryPath))) {
			List<String> results = paths
					.filter(Files::isRegularFile)
					.map(Path::getFileName)
					.map(Path::toString)
					.filter(name -> name.toLowerCase().contains(query.toLowerCase()))
					.collect(Collectors.toList());

			listView.getItems().clear();
			if (!results.isEmpty()) {
				listView.getItems().addAll(results);
			} else {
				listView.getItems().add("No matching files found.");
			}
		} catch (IOException e) {
			e.printStackTrace();
			listView.getItems().clear();
			listView.getItems().add("Error searching files.");
		}
	}

}
