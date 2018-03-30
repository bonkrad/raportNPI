/*package pl.radekbonk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.core.SoapActionCallback;
import pl.radekbonk.wsdl.*;

import javax.xml.bind.JAXBElement;

public class ProductClient extends WebServiceGatewaySupport {

	private static final Logger log = LoggerFactory.getLogger(ProductClient.class);

	public GetProdIdDetailsXmlResponse getProdIdDetailsXml(String id) {

		GetProdIdDetailsXml request = new GetProdIdDetailsXml();
		ObjectFactory factory = new ObjectFactory();
		JAXBElement<String> createGetProdIdDetailsXmlPRODID = factory.createGetProdIdDetailsXmlPRODID(id);
		request.setPRODID(createGetProdIdDetailsXmlPRODID);

		log.info("Requesting details for " + id);

		GetProdIdDetailsXmlResponse response = (GetProdIdDetailsXmlResponse) getWebServiceTemplate().marshalSendAndReceive("http://systemcorewcf:666/GetDetails.svc", request, new SoapActionCallback("http://tempuri.org/IGetProdIdDetails/GetProdIdDetailsXml"));

		return response;
	}
}*/
