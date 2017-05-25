package hu.itv.services;

import itv.hu.sampleservice.SampleService;

import java.util.logging.Logger;

import javax.annotation.PostConstruct;

import org.springframework.web.context.support.SpringBeanAutowiringSupport;

//we won't expose this interface as a SOAP over HTTP service
//@WebService(endpointInterface = "itv.hu.sampleservice.SampleService", serviceName = "SampleService")
public class SampleServiceImpl extends SpringBeanAutowiringSupport implements SampleService {

	private static Logger LOG = Logger.getLogger("SampleServiceImpl");
	
	@PostConstruct
	private void onInit(){
		LOG.info("SampleServiceImpl created.");
	}
	
	public String echoRequest(String input) {
		LOG.info("Entered echoRequest. Param is: ["+input+"]");
		return new StringBuffer(input).reverse().toString();
	}

}
