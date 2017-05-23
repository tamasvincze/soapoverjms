package hu.itv.services;

import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;


//@Component
public class DynamicMDB extends SpringBeanAutowiringSupport implements MessageListener {
	
	public static final Logger LOG = Logger.getLogger(DynamicMDB.class.toString());
	
//	@Autowired
//	@Qualifier("InQueue")
//	private Destination destination;

	private String defaultResponseQueue;

	private String serviceName;
	
	//TODO: the jmsTemplate will send out 
	@Autowired
	private JmsTemplate jmsTemplate;
	
	@PostConstruct
	public void onInit(){
		LOG.info("======\n Dynamic MDB started!\n======");
	}
	
	@Override
	public void onMessage(Message message) {
		LOG.info("-------\nStart on dynamic MDB\n-------");
		try {
			LOG.info("BE on dynamic MDB");
			if (message instanceof TextMessage || message instanceof BytesMessage) {

				String messageString = null;
				if (message instanceof TextMessage) {
					messageString = ((TextMessage) message).getText();
				} else if (message instanceof BytesMessage) {
					//messageString = new String(message.getBody(byte[].class));
				}
				LOG.info("message on dynamic MDB = " + messageString);
				
				//here do something with the message
				processMessage(messageString);
			}
		} catch (Exception e) {
			LOG.severe("Error on dynamic MDB!"+ e.getMessage());
		}
		
		LOG.fine("Done on dynamic MDB");	
	}

	private void processMessage(String messageString) {
		// TODO get the service type, call the service
		Object responseMessageFromService = null;
		sendResponse(responseMessageFromService);
	}

	private void sendResponse(final Object responseMessageFromService) {
		// TODO 
		jmsTemplate.send(defaultResponseQueue, new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
            	
            	TextMessage testMessage = null;
            	testMessage = session.createTextMessage(responseMessageFromService.toString() + System.currentTimeMillis());
            	testMessage.setJMSCorrelationID("CorrelId" + System.currentTimeMillis());
            	
            	return testMessage;
            }});  
	}

	public void setService(String serviceName) {
		this.serviceName = serviceName;
	}

	public void setResponseQueue(String outboundQueueName) {
		this.defaultResponseQueue = outboundQueueName;
	}
}
