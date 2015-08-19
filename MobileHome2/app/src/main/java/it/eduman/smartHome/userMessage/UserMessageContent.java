package it.eduman.smartHome.userMessage;

import java.util.ArrayList;

public class UserMessageContent {
	private long id;
	private String username;
	private ArrayList<MessageContent> messageList;
	
	public UserMessageContent (long id, String username, ArrayList<MessageContent> messageList) {
		this.id = id;
		this.username = username;
		this.messageList = messageList;
	}
	
	public UserMessageContent (long id, String username) {
		this.id = id;
		this.username = username;
		this.messageList = new ArrayList<MessageContent>();
	}

	public long getId() {
		return id;
	}

	public UserMessageContent setId(long id) {
		this.id = id;
		return this;
	}

	public String getUsername() {
		return username;
	}

	public UserMessageContent setUsername(String username) {
		this.username = username;
		return this;
	}

	public ArrayList<MessageContent> getMessageList() {
		return messageList;
	}

	public UserMessageContent setMessageList(ArrayList<MessageContent> messageList) {
		this.messageList = messageList;
		return this;
	}
	
	public UserMessageContent deleteMessage(long messageID){
		for (MessageContent m : this.messageList){
			if (m.getId() == messageID){
				this.messageList.remove(m);
			}
		}
		return this;
	}
	
	public UserMessageContent deleteMessage(MessageContent messageContent){		
		if (this.messageList.contains(messageContent)){
			this.messageList.remove(messageContent);
		}
		return this;
	}
	

}
