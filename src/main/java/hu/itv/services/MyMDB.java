package hu.itv.services;

import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.jms.BytesMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

//@Component
public class MyMDB implements MessageListener {
	
	public static final Logger LOG = Logger.getLogger(MyMDB.class.toString());
	
	@PostConstruct
	public void onInit(){
		LOG.info("======MDB started!======");
	}
	
	@Override
	public void onMessage(Message message) {
		LOG.info("-------Start-------");
		try {
			LOG.info("BE");
			if (message instanceof TextMessage || message instanceof BytesMessage) {

				String messageString = null;
				if (message instanceof TextMessage) {
					messageString = ((TextMessage) message).getText();
				} else if (message instanceof BytesMessage) {
					//TODO: "cannot find symbol" error for getBody method: jms api 1.1 <-> 2.0 collision 
//					messageString = new String(message.getBody(byte[].class));
				}
				LOG.info("message = " + messageString);
			}
		} catch (Exception e) {
			LOG.severe("Error! " + e.getMessage());
		}
		
		LOG.fine("Done");	
	}
}
