package com.example.ymdbanking.model;

public class Message
{
	private String fromUser;
	private String toUser;
	private String message;
	private String messageId;

	public Message()
	{
		//Empty Constructor
	}

	public Message(String fromUser,String toUser,String message,String messageId)
	{
		this.fromUser = fromUser;
		this.toUser = toUser;
		this.message = message;
		this.messageId = messageId;
	}

	public String getFromUser() {return fromUser;}
	public void setFromUser(String fromUser) {this.fromUser = fromUser;}
	public String getToUser() {return toUser;}
	public void setToUser(String toUser) {this.toUser = toUser;}
	public String getMessage() {return message;}
	public void setMessage(String message) {this.message = message;}
	public String getMessageId() {return messageId;}
	public void setMessageId(String messageId) {this.messageId = messageId;}
}
