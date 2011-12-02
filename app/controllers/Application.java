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
		try {
			SfdcOAuth.retrieveVerificationCode();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public static void config() {
    	SfdcOAuthResponse response = SfdcOAuth.retrieveSfdcAccessToken();
    	
    	initCacheFromResponse(response);		
		
    	throw new Redirect("/shop");
    }
    
    private static void initCacheFromResponse(SfdcOAuthResponse response){
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
		
		Cache.set(session.getId() + "-sfdcInfo", sfdcInfo, expiration);		
    }
    

    
    
}