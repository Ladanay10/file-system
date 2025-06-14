package com.example;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import javafx.scene.control.Alert;

public class SecurityUtils {

	public static void encryptFile(File file, String password) throws Exception {
		SecretKey secretKey = generateKey(password);
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);

		byte[] fileContent = Files.readAllBytes(file.toPath());
		byte[] encryptedContent = cipher.doFinal(fileContent);

		String encryptedFilePath = file.getAbsolutePath() + ".enc";
		Files.write(Paths.get(encryptedFilePath), encryptedContent);

		file.delete();
	}

	public static void decryptFile(File file, String password) throws Exception {
		boolean success = false;
		try {
			SecretKey secretKey = generateKey(password);
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, secretKey);

			byte[] encryptedContent = Files.readAllBytes(file.toPath());
			byte[] decryptedContent = cipher.doFinal(encryptedContent);

			String decryptedFilePath = file.getAbsolutePath().replace(".enc", "");
			Files.write(Paths.get(decryptedFilePath), decryptedContent);

			success = true;
		} catch (Exception e) {
			System.err.println(" Decryption failed: " + e.getMessage());
			e.printStackTrace();
			throw new Exception("Incorrect password or corrupted file.");
		}

		if (success) {
			if (file.delete()) {
				System.out.println(" Encrypted file deleted: " + file.getAbsolutePath());
			} else {
				System.err.println(" Failed to delete encrypted file: " + file.getAbsolutePath());
			}
		}
	}

	private static SecretKey generateKey(String password) throws Exception {
		byte[] key = new byte[16];
		byte[] passwordBytes = password.getBytes();
		System.arraycopy(passwordBytes, 0, key, 0, Math.min(passwordBytes.length, key.length));

		return new SecretKeySpec(key, "AES");
	}

	public static void showAlert(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
}
