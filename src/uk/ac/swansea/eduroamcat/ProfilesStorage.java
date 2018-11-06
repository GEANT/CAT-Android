//*******************
//Author: Gareth Ayres <g.j.ayres@swansea.ac.uk>
//SENSE Project
//https://github.com/GEANT/CAT-Android
//*******************
package uk.ac.swansea.eduroamcat;

import java.io.UnsupportedEncodingException;
import java.security.KeyStoreException;
import java.util.ArrayList;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ProfilesStorage extends SQLiteOpenHelper {
	  
	  private static final String DATABASE_NAME = "eduroamProfiles.db";
	  private static final int DATABASE_VERSION = 2;
	  
	  //EAP CONFIG TABLE
	  public static final String TABLE_NAME_EAP = "eapProfiles";
	  public static final String COLUMN_ID_EAP = "id_eap";
	  public static final String COLUMN_COMMENT = "comment";
	  
	  //AUTH CONFIG TABLE
	  public static final String TABLE_NAME_AUTH = "authProfiles";
	  public static final String COLUMN_ID_AUTH = "id_auth";
	  public static final String COLUMN_COMMENT_AUTH = "comment";

	  //WIFI CONFIG TABLE
	  public static final String TABLE_NAME_WIFI = "wifiProfiles";
	  public static final String COLUMN_ID_WIFI = "id_wifi";
	  public static final String COLUMN_COMMENT2 = "comment";
	  
	  //USER TABLE
	  public static final String TABLE_NAME_USER = "userProfiles";
	  public static final String COLUMN_ID_USER = "id_user";

	  // Database creation sql statement
	  private static final String DATABASE_CREATE_EAP = "create table IF NOT EXISTS "
			  + TABLE_NAME_EAP + "(" 
			  + COLUMN_ID_EAP
			  + " integer primary key autoincrement, " 
			  + "EAPIdP_ID"
			  + " text, " 
			  + "displayName"
			  + " text, " 	
			  + "displayNameLang"
			  + " text, " 
			  + "description"
			  + " text, " 
			  + "logo"
			  + " text, "
			  + "terms"
			  + " text, " 
			  + "emails"
			  + " text, " 
			  + "phone"
			  + " text, "
			  + "web"
			  + " text" 
			  + ");";	  

	  private static final String DATABASE_CREATE_AUTH = "create table IF NOT EXISTS "
			  + TABLE_NAME_AUTH + "(" 
			  + COLUMN_ID_AUTH
			  + " integer primary key autoincrement, "
			  + COLUMN_ID_EAP
			  + " integer, " 
			  + "outterEAPType"
			  + " integer,"
			  + "innerEAPType"
			  + " integer,"
			  + "innerNonEAPType"
			  + " integer,"
			  + "CAencoding"
			  + " text,"
			  + "CAformat"
			  + " text,"
			  + "serverIDs"
			  + " text,"
			  + "clientCert"
			  + " text,"
			  + "clientCertEncoding"
			  + " text,"
			  + "clientCertFormat"
			  + " text,"
			  + "anonID"
			  + " text,"
			  + "anonID_save"
			  + " integer,"
			  + "CAcert"
			  + " text,"
			  + "clientCertPass"
			  + " text"
			  + ");";	

	  private static final String DATABASE_CREATE_WIFI = "create table IF NOT EXISTS "
			  + TABLE_NAME_WIFI + "(" 
			  + COLUMN_ID_WIFI
			  + " integer primary key autoincrement, " 
			  + COLUMN_ID_EAP
			  + " integer, " 
			  + "ssid"
			  + " text,"
			  + "authType"
			  + " text,"
			  + "encType"
			  + " text,"			  
			  + "ssidPriority"
			  + " integer,"	
			  + "autoJoin"
			  + " integer"
			  + ");";
	  
	  private static final String DATABASE_CREATE_USER = "create table IF NOT EXISTS "
			  + TABLE_NAME_USER + "(" 
			  + COLUMN_ID_USER
			  + " integer primary key autoincrement, " 
			  + "username"
			  + " text"
			  + ");";

	  public ProfilesStorage(Context context) {
	    super(context, DATABASE_NAME, null, DATABASE_VERSION);
	  }

	  @Override
	  public void onCreate(SQLiteDatabase database) {
	    database.execSQL(DATABASE_CREATE_EAP);
	    database.execSQL(DATABASE_CREATE_AUTH);
	    database.execSQL(DATABASE_CREATE_WIFI);
	    database.execSQL(DATABASE_CREATE_USER);
	    eduroamCAT.debug("Created DBs");
	  }
	  
	   public long insertWiFi (String ID_EAP, String ssid, String authType, String encType, int ssidPriority, int autoJoin)
	   {
	      SQLiteDatabase db = this.getWritableDatabase();
	      ContentValues contentValues = new ContentValues();
	      contentValues.put("ID_EAP", ID_EAP);
	      contentValues.put("ssid", ssid);
	      contentValues.put("authType", authType);	
	      contentValues.put("encType", encType);
	      contentValues.put("ssidPriority", ssidPriority);
	      contentValues.put("autoJoin", autoJoin);
	      long result = db.insert(TABLE_NAME_WIFI, null, contentValues);
	      db.close();
	      return result;
	   }
	   
	   public long insertEAP (String EAPIdP_ID, String displayName, String displayNameLang, String description, String logo, String terms, String emails,
			   String phone, String web)
	   {
	      SQLiteDatabase db = this.getWritableDatabase();
	      ContentValues contentValues = new ContentValues();
	      contentValues.put("EAPIdP_ID", EAPIdP_ID);
	      contentValues.put("displayName", displayName);
	      contentValues.put("displayNameLang", displayNameLang);	
	      contentValues.put("description", description);
	      contentValues.put("logo", logo);
	      contentValues.put("terms", terms);
	      contentValues.put("emails", emails);
	      contentValues.put("phone", phone);
	      contentValues.put("web", web);
	      long result = db.insert(TABLE_NAME_EAP, null, contentValues);
	      db.close();
	      return result;
	   }
	   
	   public long insertAuth (String ID_EAP, int outterEAPType, int innerEAPType, int innerNonEAPType, String CAencoding, String CAformat,
			   String serverIDs, String clientCert, String clientCertEncoding, String clientCertFormat, String clientCertPass, String anonID, int anonID_save, String CAcert)
	   {
	      SQLiteDatabase db = this.getWritableDatabase();
	      ContentValues contentValues = new ContentValues();
	      contentValues.put("ID_EAP", ID_EAP);
	      contentValues.put("outterEAPType", outterEAPType);
	      contentValues.put("innerEAPType", innerEAPType);	
	      contentValues.put("innerNonEAPType", innerNonEAPType);
	      contentValues.put("CAencoding", CAencoding);
	      contentValues.put("CAformat", CAformat);
	      contentValues.put("serverIDs", serverIDs);	
	      contentValues.put("clientCert", clientCert);
	      contentValues.put("clientCertEncoding", clientCertEncoding);
	      contentValues.put("clientCertFormat", clientCertFormat);
	      contentValues.put("anonID", anonID);
	      contentValues.put("anonID_save", anonID_save);
	      contentValues.put("CAcert", CAcert);
		  contentValues.put("clientCertPass", clientCertPass);
	      long result = db.insert(TABLE_NAME_AUTH, null, contentValues);
	      db.close();
	      return result;
	   }
	   
	   public long insertUSER (String username)
	   {
	      SQLiteDatabase db = this.getWritableDatabase();
	      ContentValues contentValues = new ContentValues();
	      contentValues.put("username", username);
	      long result = db.insert(TABLE_NAME_USER, null, contentValues);
	      db.close();
	      return result;
	   }
	   
	   public WiFiProfile getWiFi(int id){
		  WiFiProfile temp = new WiFiProfile();
	      SQLiteDatabase db = this.getReadableDatabase();
	      Cursor res =  db.rawQuery( "select * from "+TABLE_NAME_WIFI+" where ID_EAP ="+id+"", null );
	      if (res.moveToFirst()){
	    	  res.moveToFirst();
	    	  temp.isOK();
	    	  temp.setSSID(res.getString(2));
	    	  temp.setAuthType(res.getString(3));
	    	  temp.setEncryptionType(res.getString(4));
	    	  temp.setSSIDPriority(res.getInt(5));
	    	  if (res.getInt(6)==1) temp.setAutojoin(true);
	    	  else temp.setAutojoin(false);
	      }
	      else
	      {
	    	  temp.hasError();
	    	  return temp;
	      }
	      db.close();
	      return temp;
	   }
	   
	   public WiFiProfile getWiFi(String ssid){
		  WiFiProfile temp = new WiFiProfile();
	      SQLiteDatabase db = this.getReadableDatabase();
	      Cursor res =  db.rawQuery( "select * from "+TABLE_NAME_WIFI+" where ssid ='"+ssid+"'", null );
	      if (res.moveToFirst()){
	    	  res.moveToFirst();
	    	  temp.isOK();
	    	  temp.setSSID(res.getString(2));
	    	  temp.setAuthType(res.getString(3));
	    	  temp.setEncryptionType(res.getString(4));
	    	  temp.setSSIDPriority(res.getInt(5));
	    	  if (res.getInt(6)==1) temp.setAutojoin(true);
	    	  else temp.setAutojoin(false);
	      }
	      else
	      {
	    	  temp.hasError();
	    	  return temp;
	      }
	      db.close();
	      return temp;
	   }
	   
	   public ArrayList<String> getWiFi_all(){
		  ArrayList <String> templist = new ArrayList<String>();
	      SQLiteDatabase db = this.getReadableDatabase();
	      //Cursor res =  db.rawQuery( "select * from "+TABLE_NAME_WIFI+" where ID_EAP ="+id+"", null );
	      Cursor res =  db.rawQuery( "select * from "+TABLE_NAME_WIFI, null );
	      if (res.moveToFirst()){
	    	  res.moveToFirst();
	    	  while (!res.isAfterLast())
	    	  {
	    		  WiFiProfile temp = new WiFiProfile();
	    		  temp.isOK();
	    		  temp.setSSID(res.getString(2));
	    		  templist.add(temp.getSSID());
	    		  temp=null;
	    		  res.moveToNext();
	    		  eduroamCAT.debug("loop");
	    	  }
	      }
	      else
	      {
	    	  return templist;
	      }
	      db.close();
	      return templist;
	   }
	   
	   public ArrayList<ConfigProfile> getEAPProfiles(){
	      SQLiteDatabase db = this.getReadableDatabase();
	      ArrayList<ConfigProfile> tempProfiles = new ArrayList<ConfigProfile>();
	      Cursor res =  db.rawQuery( "select * from "+TABLE_NAME_EAP, null );
	      if (res.moveToFirst()){
	    	  while (!res.isAfterLast())
	    	  {
	    	      ConfigProfile tempProfile = new ConfigProfile();
	    	      tempProfile.setEAPIdP_ID(res.getString(1));
	    	      tempProfile.setDisplayName(res.getString(2), res.getString(3));
	    	      tempProfile.setDescription(res.getString(4),"");
	    	      if (res.getString(5).length()>0) tempProfile.setLogo(res.getString(5), "", "jpg");
	    	      if (res.getString(6).length()>0) tempProfile.setTermsOfUse(res.getString(6));
	    	      if (res.getString(7).length()>0) { tempProfile.setHelpdeskEmail(res.getString(7), ""); tempProfile.setHelpdeskInfo(true); }
	    	      if (res.getString(8).length()>0) { tempProfile.setHelpdeskPhone(res.getString(8), ""); tempProfile.setHelpdeskInfo(true); }
	    	      if (res.getString(9).length()>0) { tempProfile.setHelpdeskURL(res.getString(9), ""); tempProfile.setHelpdeskInfo(true); }
	    	      //look up any auth methods for eap profile
	    	      Cursor res2 =  db.rawQuery( "select * from "+TABLE_NAME_AUTH+" where ID_EAP='"+tempProfile.getEAPIdP_ID()+"'", null );
	    	      if (res2.moveToFirst()){
	    	    	  while (!res2.isAfterLast())
	    	    	  {
	    	    		  AuthenticationMethod tempAuthMethod = new AuthenticationMethod();
	    	    		  if (res2.getInt(2)>0) tempAuthMethod.setOuterEAPType(res2.getInt(2));
	    	    		  if (res2.getInt(3)>0) tempAuthMethod.setInnerEAPType(res2.getInt(3));
	    	    		  if (res2.getInt(4)>0) tempAuthMethod.setInnerNonEAPType(res2.getInt(4));
	    	    		  try {
							if (res2.getString(13).length()>0) tempAuthMethod.setCAcert(res2.getString(13), res2.getString(6), res2.getString(5));
							eduroamCAT.debug("CERT="+res2.getString(13)+" format="+res2.getString(6)+" encoding="+res2.getString(5));
	    	    		  } catch (UnsupportedEncodingException e) {
							e.printStackTrace();
	    	    		  }
	    	    		  if (res2.getString(7).length()>0) {
							  String[] temp = res2.getString(7).split(";");
							  for (int x = 0; x < temp.length; x++) {
								  tempAuthMethod.addServerID(temp[x]);
							  }
						  }
	    	    		  if (res2.getString(11).length()>0) tempAuthMethod.setAnonID(res2.getString(11), true);
	    	    		  if (res2.getString(8).length()>0)
							try {
									tempAuthMethod.loadClientCert(res2.getString(8), res2.getString(10), res2.getString(9),res2.getString(14));
								} catch (KeyStoreException e) {
									e.printStackTrace();
								}
	    	    		  tempProfile.addAuthenticationMethod(tempAuthMethod);
	    	    		  res2.moveToNext();
	    	    	  }
	    	      }
	    	      tempProfiles.add(tempProfile);
	    	      res.moveToNext();
	    	  }
	      }
	      else
	      {
	    	  return tempProfiles;
	      }
	      db.close();
	      return tempProfiles;
	   }
	   
	   public String getUser(){
		   String username="";
	      SQLiteDatabase db = this.getReadableDatabase();
	      Cursor res =  db.rawQuery( "select * from "+TABLE_NAME_USER, null );
	      if (res.moveToLast()){
	    	  res.moveToLast();
	    	  username=res.getString(1);
	      }
	      else
	      {
	    	  return "";
	      }
	      db.close();
	      return username;
	   }
	   
	   
	   public boolean deleteWiFi ()
	   {
	      SQLiteDatabase db = this.getWritableDatabase();
	      db.execSQL("DROP TABLE IF EXISTS " +TABLE_NAME_WIFI);
		  db.execSQL(DATABASE_CREATE_WIFI);
	      db.close();
		  return true;
	   }
	   
	   public boolean deleteAllProfiles ()
	   {
	      SQLiteDatabase db = this.getWritableDatabase();
	      db.execSQL("DROP TABLE IF EXISTS " +TABLE_NAME_EAP);
	      db.execSQL("DROP TABLE IF EXISTS " +TABLE_NAME_AUTH);
	      db.execSQL("DROP TABLE IF EXISTS " +TABLE_NAME_USER);
		  db.execSQL(DATABASE_CREATE_EAP);
		  db.execSQL(DATABASE_CREATE_AUTH);
		  db.execSQL(DATABASE_CREATE_USER);
	      db.close();
		  return true;
	   }

	   public int numberOfRowsWiFi(){
	      SQLiteDatabase db = this.getReadableDatabase();
	      int numRows = (int) DatabaseUtils.queryNumEntries(db, TABLE_NAME_WIFI);
	      db.close();
	      return numRows;
	   }
	   
	   public int numberOfRowsEAP(){
		      SQLiteDatabase db = this.getReadableDatabase();
		      int numRows = (int) DatabaseUtils.queryNumEntries(db, TABLE_NAME_EAP);
		      db.close();
		      return numRows;
		   }
	   
	   public int numberOfRowsAUTH(){
		      SQLiteDatabase db = this.getReadableDatabase();
		      int numRows = (int) DatabaseUtils.queryNumEntries(db, TABLE_NAME_AUTH);
		      db.close();
		      return numRows;
		   }
	   
	   public int numberOfRowsUSER(){
		   	int numRows=0;
		   	SQLiteDatabase db = this.getReadableDatabase();
		   	db.execSQL(DATABASE_CREATE_USER);
		   	numRows = (int) DatabaseUtils.queryNumEntries(db, TABLE_NAME_USER);
		   	db.close();
		   	return numRows;
		   }
	   

	  //Need to handle this, and see how data can be migrated on upgrade
	  @Override
	  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	    Log.w(ProfilesStorage.class.getName(),"Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
	    db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_EAP);
	    db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_AUTH);
	    db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_WIFI);
	    db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_USER);
	    onCreate(db);
	  }

	} 