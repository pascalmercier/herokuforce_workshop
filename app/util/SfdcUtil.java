package util;

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
	
	public static void setToken(WSRequest request, String accessToken) {
        request.headers.put("Authorization", "OAuth " + accessToken);
    }
}
