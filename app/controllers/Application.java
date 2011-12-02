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
		/*    	
    	try {
			SfdcOAuth.retrieveVerificationCodeWithCredentials();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		*/
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
    
    public static void config() {
    	SfdcOAuthResponse response = SfdcOAuth.retrieveSfdcAccessToken();
    	initCacheFromResponse(response, "userInfo");
    	
    	
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
    }
    

    
    
}