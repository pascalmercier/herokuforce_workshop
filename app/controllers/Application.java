package controllers;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import play.cache.Cache;
import play.libs.Crypto;
import play.mvc.Controller;
import play.mvc.results.Redirect;
import util.SfdcOAuth;
import util.SfdcUtil;
import util.gson.SfdcOAuthResponse;
import util.gson.SfdcRecord;


public class Application extends Controller {

    public static void index() {
    	SfdcOAuthResponse response = SfdcOAuth.retrieveSfdcAccessTokenWithCredentials();
    	
    	initCacheFromResponse(response, "sfdcInfo");
    	
    	// Load Merchandises
    	List<SfdcRecord> merchandises = SfdcUtil.getRecords(session.getId(), "select Id, IsDeleted, Name, CreatedDate, LastModifiedDate, SystemModstamp, Description__c, Price__c, Total_Inventory__c from Merchandise__c where IsDeleted = false and Total_Inventory__c > 0");
        render(merchandises);
    }
    
    public static void purchase() {
    	try {
			SfdcOAuth.retrieveVerificationCode();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
    }
    
    
    public static void makePurchase(String merchIDList, String merchQList)
    { //** HEY FRENCHIE-
    		//Ok so I set up the form to return to this method, and JQ is creating composite strings of Quantities and IDs, in order
    		//To provide you with what you need in config(). I can't get this to run 100% but it should give you a good position Monday.
    		//I added photos, the logic there is just ot look up the id. Good times.
    	/*String[] IDList = merchIDList.split(",");
		String[] QList = merchQList.split(",");
		Map<String,Object> idQMap = new HashMap<String,Object>();
		Integer x = 0;
		for(String s : IDList)
		{
			idQMap.put(IDList[x], QList[x]);
			x++;
		}
		//now have a map of ID,Quantity
    	*/
    }
    
    
    public static void config() {
    	SfdcOAuthResponse response = SfdcOAuth.retrieveSfdcAccessToken();
    	initCacheFromResponse(response, "userInfo");
    	
    	String invoiceStatementId = SfdcUtil.insertInvoiceStatement(session.getId(), "userInfo");
    	
    	
    	
    	
    	Map<String, Object> lineItem = new HashMap<String, Object>();
    	lineItem.put("Name", "");
    	lineItem.put("Invoice_Statement__c", invoiceStatementId);
    	lineItem.put("Merchandise__c", "");
    	lineItem.put("Unit_Price__c", "");
    	lineItem.put("Units_Sold__c", "");
    	
    	
    	//SfdcUtil.insertLineItems(lineItems, session.getId(), "user-Info");
    	throw new Redirect("/result");
    }
    
    public static void result() {
    	System.out.println("Query to get all lineItems related to the created Invoice Statement");
    	render();
    }
    
    private static void initCacheFromResponse(SfdcOAuthResponse response, String whichCache){
    	String expiration = "30mn";
    	Cache.set("instanceUrl", response.instanceUrl, expiration);
    	
    	Map<String, String> sfdcInfo = new HashMap<String, String>();
    	sfdcInfo.put("accessToken", Crypto.encryptAES(response.accessToken));
		if (response.refreshToken != null) {
    		// refresh token is only returned in the original login response, not future refresh requests
			sfdcInfo.put("refreshToken", Crypto.encryptAES(response.refreshToken));
		}		
		String userId = response.id.substring(response.id.lastIndexOf('/') + 1, response.id.length() - 3);
		sfdcInfo.put("userId", userId);
		sfdcInfo.put("signature", Crypto.encryptAES(response.signature));
		sfdcInfo.put("issuedAt", response.issuedAt);
		
		Cache.set(session.getId() + "-" + whichCache, sfdcInfo, expiration);
		
		System.out.println("Cache init for " + session.getId() + " on " + whichCache);
    }
    

    
    
}