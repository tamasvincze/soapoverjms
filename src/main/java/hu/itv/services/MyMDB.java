package hu.itv.services;

import javax.annotation.PostConstruct;
import javax.jms.BytesMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class MyMDB implements MessageListener {
	
	public static final Logger LOG = Logger.getLogger(MyMDB.class);
	
	@PostConstruct
	public void onInit(){
		LOG.info("======\nMDB started!\n======");
		System.out.println("------------------------------- START");
	}
	
	@Override
	public void onMessage(Message message) {
		LOG.info("-------\nStart\n-------");
		System.out.println("-------\nStart\n-------");
		try {
			LOG.info("BE");
			System.out.println("BE");
			if (message instanceof TextMessage || message instanceof BytesMessage) {

				String messageString = null;
				if (message instanceof TextMessage) {
					messageString = ((TextMessage) message).getText();
				} else if (message instanceof BytesMessage) {
					//TODO: "cannot find symbol" error for getBody method: jms api 1.1 <-> 2.0 collision 
//					messageString = new String(message.getBody(byte[].class));
				}
				LOG.info("message = " + messageString);
				System.out.println("message = " + messageString);
			}
		} catch (Exception e) {
			LOG.error("Error!", e);
			System.err.println("ERROR: " + e.getMessage());
		}
		
		System.out.println("Done");
		LOG.debug("Done");	
	}
}
