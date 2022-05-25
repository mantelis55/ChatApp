package ws;

import agents.*;
import java.io.IOException;
import java.util.*;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import org.json.JSONObject;

@Singleton
@ServerEndpoint("/ws")
@LocalBean
public class WebSocketEP {
	public static Map<String, Session> sessions = new HashMap<String, Session>();

	@OnOpen
	public void OnOpen(Session session) {
		if (!sessions.containsValue(session)) {
			sessions.put(session.getId(), session);
			System.out.println("Connected, Session list size - " + sessions.size());
			AgentCenter.CreateUserAgent(session.getId());
		}
	}

	@OnMessage
	public void OnMessage(String message, Session session) {
		if (session.isOpen()) {
			Session s = sessions.get(session.getId());
			if (s != null) {
				try {
					AgentCenter.agents.get(session.getId()).setUsername(message);
					JSONObject json = new JSONObject();
					json.put("purpose", "SESSIONID");
					json.put("message", s.getId());
					s.getBasicRemote().sendText(json.toString());
					SendUserListUpdate();

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@OnClose
	public void OnClose(Session session) {
		sessions.remove(session.getId());
		System.out.println("Disconnected, Session list size - " + sessions.size());
		AgentCenter.RemoveUserAgent(session.getId());
		SendUserListUpdate();
	}

	@OnError
	public void OnError(Session session, Throwable error) {
		sessions.remove(session.getId());
		AgentCenter.RemoveUserAgent(session.getId());
		SendUserListUpdate();
		error.printStackTrace();
	}

	public void SendUserListUpdate() {
		JSONObject jsonUsers = new JSONObject();
		jsonUsers.put("purpose", "USERLIST");
		for (Session a : sessions.values()) {
			try {
				a.getBasicRemote().sendText(jsonUsers.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}