package agents;

import java.util.ArrayList;

import javax.ejb.Stateful;
import javax.jms.JMSException;
import javax.jms.Message;

@Stateful
public class ChatAgent implements Agent {

	String agentID;
	String username;
	public ArrayList<String> messages = new ArrayList<String>();

	@Override
	public void handleMessage(Message message) {
		String sender = "";
		try {
			sender = message.getStringProperty("senderSessionID");
		} catch (JMSException e) {
			e.printStackTrace();
		}
		for(Agent a : AgentCenter.agents.values()) {
			if(!sender.equals(a.getID()))
				a.handleMessage(message);
		}
	}
	
	public void setID(String id) {
		agentID = id;
	}
	public String getID() {
		return agentID;
	}
	
	public void setUsername(String user) {
		username = user;
	}
	
	public String getUsername() {
		return username;
	}
	
	public ArrayList<String> getMessages(){
		return messages;
	}
}
