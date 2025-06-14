package com.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class BookmarkUtils {

	public static void saveBookmark(String path, String BOOKMARKS_FILE) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(BOOKMARKS_FILE, true))) {
			writer.write(path);
			writer.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void deleteBookmark(String pathToDelete, String BOOKMARKS_FILE) {
		try {
			File inputFile = new File(BOOKMARKS_FILE);
			File tempFile = new File("bookmarks_temp.txt");

			try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
					BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
				String currentLine;
				while ((currentLine = reader.readLine()) != null) {
					if (!currentLine.trim().equals(pathToDelete)) {
						writer.write(currentLine);
						writer.newLine();
					}
				}
			}

			if (!inputFile.delete() || !tempFile.renameTo(inputFile)) {
				throw new IOException("Error when updating bookmarks list");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
