package util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import play.Logger;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.mvc.Scope.Params;
import play.mvc.results.Redirect;
import util.gson.SfdcOAuthResponse;

import com.google.gson.Gson;

public class SfdcOAuth {
	private final static String environment = "https://login.salesforce.com/services/oauth2/authorize";
	private final static String accessTokenURL = "https://login.salesforce.com/services/oauth2/token";
	private final static String consumerKey = "3MVG9PhR6g6B7ps6wbfHR.lgxq7aTdUaosLfLVpJiUMLc5C3M82GcUfYrNZxdpURX6lGMGJ0xO1HIT9PLxeLz";
	private final static String consumerSecret = "4310885807798930562";
	private final static String redirectUri = "https://localhost:9090/config";
	
	
	public static SfdcOAuthResponse retrieveSfdcAccessToken() {
    	System.out.println("** Retrieve access token **");
    	String accessCode = Params.current().get("code");
        System.out.println("Access code: " + accessCode);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("client_id", consumerKey);
        params.put("client_secret", consumerSecret);
        params.put("redirect_uri", redirectUri);
        params.put("code", accessCode);
        params.put("grant_type", "authorization_code");
        HttpResponse response = WS.url(accessTokenURL).params(params).post();
        if (!response.success()) {
        	Logger.error("Error when trying to get access code: " + response.getString());
        }
        System.out.println("Response: " + response.getString());
        Gson gson = new Gson();
        return gson.fromJson(response.getJson(), SfdcOAuthResponse.class);
    }
    
    public SfdcOAuthResponse refreshSfdcAccessToken(String refreshToken) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("client_id", consumerKey);
        params.put("client_secret", consumerSecret);
        params.put("grant_type", "refresh_token");
        params.put("refresh_token", refreshToken);
        HttpResponse response = WS.url(accessTokenURL).params(params).post();
        if (!response.success()) {
        	
        	System.out.println("XXXXXXXXXXXXXX  " + response.getJson().getAsString());
        	throw new IllegalStateException("Error when trying to get access code: " + response.getJson().getAsString());
        }
        System.out.println("XXXXXXXXXXXXXX  " + response.getJson().getAsString());
        Gson gson = new Gson();
        return gson.fromJson(response.getJson(), SfdcOAuthResponse.class);
    }
    
    public static void retrieveVerificationCode() throws UnsupportedEncodingException{
    	String urlStr = environment + "?" + 
						"response_type=code" + 
						"&client_id=" + consumerKey + 
						"&client_secret=" + consumerSecret + 
						"&redirect_uri=" + URLEncoder.encode(redirectUri, "UTF-8") + 
				        "&display=touch";
		
    	System.out.println("** Init OAuth authorization with Salesforce **");
    	System.out.println(urlStr);
    	
    	throw new Redirect(urlStr);
    }

}
