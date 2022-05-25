package agents;

import java.util.ArrayList;
import javax.ejb.Local;
import javax.jms.Message;

@Local
public interface Agent{
	public void handleMessage(Message message);
	public void setID(String id);
	public void setUsername(String user);
	public String getID();
	public String getUsername();
	public ArrayList<String> getMessages();
}
