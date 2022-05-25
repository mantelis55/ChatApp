package rest;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import ws.WebSocketEP;
import javax.ejb.EJB;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.websocket.Session;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.DELETE;
import javax.ws.rs.Produces;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import org.json.*;
import agents.Agent;
import agents.AgentCenter;
import agents.JMSTopicPublisher;

@Path("/")
public class RestCalls {
	public static Connection connection;

	@EJB
	JMSTopicPublisher factory;
	
	@POST
	@Path("/users/register")
	public Response register(String jsonString) {
		if(connection == null) {
			Connect();
		}
		JSONObject json = new JSONObject(jsonString);
	    String username = json.get("username").toString();
	    String password = json.get("password").toString();
	    Statement statement;
		try {
			statement = connection.createStatement();
			statement.executeUpdate("INSERT INTO users (username, password) VALUES " + " ('" + username + "','"+ password + "')");
		} catch (SQLException e) {
			return Response
					.status(Response.Status.BAD_REQUEST)
	                .entity(e.toString())
	                .build();
		}
		return Response
				.status(Response.Status.CREATED)
                .entity("User created")
                .build();
	}
	
	@POST
	@Path("/users/login")
	@Produces("application/json")
	public Response login(String jsonString){
		if(connection == null) {
			Connect();
		}
		JSONObject json = new JSONObject(jsonString);
	    String username = json.get("username").toString();
	    String password = json.get("password").toString();
	    Statement statement;
	    ResultSet result;
	    int count = 0;
		try {
			statement = connection.createStatement();
			result = statement.executeQuery("SELECT COUNT(*) AS recordCount FROM users WHERE username = '" + username +"' AND password = '" + password + "'");
			if(result.next())
				count = result.getInt("recordCount");
		} catch (SQLException e) {
			return Response
					.status(Response.Status.BAD_REQUEST)
	                .entity(e.toString())
	                .build();
		}

		if(count == 1) {
			if(AgentCenter.getIDByUsername(username) == null) {
				return Response
		                .status(200)
		                .entity("Logged in")
		                .build();
			}
			else {
				return Response
		                .status(400)
		                .entity("User already logged in using this account")
		                .build();
			}
		}
		else return Response
                .status(401)
                .entity("Incorrect credentials")
                .build();
	}

	@POST
	@Path("/messages/all")
	@Produces("application/json")
	public Response messageAll(String jsonString){
		JSONObject json = new JSONObject(jsonString);
	    String message = json.get("message").toString();
	    String sessionID = json.get("sessionID").toString();
	    Session s = WebSocketEP.sessions.get(sessionID);
	    if(s != null) {
	    	MessageProducer producer = factory.getProducer();
	    	try {
				Message sendingMessage = factory.getSession().createMessage();
				sendingMessage.setStringProperty("message", message);
				sendingMessage.setStringProperty("senderSessionID", sessionID);
				producer.send(sendingMessage);
			} catch (JMSException e) {
				e.printStackTrace();
			}
	    	
	        return Response
	                .status(200)
	                .entity("Message sent to ALL")
	                .build();
	    }
	    else {
	        return Response
	                .status(400)
	                .entity("Unauthorized action")
	                .build();
	    }
	}
	
	@POST
	@Path("/messages/user")
	@Produces("application/json")
	public Response messageTo(String jsonString){
		JSONObject json = new JSONObject(jsonString);
	    String message = json.get("message").toString();
	    String sessionID = json.get("sessionID").toString();
	    String reciever = json.get("reciever").toString();
	    Session s = WebSocketEP.sessions.get(sessionID);
	    if(s != null) {
	    	String recieverID = AgentCenter.getIDByUsername(reciever);
	    	if(recieverID != null) {
		    	MessageProducer producer = factory.getProducer();
		    	try {
					Message sendingMessage = factory.getSession().createMessage();
					sendingMessage.setStringProperty("message", message);
					sendingMessage.setStringProperty("senderSessionID", sessionID);
					sendingMessage.setStringProperty("recieverSessionID", recieverID);
					producer.send(sendingMessage);
				} catch (JMSException e) {
					e.printStackTrace();
				}
		    	
		        return Response
		                .status(200)
		                .entity("Message sent to " + reciever)
		                .build();
	    	}
	    	else {
	    		return Response
	    				.status(400)
		                .entity("User - " + reciever + " not found")
		                .build();
	    	}

	    }
	    else {
	        return Response
	                .status(400)
	                .entity("Unauthorized action")
	                .build();
	    }
	}

	@GET
	@Path("/users/loggedIn")
	@Produces("application/json")
	public Response loggedIn(){
    	JSONObject jsonUsers = new JSONObject();
		jsonUsers.put("message", AgentCenter.getAllUsers());
        return Response
                .status(200)
                .entity(jsonUsers.toString())
                .build();
	}
	
	@DELETE
	@Path("/users/loggedIn/{user}")
	public Response logoutUser(@PathParam("user") String username, String jsonString){
		JSONObject json = new JSONObject(jsonString);
	    String sessionID = json.get("sessionID").toString();
    	String senderID = AgentCenter.getIDByUsername(username);
    	if(senderID.equals(sessionID)) {
    		try {
				WebSocketEP.sessions.get(sessionID).close();
			} catch (IOException e) {
				e.printStackTrace();
			}
            return Response
                    .status(200)
                    .entity("User logged out")
                    .build();
    	}
    	else {
            return Response
                    .status(400)
                    .entity("Unauthenticated action")
                    .build();
    	}
	}
	
	@GET
	@Path("/messages/{user}")
	@Produces("application/json")
	public Response userMessages(@PathParam("user") String username){
    	JSONObject jsonUsers = new JSONObject();
    	Agent agent = AgentCenter.agents.get(AgentCenter.getIDByUsername(username));
    	if(agent != null) {
    		jsonUsers.put("messages", agent.getMessages());
            return Response
                    .status(200)
                    .entity(jsonUsers.toString())
                    .build();
    	}
    	else {
    		 return Response
                     .status(400)
                     .entity("User is not logged in")
                     .build();
    	}

	}
	
	@GET
	@Path("/users/registered")
	@Produces("application/json")
	public Response registered(){
		if(connection == null) {
			Connect();
		}
	    Statement statement;
	    ResultSet result;
		try {
			statement = connection.createStatement();
			result = statement.executeQuery("SELECT username FROM users");
			ArrayList<String> usernames = new ArrayList<String>();
			while (result.next()) { 
				usernames.add(result.getString(1));
			}
	        return Response
	                .status(200)
	                .entity(usernames.toString())
	                .build();
		} catch (SQLException e) {
			return Response
					.status(Response.Status.BAD_REQUEST)
	                .entity(e.toString())
	                .build();
		}
	}
	
	public void Connect() {
		try {
			connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/chat_base", "root", "");
			return;
		} catch (SQLException e) {
			return;
		}
	}
}