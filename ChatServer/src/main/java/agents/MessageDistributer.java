package agents;

import javax.ejb.ActivationConfigProperty;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.ejb.MessageDriven;

@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "jms/topic/publicTopic") })
public class MessageDistributer implements MessageListener {
	
	public void onMessage(Message message) {
		String reciever;
		try {
			reciever = (String) message.getObjectProperty("recieverSessionID");
			
			// sending to everyone
			if(reciever == null) {
				Agent chatAgent = AgentCenter.CreateChatAgent();
				chatAgent.handleMessage(message);
			}
			// private message
			else {
				Agent userAgent = AgentCenter.agents.get(reciever);
				userAgent.handleMessage(message);
			}
		} catch (JMSException e) {
			e.printStackTrace();
		}
		
	}
}