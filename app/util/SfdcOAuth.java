package util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import play.Logger;
import play.Play;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.mvc.Scope.Params;
import play.mvc.results.Redirect;
import util.gson.SfdcOAuthResponse;

import com.google.gson.Gson;

public class SfdcOAuth {
	private final static String environment = Play.configuration.getProperty("sfdc.environment");
	private final static String accessTokenURL = Play.configuration.getProperty("sfdc.accessTokenURL");
	private final static String consumerKey = Play.configuration.getProperty("sfdc.consumerKey");
	private final static String consumerSecret = Play.configuration.getProperty("sfdc.consumerSecret");
	private final static String redirectUri = Play.configuration.getProperty("sfdc.redirectUri");
	
	// Credentials for public access
	private final static String username = Play.configuration.getProperty("sfdc.public.username");
	private final static String password = Play.configuration.getProperty("sfdc.public.password");	
	
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
	
	public static SfdcOAuthResponse retrieveSfdcAccessTokenWithCredentials() {
    	System.out.println("** Retrieve access token with credentials **");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("client_id", consumerKey);
        params.put("client_secret", consumerSecret);
        params.put("redirect_uri", redirectUri);
        params.put("grant_type", "password");
        params.put("username", username);
        params.put("password", password);
        
        HttpResponse response = WS.url(accessTokenURL).params(params).post();
        if (!response.success()) {
        	Logger.error("Error when trying to get access code: " + response.getString());
        }
        System.out.println("Response: " + response.getString());
        Gson gson = new Gson();
        return gson.fromJson(response.getJson(), SfdcOAuthResponse.class);
    }
    
    public static String retrieveVerificationCode() throws UnsupportedEncodingException{
    	String urlStr = environment + "?" + 
						"response_type=code" + 
						"&client_id=" + consumerKey + 
						"&client_secret=" + consumerSecret + 
						"&redirect_uri=" + URLEncoder.encode(redirectUri, "UTF-8") + 
				        "&display=touch";
		
    	System.out.println("** Init OAuth authorization with Salesforce **");
    	System.out.println(urlStr);
    	
    	return urlStr;
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

}
