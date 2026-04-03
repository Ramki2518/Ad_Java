package com.java.rest.apis;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

class GetInstanceRESTAPI {

	public static void main(String[] args) {
		
		
		String GET_URL = "";
		 String env = "PROD";
		 String instanceid = "438477,431278,472029,457964";
		  
		 //////////Add instanceIDs here/////////
		 
		 
		 String[] pid =instanceid.split(",");
		 String username="svc-cm_usr";
		 String password="Drreddy$june@102021";
		 String opresponse="";
		 String tokenID="";
		 String FlowObjID = "6fd58c0a-fbe3-4086-85af-8e231c880cc5";
				 
				 //"a9e9908c-4f7f-46ac-821b-d2c9e1a03859";  --post blocks queue flow
		 
		 	
		  String pidd="";
		  System.out.println("length"+pid.length);
		  XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet spreadsheet =  workbook.createSheet("HOTEL GST Data");

	      for (int j=0;j<pid.length;j++) {
	    	      pidd=pid[j];
	    	      System.out.println(pidd);
	    	      
	    	      if(env == "PROD") {
	    	    	  GET_URL="https://dms.drreddys.com/rest/bpm/wle/v1/process/"+pidd+"?parts=all";
	    	      }
	    	      else if(env == "DEV") {
	    	    	  System.out.println("Running DEV URL");
	    	    	  GET_URL="https://devbaw.drreddys.com/rest/bpm/wle/v1/process/"+pidd+"?parts=all";
	    	      }
	    	      else{
	    	    	  GET_URL="https://uatbaw.drreddys.com/rest/bpm/wle/v1/process/"+pidd+"?parts=all";
	    	      }


			try {	
				
				URL obj = new URL(GET_URL);
		        HttpURLConnection httpURLConnection = (HttpURLConnection) obj.openConnection();
		        httpURLConnection.setRequestMethod("GET");
		        String authString = username + ":" + password;
		         String authHeader = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(authString.getBytes());
		         httpURLConnection.setRequestProperty("Authorization", authHeader);
		         httpURLConnection.setRequestMethod("GET");
		         httpURLConnection.setRequestProperty("Accept", "application/json");
		  int responseCode=httpURLConnection.getResponseCode();
		  
		  
		         if (responseCode == 200) {
	             BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), StandardCharsets.UTF_8));
	             String inputLine;
	             
	             StringBuilder content = new StringBuilder();
	             while ((inputLine = reader.readLine()) != null) {
	                 content.append(inputLine);
	             }
	             reader.close();
	             opresponse=content.toString();
	             //System.out.println(opresponse);
	             tokenID=getToken(opresponse);
	             //System.out.println("tokenID"+tokenID);
	            System.out.println(tokenID);
	             System.out.println("j"+j+"pidd"+pidd);
	           
//	            moveToken(pidd, tokenID, FlowObjID,env);
//	            deleteToken(pidd, tokenID);
	             

	             
	         } 
		         else {
	             System.out.println("IBM Rest call failed");
	         }
			} catch (Exception e) {
				e.printStackTrace();
			
		         }
	              }
		
	}

	
	public static String getToken(String opresponse ) throws ParseException {
		  
		  JSONParser parser=new JSONParser(); Object object =parser.parse(opresponse);
		  JSONObject mainobj=(JSONObject)object; JSONObject
		  Data=(JSONObject)mainobj.get("data"); //System.out.println("Data"+Data);
		  
		  JSONObject executiontree=(JSONObject)Data.get("executionTree"); JSONObject
		  root=(JSONObject)executiontree.get("root"); JSONArray
		  children=(JSONArray)root.get("children"); String tokenId=""; for(int
		  i=0;i<children.size();i++) {
		  
		  JSONObject token=(JSONObject)children.get(i);
		  tokenId=(String)token.get("tokenId"); System.out.println(tokenId);
		  
		  
		  }
		  
		  return tokenId;
	  }
	
}
