package util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import play.Logger;
import play.cache.Cache;
import play.libs.Crypto;
import play.libs.WS;
import play.libs.WS.WSRequest;
import util.gson.SfdcRecord;
import util.gson.SfdcRestResponse;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializer;

public class SfdcUtil {
	private final static String QUERY_REST_ENDPOINT = "/services/data/v22.0/query/";
    private final static String INVOICE_STATEMENT_REST_ENDPOINT = "/services/data/v22.0/sobjects/Invoice_Statement__c/";
    private final static String LINE_ITEM_REST_ENDPOINT = "/services/data/v22.0/sobjects/Line_Item__c/";
	
	public static List<SfdcRecord> getRecords(String sessionId, String query) {	
		// Get instanceUrl and userInfo from cache
		String instanceUrl = Cache.get("instanceUrl", String.class);
		Map<String, String> sfdcInfo = Cache.get(sessionId + "-sfdcInfo", Map.class);
		
		WSRequest request = WS.url(instanceUrl + SfdcUtil.QUERY_REST_ENDPOINT);
		setToken(request, Crypto.decryptAES(sfdcInfo.get("accessToken")));
		request.parameters.put("q", query);
		JsonElement jsonElement = request.get().getJson();
		Logger.info("Read from SFDC. Query: '%s' Result: %s", query, jsonElement);
		SfdcRestResponse restResponse = new Gson().fromJson(jsonElement, SfdcRestResponse.class);
		return restResponse.records;
	}
	
	public static String insertInvoiceStatement(String sessionId, String whichCache) {	
		// Get instanceUrl and userInfo from cache
		String instanceUrl = Cache.get("instanceUrl", String.class);
		Map<String, String> userInfo = Cache.get(sessionId + "-" + whichCache, Map.class);
		
		WSRequest request = WS.url(instanceUrl + SfdcUtil.INVOICE_STATEMENT_REST_ENDPOINT);
		setToken(request, Crypto.decryptAES(userInfo.get("accessToken")));
		request.headers.put("Content-Type", "application/json");
		
		// Create Map serialize it in Json and push into body
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("Description__c", "");
		data.put("Status__c", "Open");
		Gson gson = new Gson();
		String jsonData = gson.toJson(data);
		System.out.println("Json Data: " + jsonData);
		request.body(jsonData);
		
		JsonElement jsonElement = request.post().getJson();
		Logger.info("Result from SFDC: %s", jsonElement);

		return jsonElement.getAsJsonObject().get("id").getAsString();
	}
	
	public static void insertLineItems(Map<String, Object> lineItems, String sessionId, String whichCache) {
		// Get instanceUrl and userInfo from cache
		String instanceUrl = Cache.get("instanceUrl", String.class);
		Map<String, String> userInfo = Cache.get(sessionId + "-" + whichCache, Map.class);
		
		WSRequest request = WS.url(instanceUrl + SfdcUtil.LINE_ITEM_REST_ENDPOINT);
		setToken(request, Crypto.decryptAES(userInfo.get("accessToken")));
		request.headers.put("Content-Type", "application/json");
		
		// Serialize lineItems into a json string and push it into the body
		Gson gson = new Gson();
		String jsonData = gson.toJson(lineItems);
		System.out.println("Json Data: " + jsonData);
		request.body(jsonData);
		
		JsonElement jsonElement = request.post().getJson();
		Logger.info("Result from SFDC: %s", jsonElement);
	}
	
	public static void setToken(WSRequest request, String accessToken) {
        request.headers.put("Authorization", "OAuth " + accessToken);
    }
}
