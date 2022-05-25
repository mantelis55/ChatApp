package agents;

import java.util.HashMap;
import java.util.Map;
import javax.ejb.LocalBean;
import javax.ejb.Stateful;
import javax.naming.InitialContext;
import javax.naming.NamingException;

@Stateful
@LocalBean
public class AgentCenter {
	
	public static Map<String,Agent> agents = new HashMap<String,Agent>();	
	
	static public Agent CreateUserAgent(String sessionID) {
		InitialContext ctx;
		try {
			ctx = new InitialContext();
			Agent agent = (Agent)ctx.lookup("java:global/ChatServer/UserAgent!agents.Agent");
			agent.setID(sessionID);
			agents.put(sessionID,agent);
			System.out.println("Created Agent, Agent list size - " + agents.size());
			return agent;
		} catch (NamingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	static public Agent CreateChatAgent() {
		InitialContext ctx;
		try {
			ctx = new InitialContext();
			Agent agent = (Agent)ctx.lookup("java:global/ChatServer/ChatAgent!agents.Agent");
			return agent;
		} catch (NamingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	static public void RemoveUserAgent(String sessionID) {
		agents.remove(sessionID);
		System.out.println("Deleted Agent, Agent list size - " + agents.size());
	}
	
	static public String[] getAllUsers() {
		String[] usernames = new String[agents.size()];
		int i = 0;
		for(Agent a : agents.values()) {
			usernames[i] = a.getUsername() + "\n";
			i++;
		}
		return usernames;
	}
	
	static public String getIDByUsername(String username) {
		for(Agent a : agents.values()) {
			if(a.getUsername().equals(username)){
				return a.getID();
			}
		}
		return null;
	}
}