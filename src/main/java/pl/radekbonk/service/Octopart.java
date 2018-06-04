package pl.radekbonk.service;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class Octopart {

	static private Octopart theInstance = null;
	final static private String OCTOPART_URL = "http://octopart.com/api/v3/parts/match";
	static private String API_KEY = "EXAMPLE_KEY";

	private Octopart() {
		/* Singleton */
	}

	public static Octopart getInstance() {
		if (theInstance == null) {
			theInstance = new Octopart();
		}
		return theInstance;
	}

	public static Octopart getInstance(String apiKey) {
		setApiKey(apiKey);
		return getInstance();
	}

	public static void setApiKey(String apiKey) {
		API_KEY = apiKey;
	}

	public JSONObject searchParts(String query) throws IOException,
			ParseException {

		JSONObject results = null;

		query = replaceBlanks(query);

		String paramString = "?queries=%5B{\"mpn\":\"" + query + "\"}%5D&apikey=" + API_KEY + "&include%5B%5D=specs";

		URL url = new URL(OCTOPART_URL + paramString);

		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Accept", "application/json");

		if (conn.getResponseCode() == 400) {
			/*
			 * This means BAD_REQUEST -- our search query was malformed.
			 */
		}

		if (conn.getResponseCode() == 401) {
			/*
			 * UNAUTHORIZED -- the API key was invalid.
			 */
		}

		if (conn.getResponseCode() != 200) {
			throw new RuntimeException("Octopart returned HTTP error code: "
					+ conn.getResponseCode() + " " + conn.getResponseMessage());
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(
				(conn.getInputStream())));

		StringBuilder output = new StringBuilder();
		String line = "";

		while ((line = br.readLine()) != null) {
			output.append(line);
		}

		conn.disconnect();
		results = getPartsFromJSONData(output.toString());

		return results;
	}

	private String replaceBlanks(String query) {
		return query.replaceAll("\\s+", "+");
	}

	private JSONObject getPartsFromJSONData(String jsonResponse)
			throws ParseException {

		List<Part> parts = new ArrayList<Part>();

		/*JSONParser parser = new JSONParser(jsonResponse);
		Object obj = parser.parse(jsonResponse);*/
		JSONObject jsonObject = new JSONObject(jsonResponse);

		return jsonObject;
	}
}
