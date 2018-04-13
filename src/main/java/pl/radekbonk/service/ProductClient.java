package pl.radekbonk.service;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.core.SoapActionCallback;
import pl.radekbonk.wsdl.GetItemIdDetailsJson;
import pl.radekbonk.wsdl.GetItemIdDetailsJsonResponse;
import pl.radekbonk.wsdl.ObjectFactory;

import javax.xml.bind.JAXBElement;

public class ProductClient extends WebServiceGatewaySupport {

	private static final Logger log = LoggerFactory.getLogger(ProductClient.class);

	private GetItemIdDetailsJsonResponse getItemIdDetailsJson(String id) {
		id = checkId(id);

//		GetProdIdDetailsXml request = new GetProdIdDetailsXml();
		GetItemIdDetailsJson request = new GetItemIdDetailsJson();
		ObjectFactory factory = new ObjectFactory();
		//JAXBElement<String> createGetProdIdDetailsXmlPRODID = factory.createGetProdIdDetailsXmlPRODID(id);
		JAXBElement<String> createGetItemIdDetailsJsonItemId = factory.createGetItemIdDetailsJsonItemId(id);
		//request.setPRODID(createGetProdIdDetailsXmlPRODID);
		request.setItemId(createGetItemIdDetailsJsonItemId);

		log.info("Requesting details for " + id);

		//GetProdIdDetailsXmlResponse response = (GetProdIdDetailsXmlResponse) getWebServiceTemplate().marshalSendAndReceive("http://systemcorewcf:666/GetDetails.svc", request, new SoapActionCallback("http://tempuri.org/IGetProdIdDetails/GetProdIdDetailsXml"));

		GetItemIdDetailsJsonResponse response = (GetItemIdDetailsJsonResponse) getWebServiceTemplate().marshalSendAndReceive("http://systemcorewcf:666/GetDetails.svc", request, new SoapActionCallback("http://tempuri.org/IGetItemIdDetails/GetItemIdDetailsJson"));

		return response;
	}

	public String getName(String id) {
		GetItemIdDetailsJsonResponse response = getItemIdDetailsJson(id);
		JAXBElement<String> object = response.getGetItemIdDetailsJsonResult();
		//System.err.println(response.getGetItemIdDetailsJsonResult());
		//System.out.println(object.getDeclaredType());
		//System.out.println(object.getValue());
		//System.out.println(object.getName());
		//System.out.println(object.getValue());
		try {
			JSONObject jsonObject = new JSONObject(object.getValue());
		//System.out.println(jsonObject.get("ITEMNAME"));
			return jsonObject.get("ITEMNAME").toString();
		} catch (JSONException e) {
			return "Error";
		}
	}

	public String checkId(String id) {
		int intId = Integer.valueOf(id);
		if (intId < 10) {
			return "0000" + id;
		} else if (intId < 100) {
			return "000" + id;
		} else if (intId < 1000) {
			return "00" + id;
		} else if (intId < 10000) {
			return "0" + id;
		}
		else {
			return id;
		}
	}
}
