/*package pl.radekbonk.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import pl.radekbonk.mouser.*;

import java.util.List;

public class ComponentClient extends WebServiceGatewaySupport {

	private static final Logger log = LoggerFactory.getLogger(ComponentClient.class);

	public SearchByPartNumberResponse searchByPartNumber(String partNumber) {

		SearchByPartNumber request = new SearchByPartNumber();
		//ObjectFactory factory = new ObjectFactory();
		request.setMouserPartNumber(partNumber);
		log.info("Mouser - requesting details for " + partNumber);

		//SearchByPartNumberResponse response = (SearchByPartNumberResponse) getWebServiceTemplate().marshalSendAndReceive("http://api.mouser.com/service/searchapi.asmx", request, new SoapActionCallback("http://api.mouser.com/service/SearchByPartNumber"));
		SearchByPartNumberResponse response = (SearchByPartNumberResponse) getResultParts(partNumber);

		return response;
	}

	public List<MouserPart> getResultParts(String partNumber) {
		SearchByPartNumberResponse response = searchByPartNumber(partNumber);
		ResultParts resultParts = response.getSearchByPartNumberResult();
		ArrayOfMouserPart mouserPartArray= resultParts.getParts();
		List<MouserPart> mouserPartList= mouserPartArray.getMouserPart();
		return mouserPartList;
	}
}*/
