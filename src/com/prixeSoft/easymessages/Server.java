package com.prixeSoft.easymessages;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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


	private static final SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss");;
	private int port;
	private boolean keepGoing = true;

	private ServerSocket serverSocket = null;
	private Socket socket =new Socket();
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
	public boolean checkStatus()
	{
		return keepGoing;
	}
	
	// server start
	public void start() {
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
		
		for (int i = 0; i < arrayOfClients.size(); ++i) {
			ClientThread threadForClosing = arrayOfClients.get(i);
			try {
				threadForClosing.streamInput.close();
				threadForClosing.streamOutput.close();
				threadForClosing.socket.close();
			} catch (IOException io) {
				System.out.print(io.toString());
			}
		}
		try {
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
		Socket socket;
		ObjectInputStream streamInput;
		ObjectOutputStream streamOutput;

		int id;
		String username;
		//com.prixeSoft.easymessages.ChatMessage chatMsg;
		ComProtobuf.msg msg ;
		String date;

		ClientThread(Socket socket) {
			
			this.socket = socket;
			try {
				//streamOutput = new ObjectOutputStream(socket.getOutputStream());
				//streamInput = new ObjectInputStream(socket.getInputStream());
				
					msg  = ComProtobuf.msg.parseDelimitedFrom(socket.getInputStream());
				System.out.print("WTF");
				//if(msg.getType().hashCode() == 3){
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
			while (keepGoing) {
					
					try {
						
						//System.out.print(socket.getInputStream());
						//DataInputStream in = new DataInputStream(socket.getInputStream());
						msg  = ComProtobuf.msg.parseDelimitedFrom(socket.getInputStream());
						if( msg == null ) 
						{
							display(username + " disconnected with a LOGOUT message.");
							keepGoing=false; socket.close();
						} else {
						takeAction();
						 }
					} catch (IOException e) {
						// TODO Auto-generated catch block
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		}
		void takeAction() {
			switch (msg.getType()) {
			case MESSAGE:
				broadcast( msg.getFrom(),msg.getMessage());
				break;
			case LOGOUT:
				display(username + " disconnected with a LOGOUT message.");
				close();
				keepGoing = false;
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
			default:
				break;
			}
		}
		// on close
		private void close() {
			try {
				if (streamOutput != null)
					streamOutput.close();
				if (streamInput != null)
					streamInput.close();
				if (socket != null)
					socket.close();
			} catch (Exception e) {
				System.out.print(e.toString());
				e.printStackTrace();
			}
		}

		// sending message to user
		private boolean writeMsg(String msg) {
			if (!socket.isConnected()) {
				close();
				return false;
			}
			
				//streamOutput.writeObject(msg);
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
