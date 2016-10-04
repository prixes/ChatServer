package com.prixeSoft.easymessages;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileLogger {
	String message;
	File file;
	
//creating new file
	String startNewFile() throws IOException {
		String time = getFileName();
		file = new File(time);
		file.createNewFile();
		return time;
	}
//when server receive message by this method it safes it instantly in file and close it
	void receiveMessage(String text) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
		writer.write(text);
		writer.newLine();
		writer.close();
	}
//get name of file for chat log
	String getFileName() {
		String result = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yy HH-mm-ss"));
		return "ChatLog " + result + ".txt";
	}

	//@deprecated methods
	 /*
	 String getChatLog() throws IOException {
		 List<String> listOfMessages;
		 listOfMessages= Files.readAllLines(file.toPath(),StandardCharsets.UTF_8);
		String result="";
		for(int i=0;i<listOfMessages.size();i++)
		{
			result +=listOfMessages.get(i) + "\n";
		} 
		 return result;
	 }
	 String getMessageDate() {
			String result= LocalDateTime.now()
					.format(DateTimeFormatter.ofPattern("[dd.MM.yy HH:mm:ss"));
			return  "[ " + result + " ] - Admin: ";
	}
	 */
}
