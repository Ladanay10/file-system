package com.example;

import java.io.File;
import java.text.CharacterIterator;
import java.util.HashMap;
import java.util.Map;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DiagramUtils {

	public static void showDiskUsageChart(String directoryPath) {
		File directory = new File(directoryPath);
		if (!directory.isDirectory())
			return;

		Stage chartStage = new Stage();
		chartStage.setTitle("Disk Usage Pie Chart");
		Label loadingLabel = new Label("Loading disk usage data...");
		VBox chartRoot = new VBox(loadingLabel);
		chartRoot.setPadding(new Insets(20));
		Scene scene = new Scene(chartRoot, 600, 450);
		chartStage.setScene(scene);
		chartStage.show();

		new Thread(() -> {
			Map<String, Long> typeSizeMap = new HashMap<>();
			collectFileSizesRecursive(directory, typeSizeMap);

			Platform.runLater(() -> {
				PieChart pieChart = new PieChart();
				for (Map.Entry<String, Long> entry : typeSizeMap.entrySet()) {
					String label = entry.getKey() + " (" + humanReadableByteCountBin(entry.getValue()) + ")";
					pieChart.getData().add(new PieChart.Data(label, entry.getValue()));
				}
				pieChart.setTitle("Disk Usage by File Category");

				chartRoot.getChildren().clear();
				chartRoot.getChildren().add(pieChart);
			});
		}).start();
	}

	private static String getCategoryByExtension(File file) {
		String name = file.getName().toLowerCase();
		String ext = name.contains(".") ? name.substring(name.lastIndexOf(".") + 1) : "";

		if (ext.matches("jpg|jpeg|png|gif|bmp|webp|tiff"))
			return "Images";
		if (ext.matches("mp4|mkv|avi|mov|flv|wmv"))
			return "Videos";
		if (ext.matches("mp3|wav|flac|aac|ogg|m4a"))
			return "Audio";
		if (ext.matches("pdf|doc|docx|xls|xlsx|ppt|pptx|txt|csv|md|rtf"))
			return "Documents";
		if (ext.matches("zip|rar|7z|tar|gz|iso"))
			return "Archives";
		if (ext.matches("exe|msi|apk|bat|sh|jar"))
			return "Executables";
		if (ext.matches("html|htm|css|js|ts|jsx|tsx"))
			return "Web Files";
		if (ext.matches("java|py|cpp|c|cs|kt|swift|php|rb|go"))
			return "Source Code";
		if (ext.matches("json|xml|yml|yaml|ini|log|conf"))
			return "Config Files";

		return "Other";
	}

	public static String humanReadableByteCountBin(long bytes) {
		long absB = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
		if (absB < 1024)
			return bytes + " B";
		long value = absB;
		CharacterIterator ci = new java.text.StringCharacterIterator("KMGTPE");
		for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
			value >>= 10;
			ci.next();
		}
		return String.format("%.1f %cB", value / 1024.0, ci.current());
	}

	private static void collectFileSizesRecursive(File folder, Map<String, Long> map) {
		File[] files = folder.listFiles();
		if (files == null)
			return;

		for (File file : files) {
			if (file.isDirectory()) {
				collectFileSizesRecursive(file, map); // 🔄 рекурсивно для папок
			} else {
				String category = getCategoryByExtension(file);
				long size = file.length();
				map.put(category, map.getOrDefault(category, 0L) + size);
			}
		}
	}

}
