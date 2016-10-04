package com.prixeSoft.easymessages;
import java.io.IOException;
import java.util.Scanner;

public class General {
	private static final int port = 10514;
	private Scanner readInput=new Scanner(System.in);
	private String cmdInput ;
	
	private FileLogger fileLogger = new FileLogger();
	private Server server;
	
	
	 public static void main(String[] args) {
		 General general= new General();
		 general.start(port);
		 
		 while(true)
		 {
			  general.cmdInput= general.readInput.nextLine();
			  general.checkCmd(general.cmdInput);
		 }
		 
	    
	 }
	 
	 void checkCmd(String cmdInput)
	 {
		 if(cmdInput.startsWith("/"))
		 {
			 switch(cmdInput)
			 {
			 	case "/start" : server.start();
			 	case "/stop" : server.stop();
			 }
		 } else {
			 server.sendSystemMsg("***Admin***: " , cmdInput);
		 }
	 }
	 
	 void start(int port)
	 {
		 if (server != null) {
				server.stop();
				server = null;
				return;
			}

			startNewServer(port);
			

			try {
				fileLogger.startNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
	 }
	 
	// global messages and filelogger writing them immediately after confirming them
		void appendRoom(String str) {
			System.out.print(str);
			try {
				fileLogger.receiveMessage(str);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	 
	//server log
		void appendEvent(String str) {
			System.out.println(str);
			try {
				fileLogger.receiveMessage(str);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	 
	//reserves the port for the server
		void startNewServer(int port) {
			server = new Server(port);
			new ServerRunning().start();
		}
		
		class ServerRunning extends Thread {
			public void run() {
				server.start(); // should execute until if fails
				// the server failed
				
				appendEvent("Server crashed\n");
				server = null;

			}
		}
}
