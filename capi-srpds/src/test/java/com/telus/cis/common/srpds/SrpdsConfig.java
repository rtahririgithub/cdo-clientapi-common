
package com.telus.cis.common.srpds;


import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.remoting.jaxws.JaxWsPortProxyFactoryBean;

import com.telus.cis.common.srpds.dao.ServiceRequestInformationServiceDao;
import com.telus.cis.wsclient.ServiceRequestInfoServicePort;

@Configuration
public class SrpdsConfig {

	@Bean
	public JaxWsPortProxyFactoryBean serviceRequestInfoServicePortFactory() throws IOException {

		JaxWsPortProxyFactoryBean factory = new JaxWsPortProxyFactoryBean();
		Resource wsdlResource = new ClassPathResource("wsdls/ServiceRequestInfoService_v5_4.wsdl");
		factory.setWsdlDocumentResource(wsdlResource);
		factory.setServiceInterface(ServiceRequestInfoServicePort.class);
		factory.setEndpointAddress("https://apigw-soap-st.tsl.telus.com/soap/pt148/CMO/OrderMgmt/ServiceRequestInfoService_v5_4_RP");
		factory.setUsername("ClientAPI_EJB");
		factory.setPassword("soaorgid");
		factory.setServiceName("ServiceRequestInfoService_v5_4");
		factory.setPortName("ServiceRequestInfoServicePort");
		factory.setNamespaceUri("http://telus.com/wsdl/CMO/OrderMgmt/ServiceRequestInfoService_5");
		return factory;
	}
	
	
	@Bean
	public ServiceRequestInfoServicePort serviceRequestInfoServicePort() throws IOException {
		return (ServiceRequestInfoServicePort) serviceRequestInfoServicePortFactory().getObject();
	}

	
	@Bean
	public ServiceRequestInformationServiceDao serviceRequestInformationServiceDao() throws IOException {
		return new ServiceRequestInformationServiceDao(serviceRequestInfoServicePort());
	}
	 
}
