package com.prixeSoft.easymessages;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class Accounts {

	private File file;
	private JPanel messagePanel;

	Accounts() {
		try {
			startNewFile();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(messagePanel, "Error creating new file !");
			e.printStackTrace();
		}
	}

	// creating new file for saving accounts
	String startNewFile() throws IOException {
		String time = getFileName();
		file = new File(time);
		file.createNewFile();
		return time;
	}

	// adding new name
	private void newName(String name) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));

		writer.newLine();
		writer.close();
	}

	// whole process when server wants to proceed new login
	public String login(String user) throws IOException {
		if (checkNameExistanse(user) == true) {
			int randomPIN = (int) (Math.random() * 9000) + 1000;
			return login(user + String.valueOf(randomPIN));
		} else {
			newName(user);
			return user;
		}
	}

	// boolean check if name exists
	private boolean checkNameExistanse(String name) throws IOException {
		List<String> listOfMessages;
		startNewFile();
		listOfMessages = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
		for (int i = 0; i < listOfMessages.size(); i++) {
			if (listOfMessages.get(i).equals(name)) {
				return true;
			}
		}
		return false;
	}

	// Deprecated
	boolean checkIfAdmin(String password) {
		if (password == "toor") {
			return true;
		}
		return false;
	}

	// formating the name of file for names
	String getFileName() {
		String result = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yy"));
		return result + " names.txt";
	}
}
