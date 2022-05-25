package agents;

import java.io.IOException;
import java.util.ArrayList;

import javax.annotation.PostConstruct;
import javax.ejb.*;
import javax.jms.JMSException;
import javax.jms.Message;

import org.json.JSONException;
import org.json.JSONObject;

import ws.WebSocketEP;

@Stateful
public class UserAgent implements Agent {
	
	String agentID;
	String username;
	public ArrayList<String> messages = new ArrayList<String>();

	@Override
	public void handleMessage(Message message) {
		try {
			JSONObject json = new JSONObject();
			json.put("message", message.getStringProperty("message"));
			messages.add(message.getStringProperty("message"));
			WebSocketEP.sessions.get(agentID).getBasicRemote().sendText(json.toString());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (JMSException e) {
			e.printStackTrace();
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
