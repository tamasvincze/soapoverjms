package hu.itv.services;

import itv.hu.sampleservice.SampleService;

import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jws.WebService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

//we won't expose this interface as a SOAP over HTTP service
//@WebService(endpointInterface = "itv.hu.sampleservice.SampleService", serviceName = "SampleService")
public class SampleServiceImpl extends SpringBeanAutowiringSupport implements SampleService {

	private static Logger LOG = Logger.getLogger("SampleServiceImpl");
	
	
	//TODO: remove, only for testing purposes
	@Autowired
	private JmsTemplate jmsTemplate;
	@Autowired
	@Qualifier("OutQueue")
	private Destination outQ;
	
	@PostConstruct
	private void onInit(){
		LOG.info("SampleServiceImpl created.");

		//TODO: remove, only for testing purposes
		//test if jms message producing is working
		jmsTemplate.send(outQ, new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
            	
            	TextMessage testMessage = null;
            	testMessage = session.createTextMessage("New Message here, " + System.currentTimeMillis());
            	testMessage.setJMSCorrelationID("CorrelId" + System.currentTimeMillis());
            	
            	return testMessage;
            }});  
	}
	
	public String echoRequest(String input) {
		LOG.info("Entered echoRequest. Param is: ["+input+"]");
		return new StringBuffer(input).reverse().toString();
	}

}
