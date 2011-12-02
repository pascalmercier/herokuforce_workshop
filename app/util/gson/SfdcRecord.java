package util.gson;

import java.util.List;


import com.google.gson.annotations.SerializedName;

/**
 * @author marcus (extended by Pascal Mercier)
 */
public class SfdcRecord {
	
	public SfdcAttributes attributes;
	
	@SerializedName("Id")
	public String id;
	
	/*
	 * User fields
	 */
	@SerializedName("Username")
	public String username;
	
	@SerializedName("Name")
	public String name;
	
	/*
	 * Merchandise fields
	 */
	@SerializedName("IsDeleted")
	public String isDeleted;
	
	@SerializedName("CreatedDate")
	public String createdDate;
	
	@SerializedName("LastModifiedDate")
	public String lastModifiedDate;
	
	@SerializedName("SystemModstamp")
	public String systemModstamp;
	
	@SerializedName("Description__c")
	public String desc;
	
	@SerializedName("Price__c")
	public String price;
	
	@SerializedName("Total_Inventory__c")
	public String totalInventory;	
	
	/*
	 * Lead fields
	 */
	@SerializedName("FirstName")
	public String firstName;
	
	@SerializedName("LastName")
	public String lastName;
	
	@SerializedName("Description")
	public String description;
	
	@SerializedName("Company")
	public String company;
	
	@SerializedName("Website")
	public String website;
	
	@SerializedName("Status")
	public String status;
	
	@SerializedName("OwnerId")
	public String ownerId;
	
	@SerializedName("LastModifiedDate")
	public String lastActivityDate;
	
}
