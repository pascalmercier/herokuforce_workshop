package controllers;

import java.util.List;
import java.util.Map;

import play.cache.Cache;
import play.mvc.Controller;
import play.mvc.results.Redirect;
import util.SfdcUtil;
import util.gson.SfdcRecord;

public class Shop extends Controller {

	public static void index() {
    	Map<String, String> sfdcInfo = Cache.get(session.getId() + "-sfdcInfo", Map.class);
    	
    	if( sfdcInfo != null  && !sfdcInfo.get("accessToken").isEmpty() ) {
	    	// Load Merchandises
	    	List<SfdcRecord> merchandises = SfdcUtil.getRecords(session.getId(), "select Id, IsDeleted, Name, CreatedDate, LastModifiedDate, SystemModstamp, Description__c, Price__c, Total_Inventory__c from Merchandise__c where IsDeleted = false and Total_Inventory__c > 0");
	        render(merchandises);
    	} else {
    		System.out.println("No accessToken available. Redirect to /");
    		throw new Redirect("/");
    	}
    }
	
    public static void shop2() {
    	Map<String, String> sfdcInfo = Cache.get(session.getId() + "-sfdcInfo", Map.class);
    	
    	if( sfdcInfo != null  && !sfdcInfo.get("accessToken").isEmpty() ) {
	    	// Load Merchandises
	    	List<SfdcRecord> merchandises = SfdcUtil.getRecords(session.getId(), "select Id, IsDeleted, Name, CreatedDate, LastModifiedDate, SystemModstamp, Description__c, Price__c, Total_Inventory__c from Merchandise__c where IsDeleted = false and Total_Inventory__c > 0");
	        render(merchandises);
    	} else {
    		System.out.println("No accessToken available. Redirect to /");
    		throw new Redirect("/");
    	}
    }

}
