package hu.itv.services;

import java.util.List;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jms.connection.SingleConnectionFactory;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

//@Component
public class Container {

	private static Logger LOG = Logger.getLogger("Container");

	@Autowired
	private ApplicationContext appContext;
	
	@Value("#{MDBListInitializator.toArray(new java.lang.String[0])}")
	private List<String> MDBInitConfig;

	@Value("${jms.amq.user}")
	private String userName;
	@Value("${jms.amq.password}")
	private String password;
	@Value("${jms.amq.url}")
	private String brokerUrl;
	
	@PostConstruct
	public void Init(){
		System.out.println("===========");
		System.out.println("Size of MDB list: " + (MDBInitConfig!=null?MDBInitConfig.size():"NULL"));
		
//		AutowireCapableBeanFactory acbf = appContext.getAutowireCapableBeanFactory();
		
		ConfigurableListableBeanFactory beanFactory = ((ConfigurableApplicationContext) appContext).getBeanFactory();
//		beanFactory.registerSingleton(bean.getClass().getCanonicalName(), bean);
		
//		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext((DefaultListableBeanFactory) beanFactory);
		
		if(MDBInitConfig!=null)
			for(String listElement : MDBInitConfig) {
				String[] connectionElements = listElement.split(";");
				
				if(connectionElements.length == 3) {

					String serviceName = connectionElements[0];
					String inboundQueueName = connectionElements[1];
					String outboundQueueName = connectionElements[2];
					
					System.out.println("\t MDB: {" + serviceName + ", " + inboundQueueName + ", " + outboundQueueName + "}");
					
					//TODO: construct MDBs
					ActiveMQQueue queueIn = new ActiveMQQueue(inboundQueueName);
					DynamicMDB newMDB = new DynamicMDB();
					newMDB.setService(serviceName);
					newMDB.setResponseQueue(outboundQueueName);
					
//					ConnectionFactory connectionFactory = (ConnectionFactory) appContext.getBean("amqJmsQueueConnectionFactory");
					
					ConnectionFactory connectionFactory = (ConnectionFactory) appContext.getBean("amqJmsQueueConnectionFactory");
					ActiveMQConnectionFactory amqConnFact = new ActiveMQConnectionFactory(userName, password, brokerUrl);
					SingleConnectionFactory scf = new SingleConnectionFactory(new ActiveMQConnectionFactory(userName, password, brokerUrl));
					/*
					<bean id="amqConnectionFactory" class="org.apache.activemq.ActiveMQConnectionFactory">
					<property name="brokerURL" value="${jms.amq.url}" />
					<property name="userName" value="${jms.amq.user}"/>
					<property name="password" value="${jms.amq.password}" />
					</bean>
					
					<bean id="amqJmsQueueConnectionFactory" class="org.springframework.jms.connection.SingleConnectionFactory">
					    <property name="targetConnectionFactory" ref="amqConnectionFactory"/>
					</bean>
					*/
					
					DefaultMessageListenerContainer DMLC = new DefaultMessageListenerContainer();
					DMLC.setConnectionFactory(new SingleConnectionFactory(new ActiveMQConnectionFactory(userName, password, brokerUrl)));
					DMLC.setDestination(queueIn); 
//					DMLC.setAutoStartup(true);
					DMLC.setBeanName("jmsContainer-" + serviceName);
					
					
					try {
						System.out.println("DMLC in queue: " + queueIn.getQueueName());
						System.out.println("DMLC in queue: " + queueIn.getPhysicalName());
						System.out.println("DMLC in queue: " + queueIn.getQualifiedName());
						System.out.println("CF: " + "populated by new");
					} catch (JMSException e) {
						// TODO Auto-generated catch block
						System.err.println("ERROR IN queuename syserr");
						e.printStackTrace();
					}
					DMLC.setMessageListener(newMDB);
					
					System.out.println("DMLC created");
					
					DMLC.initialize();
					DMLC.start();
					
					
//					beanFactory.registerSingleton("myDMLC" + serviceName, DMLC);
//					beanFactory.registerSingleton("myDMLC" + serviceName, DMLC);
					
//					acbf.autowireBean(DMLC);
					
//					context.register(DMLC.getClass());
//					context.registerAlias("myBean" + serviceName, "customBean" + serviceName);
//					context.refresh();

					System.out.println("DMLC started");
					
					/**
					 * Contruct new MDB-s.
					 * Each one have different services, but that's handled by the MDBs internally.
					 * Each one listens on different queues, so for each MDB we need to instantiate one DefaultMessageListenerContainer
					 * 
					 * jmsContainer = new DefaultMessageListenerContainer()
					 * jmsContainer.setConnFact(beans.lookUp("amqJmsQueueConnectionFactory"));
					 * jmsContainer.setDestination(beans.lookUp(inboundQueueName));
					 * 
					 * NEW MDB construction
					 * 
					 * jmsContainer.setDestination(newMDB));
					 * 
					 */
					
				} else {
					System.err.println("Bad MDB property format!" + listElement);
					LOG.severe("Bad MDB property format: " + listElement);
				}
				
			}
		System.out.println("===========");

		System.out.println("beanCount = " + appContext.getBeanDefinitionCount());
		for(String beanName : appContext.getBeanDefinitionNames()){
			System.out.println("--> a3 beanName = " + beanName);
		}
		
//		context.refresh();
//		
//		
//		System.out.println("beanCount = " + context.getBeanDefinitionCount());
//		for(String beanName : context.getBeanDefinitionNames()){
//			System.out.println("--> a3 beanName = " + beanName);
//		}
		
	}
}
