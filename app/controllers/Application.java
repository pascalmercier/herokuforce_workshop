package controllers;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
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
    
    public static String purchase() {
    	String authURL = null;
    	try {
			authURL = SfdcOAuth.retrieveVerificationCode();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    	
    	String[] ids = params.getAll("ids[]");
    	Map<String, List<String>> lineItems = new HashMap<String, List<String>>(); 
    	
    	for(String id : ids){
    		String price = params.get(id + "[price]");
    		String qty = params.get(id + "[quantity]");
    		String name = params.get(id + "[name]");
    		System.out.println("Product id " + id + ": price=" + price + " qty=" + qty + " name=" + name);
    		
    		List<String> lineItemParams = new ArrayList<String>();
    		lineItemParams.add(price);
    		lineItemParams.add(qty);
    		lineItemParams.add(name);
    		    		
    		lineItems.put(id, lineItemParams);
    	}
    	
    	System.out.println("** Save cart content in cache **");
    	Cache.set(session.getId() + "-cart", lineItems, "30mn");
    	
    	return authURL;		
    }
    
    public static void register() {
    	
    	String[] ids = params.getAll("ids[]");
    	Map<String, List<String>> lineItems = new HashMap<String, List<String>>(); 
    	
    	for(String id : ids){
    		String price = params.get(id + "[price]");
    		String qty = params.get(id + "[quantity]");
    		String name = params.get(id + "[name]");
    		System.out.println("Product id " + id + ": price=" + price + " qty=" + qty + " name=" + name);
    		
    		List<String> lineItemParams = new ArrayList<String>();
    		lineItemParams.add(price);
    		lineItemParams.add(qty);
    		lineItemParams.add(name);
    		    		
    		lineItems.put(id, lineItemParams);
    	}
    	
    	/*
    	System.out.println("** Save cart content in cache **");
    	Cache.set(session.getId() + "-cart", lineItems, "30mn");
    	*/
    	
    	System.out.println("** Insert invoiceStatement into Salesforce **");
    	String invoiceStatementId = SfdcUtil.insertInvoiceStatement(session.getId(), "sfdcInfo");
    	
    	Cache.set(session.getId() + "-invoice#", invoiceStatementId, "30mn");
    	
    	/*
    	System.out.println("** Retrieve cart content from cache **");
    	Map<String, List<String>> cachedLineItems = Cache.get(session.getId() + "-cart", Map.class);
    	*/
    	
    	for(String id : lineItems.keySet()){
    		String price = lineItems.get(id).get(0);
    		String qty = lineItems.get(id).get(1);
    		String name = lineItems.get(id).get(2);
    		
    		Map<String, Object> lineItem = new HashMap<String, Object>();
	    	//lineItem.put("Name", invoiceStatementId + "." + id);
	    	lineItem.put("Name", name);
	    	lineItem.put("Invoice_Statement__c", invoiceStatementId);
	    	lineItem.put("Merchandise__c", id);
	    	lineItem.put("Unit_Price__c", price);
	    	lineItem.put("Units_Sold__c", qty);
    	
	    	SfdcUtil.insertLineItems(lineItem, session.getId(), "sfdcInfo");
    	}
    }
    
    public static void config() {
    	SfdcOAuthResponse response = SfdcOAuth.retrieveSfdcAccessToken();
    	initCacheFromResponse(response, "userInfo");
    	
    	System.out.println("** Insert invoiceStatement into Salesforce **");
    	String invoiceStatementId = SfdcUtil.insertInvoiceStatement(session.getId(), "userInfo");
    	
    	Cache.set(session.getId() + "-invoice#", invoiceStatementId, "30mn");
    	
    	System.out.println("** Retrieve cart content from cache **");
    	Map<String, List<String>> cachedLineItems = Cache.get(session.getId() + "-cart", Map.class);
    	
    	for(String id : cachedLineItems.keySet()){
    		String price = cachedLineItems.get(id).get(0);
    		String qty = cachedLineItems.get(id).get(1);
    		String name = cachedLineItems.get(id).get(2);
    		
    		Map<String, Object> lineItem = new HashMap<String, Object>();
	    	//lineItem.put("Name", invoiceStatementId + "." + id);
	    	lineItem.put("Name", name);
	    	lineItem.put("Invoice_Statement__c", invoiceStatementId);
	    	lineItem.put("Merchandise__c", id);
	    	lineItem.put("Unit_Price__c", price);
	    	lineItem.put("Units_Sold__c", qty);
    	
	    	SfdcUtil.insertLineItems(lineItem, session.getId(), "userInfo");
    	}
    	
    	
    	throw new Redirect("/result?action=purchase");
    }
    
    public static void result() {
    	System.out.println("** Get all lineItems related to the newly created Invoice Statement **");
    	String invoiceStatementId = Cache.get(session.getId() + "-invoice#", String.class);
    	
    	String query = "Select l.Value__c, l.Units_Sold__c, l.Unit_Price__c, l.Name, l.Merchandise__r.Name, l.Id From Line_Item__c l where l.Invoice_Statement__c = '" + invoiceStatementId + "'";
    	List<SfdcRecord> lineItems = SfdcUtil.getRecords(session.getId(), query);
    	
    	query = "Select i.Invoice_Value__c From Invoice_Statement__c i where i.Id = '" + invoiceStatementId + "' limit 1";
    	List<SfdcRecord> invoiceStatement = SfdcUtil.getRecords(session.getId(), query);
    	String total = invoiceStatement.get(0).invoiceValue;
    	
    	System.out.println(total);
    	
    	String action = params.get("action");
    	if(action.compareTo("register") == 0){
    		action = "registered";
    	} else if(action.compareTo("purchase") == 0){
    		action = "purchased";
    	}
    	
    	render(lineItems, total, action);
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