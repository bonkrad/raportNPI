/*package pl.radekbonk.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import pl.radekbonk.service.ComponentClient;

@Configuration
public class ComponentConfiguration {

	@Bean
	public Jaxb2Marshaller marshaller() {
		Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
		// this package must match the package in the <generatePackage> specified in
		// pom.xml
		marshaller.setPackagesToScan("pl.radekbonk.mouser");
		return marshaller;
	}

	@Bean
	public ComponentClient componentClient(Jaxb2Marshaller marshaller) {
		ComponentClient client = new ComponentClient();
		client.setDefaultUri("http://api.mouser.com/service/searchapi.asmx");
		client.setMarshaller(marshaller);
		client.setUnmarshaller(marshaller);
		return client;
	}
}*/
