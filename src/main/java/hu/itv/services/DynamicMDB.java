package hu.itv.services;

import itv.hu.sampleservice.EchoRequest;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;

import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.w3c.dom.Node;

/**
 * Dynamic message driven bean.
 * Can be initialized with the Container, with a properly setup MDBListInitializator list bean.
 * The dynamic MDB uses a fixed service class, as the source and target format for the incoming messages.
 * The messages will be converted with JAXBContext.
 * 
 * @see Container
 * @see JAXBContext
 * @author tamas.vincze
 *
 */
public class DynamicMDB implements MessageListener {
	
	public static final Logger LOG = Logger.getLogger(DynamicMDB.class.toString());
	
	private String defaultResponseQueue;
	private String serviceName;
	private JmsTemplate jmsTemplate;
	
	@PostConstruct
	public void onInit(){
		LOG.info("======\n Dynamic MDB started!\n======");
	}
	
	@Override
	public void onMessage(Message message) {
		LOG.info("-------\nonMessage dynamic MDB\n-------");
		try {
			LOG.info("BE on dynamic MDB");
			if (message instanceof TextMessage || message instanceof BytesMessage) {

				String messageString = null;
				if (message instanceof TextMessage) {
					messageString = ((TextMessage) message).getText();
				} else if (message instanceof BytesMessage) {
					messageString = new String(message.getBody(byte[].class));
				}
				LOG.info("message on dynamic MDB = " + messageString);
				
				//TODO unmarshall, get call the service's corresponding method (use java reflection), then marshall and reply
				Destination replyToQueue = message.getJMSReplyTo();
				processMessage(messageString, replyToQueue);
			}
		} catch (Exception e) {
			LOG.severe("Error on dynamic MDB!"+ e.getMessage());
			e.printStackTrace();
		}
		
		LOG.info("Done on dynamic MDB");	
	}

	private void processMessage(String messageString, Destination replyToQueue) {
		// TODO get the service type, call the service
		
//		Class<?> clazz = Class.forName(serviceName + "Impl");
		try {
			Object serviceInstance = Class.forName("hu.itv.services." + serviceName + "Impl").getConstructor().newInstance();
			LOG.info("o = " + serviceInstance);
			
//			SampleServiceImpl serviceImplementation = (SampleServiceImpl)serviceInstance;
//			String serviceResp = serviceImplementation.echoRequest("FROM MDB, YEEEY");
			
			try {
				JAXBContext context = JAXBContext.newInstance(EchoRequest.class);
				Unmarshaller unmarshaller = context.createUnmarshaller();
				
				
				LOG.info("------------SOAP cucc");
				SOAPMessage soapRequest = getSOAPMessageFromString(messageString);
				SOAPHeader requestHeader = soapRequest.getSOAPHeader();
//				Node firstNode = getFirstNode(requestHeader);
				LOG.info("------------SOAP cucc END");
				
				SOAPBody requestBody = soapRequest.getSOAPBody();
				Node firstNode = getFirstNode(requestBody);
				Object unmarshalledObject = unmarshaller.unmarshal(firstNode, EchoRequest.class);
				
				JAXBElement jbe = (JAXBElement)unmarshalledObject;
				System.out.println(jbe);
				System.out.println(jbe.getDeclaredType());
				System.out.println(jbe.getValue());
				System.out.println(jbe.getName());
//				StringReader reader = new StringReader(messageString);
//				Object unmarshalledObject = unmarshaller.unmarshal(reader);
				LOG.info("marshalled object: " + jbe);
				if(jbe.getValue() instanceof EchoRequest){
					LOG.info("Mûködik a fogat!!!!");
					LOG.info("marshalled object cast: " + (EchoRequest)jbe.getValue());
					LOG.info("marshalled object input: " + ((EchoRequest)jbe.getValue()).getInput());
				}

			} catch (JAXBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Method method = serviceInstance.getClass().getMethod("echoRequest", String.class);
			Object returnValue = method.invoke(serviceInstance, "parameter-value1");
			
			
			try {
				JAXBContext context = JAXBContext.newInstance(EchoRequest.class);
				Marshaller marsh = context.createMarshaller();
				
				StringWriter sw = new StringWriter();
				EchoRequest er = new EchoRequest();
				er.setInput("INPUT HERE LEL");
				marsh.marshal(er, sw);
				
				LOG.info("marshalled = " + sw.toString());
				
			} catch (JAXBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			LOG.info("Response = " + returnValue.toString());
			
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		sendResponse(messageString, replyToQueue);
	}

	private void sendResponse(final String responseMessageFromService, Destination replyToQueue) {
		
		
		Destination responseQueue = (replyToQueue!=null?replyToQueue:new ActiveMQQueue(defaultResponseQueue));
		
		// TODO 
		jmsTemplate.send(responseQueue, new MessageCreator() {
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
	
	public void setJmsTemplate(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}
	
	
	//TODO
	public static SOAPMessage getSOAPMessageFromString(String xml) throws Exception {
		LOG.info("TransportUtil - getSOAPMessageFromString - start");
	    MessageFactory factory = MessageFactory.newInstance();
	    SOAPMessage message = factory.createMessage(new MimeHeaders(), new ByteArrayInputStream(xml.getBytes(Charset.forName("UTF-8"))));
	    LOG.info("TransportUtil - getSOAPMessageFromString - stop");
	    return message;
	}

	public static Node getFirstNode(Node node){
		if(node == null || node.getFirstChild() == null) return null;
		if(node.getFirstChild().getNextSibling() != null){
			return node.getFirstChild().getNextSibling();
		} else {
			return node.getFirstChild();
		}
	}
}
