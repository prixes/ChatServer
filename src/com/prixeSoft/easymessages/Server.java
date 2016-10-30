package com.prixeSoft.easymessages;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;


public class Server {

	private Accounts accounts;
	private ArrayList<ClientThread> arrayOfClients;
	private ServerSocket serverSocket = null;
	private Socket socket =new Socket();
	
	private static final SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss");;
	private int port;
	private boolean keepGoing = true;

	// construction of the server and account//button listener
	public Server(int port) {
		
		this.port = port;
		arrayOfClients = new ArrayList<ClientThread>();
		accounts = new Accounts();
		
	}

	// admin system message
	public void sendSystemMsg(String username ,String str) {		
		broadcast( username , str );
	}
	
	//server status
	public boolean checkStatus() {
		return keepGoing;
	}
	
	// server start
	public void start()  {
		
		try {
			serverSocket = new ServerSocket(port);
			
			while (keepGoing) {
				if (!keepGoing)
					break;
				else {
				display("Server waiting for Clients on port " + port + ".");
				socket = serverSocket.accept(); //can't close it properly 
				ClientThread newClientThread = new ClientThread(socket);
				arrayOfClients.add(newClientThread);
				newClientThread.start();
				}
			} 		
			
		} catch (IOException e) {
			System.out.print(e.toString());
			e.printStackTrace();
		}
	}

	// stopping server
	protected void stop() {
		
		keepGoing = false;
		try {
		for (int i = 0; i < arrayOfClients.size(); ++i) {
			
			ClientThread threadForClosing = arrayOfClients.get(i);
			//socket.getOutputStream().close();
			//socket.getOutputStream().close();
				threadForClosing.socket.close();
		}
		serverSocket.close();
		socket.close();
		
		} catch (IOException e) {
			System.out.print(e.toString());
			e.printStackTrace();
		}
	}

	// display message with time stamp
	private void display(String msg) {
		
		String time = getMessageDate() + " " + msg;
		System.out.println(time);
	}

	// getting date in correct form
	String getMessageDate() {
		
		String result = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
		return " " + result + " -";
	}

	// broadcasting all messages to users
	private synchronized void broadcast(String username,String message) {
		
		String messageReady = getMessageDate() + " " + username + ": " + message + "\n";
		System.out.print(messageReady);
		
		for (int i = arrayOfClients.size(); --i >= 0;) {
			
			ClientThread clientThread = arrayOfClients.get(i);
			if (!clientThread.writeMsg(messageReady)) {
				arrayOfClients.remove(i);
				display("Disconnected Client " + clientThread.username + " removed from list.");
			}
		}
	}
	

	String messageReady;
	// PM to specific user
	boolean privateMessage(String from,String to,String message){
		messageReady= getMessageDate() + " << " + from + ": " + message + "\n";   
				
		for(int i=0;i<arrayOfClients.size();i++) {
			if(arrayOfClients.get(i).username.equals(to)) {
				if (!arrayOfClients.get(i).writeMsg(from,messageReady)) {
					
					arrayOfClients.remove(i);
					display("Disconnected Client " + arrayOfClients.get(i).username + " removed from list.");
				}
				
				return true;		
			}		
		}
		return false;
	}
	
	
    // remove client from the online users
	synchronized void remove(int id) {
		for (int i = 0; i < arrayOfClients.size(); ++i) {
			
			ClientThread clientThread = arrayOfClients.get(i);
			if (clientThread.id == id) {
				arrayOfClients.remove(i);
				return;
			}
		}
	}

	// this thread will run for each client
	class ClientThread extends Thread {
		int id;
		String username , date;

		Socket socket;
		ComProtobuf.msg msg ;
		boolean alive = true;
		
		ClientThread(Socket socket) {
			
			this.socket = socket;
			try {
				msg  = ComProtobuf.msg.parseDelimitedFrom(socket.getInputStream());
				username = msg.getTo();
				username = accounts.login(username);
				serverNameReport(username);
				
				//probably have to add something :D
				display(username + " just connected.");
			} catch (IOException e) {
				display("Exception creating new Input/output Streams: " + e);
				return;
			}
			date = new Date().toString() + "\n";
		}

		// on run of the thread
		public void run() {
			//boolean keepGoing = true;
			while (alive) {
				try {
					if(! socket.isClosed())
					msg  = ComProtobuf.msg.parseDelimitedFrom(socket.getInputStream());
					if( msg == null ) {
						display(username + " disconnected with a QUIT message.");
						alive = false; socket.close();
					} else {
						takeAction();
					}
				} catch (IOException e) {
					e.printStackTrace();
					}	
			}
			remove(id);
			close();
		}

		void serverNameReport(String username)  {
			
			ComProtobuf.msg.Builder writeName = ComProtobuf.msg.newBuilder();
			writeName.setTypeValue(3);
			writeName.setTo(username);
			try {
				writeName.build().writeDelimitedTo(socket.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
		}
		void takeAction() {
			switch (msg.getType()) {
			
			case MESSAGE:
				broadcast( msg.getFrom(),msg.getMessage());
				break;
			case LOGOUT:
				close();
				display(username + " disconnected with a LOGOUT message.");
				alive = false;
				break;
			case ONLINEUSERS:
				writeMsg("List of the users connected at " + time.format(new Date()) + "\n");
				for (int i = 0; i < arrayOfClients.size(); ++i) {
					ClientThread clientThread = arrayOfClients.get(i);
					writeMsg((i + 1) + ") " + clientThread.username + " since " + clientThread.date);
				}
				break;
			case WHOAMI:
				ClientThread theClient = arrayOfClients.get(arrayOfClients.size() - 1);
				writeMsg(theClient.username);
				break;
			case PM:
					
				
				if(!privateMessage( msg.getFrom(),msg.getTo(),msg.getMessage())) 
					returnMsg( msg.getTo(),   "   Person is not online!");
				else
					returnMsg( msg.getTo(), msg.getMessage() );
			
			default:
				break;
			}
		}
		// on close
		private void close() {
			try {
				if (socket != null){
					alive= false;
					socket.close();
				}
			} catch (Exception e) {
				System.out.print(e.toString());
				e.printStackTrace();
			}
		}

		// write message with 2 arguments means its PM
		boolean returnMsg(String to,String message){
			messageReady= getMessageDate() + " >> " + to + ": " + message + "\n";
			if (!socket.isConnected()) {
				close();
				return false;
			}
				ComProtobuf.msg.Builder writeMsg = ComProtobuf.msg.newBuilder();
				writeMsg.setTypeValue(4);
				writeMsg.setFrom(to);
				writeMsg.setMessage(messageReady);
				try {
					writeMsg.build().writeDelimitedTo(socket.getOutputStream());
					
				} catch (IOException e) {
					e.printStackTrace();
					System.out.print(e.toString());
				}
				return true;
			
		}
		// write message with 2 arguments means its PM
		boolean writeMsg(String from,String message){
			if (!socket.isConnected()) {
				close();
				return false;
			}
				ComProtobuf.msg.Builder writeMsg = ComProtobuf.msg.newBuilder();
				writeMsg.setTypeValue(4);
				writeMsg.setFrom(from);
				writeMsg.setMessage(message);
				try {
					writeMsg.build().writeDelimitedTo(socket.getOutputStream());
					
				} catch (IOException e) {
					e.printStackTrace();
					System.out.print(e.toString());
				}
				return true;
			
		}
		// sending message to user
		private boolean writeMsg(String msg) {
			if (socket.isClosed())  return false;
			
				ComProtobuf.msg.Builder writeMsg = ComProtobuf.msg.newBuilder();
				writeMsg.setTypeValue(1);
				writeMsg.setMessage(msg);
				try {
					writeMsg.build().writeDelimitedTo(socket.getOutputStream());
				
				} catch (IOException e) {
					e.printStackTrace();
					System.out.print(e.toString());
			}
			return true;
		}
	}
}
