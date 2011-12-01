package controllers;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import play.Logger;
import play.libs.WS;
import play.libs.WS.HttpResponse;
import play.libs.WS.WSRequest;
import play.mvc.Controller;
import play.mvc.Scope.Params;
import play.mvc.results.Redirect;


public class Application extends Controller {

	private static String environment = "https://login.salesforce.com/services/oauth2/authorize";
	private static String consumerKey = "3MVG9PhR6g6B7ps6wbfHR.lgxq7aTdUaosLfLVpJiUMLc5C3M82GcUfYrNZxdpURX6lGMGJ0xO1HIT9PLxeLz";
	private static String consumerSecret = "4310885807798930562";
	private static String redirectUri = "https://localhost:9090/config";
	private static String accessTokenURL = "https://login.salesforce.com/services/oauth2/token";
	
    public static void index() {
		try {
			retrieveVerificationCode();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public static void config() {
    	retrieveSfdcAccessToken(redirectUri);
    	throw new Redirect("/shop");
    }
    
    private static void retrieveVerificationCode() throws UnsupportedEncodingException{
    	String urlStr = environment + "?" + 
						"response_type=code" + 
						"&client_id=" + consumerKey + 
						"&client_secret=" + consumerSecret + 
						"&redirect_uri=" + URLEncoder.encode(redirectUri, "UTF-8") + 
				        "&display=touch";
		
    	System.out.println("** Init OAuth authorization with Salesforce **");
    	System.out.println(urlStr);
    	
    	throw new Redirect(urlStr);
    	
    	/*
		URL url = new URL(urlStr); 
		HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
		connection.setRequestMethod("POST"); 
		
		System.out.println("Resp Code:"+connection.getResponseCode()); 
		System.out.println("Resp Message:"+ connection.getResponseMessage()); 
		
		System.out.println("****** Content of the URL ********");			
		   BufferedReader br = 
			new BufferedReader(
				new InputStreamReader(connection.getInputStream()));
	 
		   String input;
	 
		   while ((input = br.readLine()) != null){
		      System.out.println(input);
		   }
		   br.close();
		
    	return connection.getResponseMessage();
    	*/
    }
    
    private static void retrieveSfdcAccessToken(String callbackURL) {
    	System.out.println("** Retrieve access token **");
    	String accessCode = Params.current().get("code");
        System.out.println("My Code :: " + accessCode);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("client_id", consumerKey);
        params.put("client_secret", consumerSecret);
        params.put("redirect_uri", callbackURL);
        params.put("code", accessCode);
        params.put("grant_type", "authorization_code");
        WSRequest req = WS.url(accessTokenURL).params(params);
        System.out.println(" >>>>>>>>>>>>>>>>" + req.url);
        HttpResponse response = WS.url(accessTokenURL).params(params).post();
        if (!response.success()) {
        	Logger.error("Error when trying to get access code: " + response.getString());
        }
        System.out.println("#######################");
        System.out.println("# " + response.getString());
        System.out.println("#######################");
    }
}