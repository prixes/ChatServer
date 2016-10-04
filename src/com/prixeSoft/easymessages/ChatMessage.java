package com.prixeSoft.easymessages;

import java.io.Serializable;

public class ChatMessage implements Serializable {

	private static final long serialVersionUID = 1L;
	public static final int ONLINEUSERS = 0;
	public static final int MESSAGE = 1;
	public static final int LOGOUT = 2;
	public static final int WHOAMI = 3;
	private int type;
	private String message;

	public ChatMessage(int type, String message) {
		this.type = type;
		this.message = message;
	}

	public int getType() {
		return type;
	}

	public String getMessage() {
		return message;
	}
}