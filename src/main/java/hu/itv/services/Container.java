package hu.itv.services;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.jms.ConnectionFactory;

import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

/**
 * Container class to construct the dynamic message-driven beans.
 * The Container is using the MDBListInitializator list bean in the application context for creating the MDBs.
 * Each row in the list represents a MDB, which consist of three attributes:
 * <ul>
 * <li>service: the name of the service which will be called upon receiving a matching message.</li>
 * <li>inbound queue: the queue on which the MDB is going to listen for messages</li>
 * <li>outbound queue: the default response queue for the MDB. Can be overridden with the message's ReplyToQ attribute.</li>
 * </ul>
 * 
 * @author tamas.vincze
 *
 */
public class Container {

	private static Logger LOG = Logger.getLogger("Container");

	@Autowired
	private ApplicationContext appContext;
	
	private List<DefaultMessageListenerContainer> dmlcList;
	
	@Value("#{MDBListInitializator.toArray(new java.lang.String[0])}")
	private List<String> MDBInitConfig;

	@Value("${jms.amq.user}")
	private String userName;
	@Value("${jms.amq.password}")
	private String password;
	@Value("${jms.amq.url}")
	private String brokerUrl;
	
	@Autowired
	JmsTemplate jmsTemplate;
	
	@PreDestroy
	public void onExit() {
		LOG.info("Exiting container, destroying message listeners.");
		for(DefaultMessageListenerContainer dmlc : dmlcList) {
			LOG.info("Destroying listener with destination name: " + dmlc.getDestinationName());
			dmlc.stop();
			dmlc.destroy();
		}
	}
	
	@PostConstruct
	public void Init(){
		LOG.info("=== Size of MDB list: " + (MDBInitConfig!=null?MDBInitConfig.size():"NULL") + "===");
		
		dmlcList = new ArrayList<DefaultMessageListenerContainer>();
		
		if(MDBInitConfig!=null) {
			for(String listElement : MDBInitConfig) {
				String[] connectionElements = listElement.split(";");
				
				if(connectionElements.length == 3) {

					String serviceName = connectionElements[0];
					String inboundQueueName = connectionElements[1];
					String outboundQueueName = connectionElements[2];
					
					LOG.info("\tMDB: {" + serviceName + ", " + inboundQueueName + ", " + outboundQueueName + "}");
					
					ActiveMQQueue queueIn = new ActiveMQQueue(inboundQueueName);
					DynamicMDB newMDB = new DynamicMDB();
					newMDB.setService(serviceName);
					newMDB.setResponseQueue(outboundQueueName);
					newMDB.setJmsTemplate(jmsTemplate);
					
					ConnectionFactory connectionFactory = (ConnectionFactory) appContext.getBean("amqJmsQueueConnectionFactory");
					
					DefaultMessageListenerContainer DMLC = new DefaultMessageListenerContainer();
					DMLC.setConnectionFactory(connectionFactory);
					DMLC.setDestination(queueIn); 
					
					DMLC.setMessageListener(newMDB);
					
					LOG.info("DMLC created");
					
					DMLC.initialize();
					DMLC.start();
					
					dmlcList.add(DMLC);
					
					LOG.info("DMLC started");
				} else {
					LOG.severe("Bad MDB property format: " + listElement);
				}
			}
		}
	}
}
