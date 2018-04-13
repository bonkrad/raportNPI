package pl.radekbonk.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import pl.radekbonk.service.ProductClient;

@Configuration
public class ProductConfiguration {

	@Bean
	public Jaxb2Marshaller marshaller() {
		Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
		// this package must match the package in the <generatePackage> specified in
		// pom.xml
		marshaller.setContextPath("pl.radekbonk.wsdl");
		return marshaller;
	}

	@Bean
	public ProductClient productClient(Jaxb2Marshaller marshaller) {
		ProductClient client = new ProductClient();
		client.setDefaultUri("http://systemcorewcf:666/GetDetails.svc");
		client.setMarshaller(marshaller);
		client.setUnmarshaller(marshaller);
		return client;
	}
}
