package com.java.rest.apis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;



public class Rest {


	public static void main(String[] args) {

		String GET_URL = "";
		String env = "PROD";
		String instanceid = "1436973";
		String[] pid =instanceid.split(",");
		String username="svc-cm_usr";
		String password="Drreddy$june@102021";
		String opresponse="";
		String tokenID="";
		String FlowObjID = "a3efce1b-5c0d-46b6-88a5-b96efc9303d0";//NON-PO End Event
		//String FlowObjID = "92521c8f-39fe-4b0b-8b3f-98a1075dcd8";
		//String FlowObjID = "29bd61ca-ffe3-41b5-83e6-50ab1d0c90c2";//PO END Event
		//String FlowObjID = "6b003691-728e-4fcc-b646-7a9bf13b411f"; // Payment Help Desk Case Close Event
		//String FlowObjID = "92521c8f-39fe-4b0b-8b3f-98a1075dcd8b";
		//String FlowObjID = "ae2619a9-1599-4d87-8cca-282eb5fe8e19";





		String pidd="";
		System.out.println("length"+pid.length);
		//			XSSFWorkbook workbook = new XSSFWorkbook();
		//			XSSFSheet spreadsheet =  workbook.createSheet("HOTEL GST Data");

		for (int j=0;j<pid.length;j++) {
			pidd=pid[j];
			System.out.println(pidd);

			if(env == "PROD") {
				GET_URL="https://dms.drreddys.com/rest/bpm/wle/v1/process/"+pidd+"?parts=all";
			}
			else if(env == "DEV") {
				GET_URL="https://devbaw.drreddys.com/rest/bpm/wle/v1/process/"+pidd+"?parts=all";
			}
			else{
				GET_URL="https://uatbaw.drreddys.com/rest/bpm/wle/v1/process/"+pidd+"?parts=all";
			}


			try 
			{
				URL obj = new URL(GET_URL);
				HttpURLConnection httpURLConnection = (HttpURLConnection) obj.openConnection();
				httpURLConnection.setRequestMethod("GET");
				String authString = username + ":" + password;
				String authHeader = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(authString.getBytes());
				httpURLConnection.setRequestProperty("Authorization", authHeader);
				httpURLConnection.setRequestMethod("GET");
				httpURLConnection.setRequestProperty("Accept", "application/json");
				int responseCode=httpURLConnection.getResponseCode();


				if (responseCode ==200) {
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
					System.out.println("tokenID"+tokenID);
					System.out.println(tokenID);
					System.out.println("j"+j+"pidd"+pidd);

					moveToken(pidd, tokenID, FlowObjID,env);
					//						            deleteToken(pidd, tokenID);
				}

				else {
					System.out.println("IBM Rest call failed");
				}
			}

			catch (Exception e) {
				e.printStackTrace();

			}
		}

	}

	public static String getToken(String opresponse ) throws ParseException {

		JSONParser parser=new JSONParser(); Object object =parser.parse(opresponse);
		JSONObject mainobj=(JSONObject)object; JSONObject
		Data=(JSONObject)mainobj.get("data"); 
		//					  System.out.println("REST API Data"+Data);

		JSONObject executiontree = (JSONObject)Data.get("executionTree"); 
		JSONObject root = (JSONObject)executiontree.get("root"); 
		JSONArray children = (JSONArray)root.get("children"); 
		String tokenId = ""; 

		for(int i=0;i<children.size();i++) {
			JSONObject token=(JSONObject)children.get(i);
			tokenId = (String)token.get("tokenId"); 
			System.out.println("THE TOKEN ID ::"+tokenId);
		}

		return tokenId;
	}

	public static void deleteToken(String pidd,String TokenID) throws IOException  {
		String username="svc-cm_usr";
		String password="Drreddy$june@102021";

		String URL1="https://dms.drreddys.com/rest/bpm/wle/v1/process/"+pidd+"?action=deleteToken&tokenId="+TokenID+"&resume=true&parts=all";

		URL obj = new URL(URL1);
		HttpURLConnection httpURLConnection = (HttpURLConnection) obj.openConnection();
		String authString = username + ":" + password;
		String authHeader = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(authString.getBytes());
		httpURLConnection.setRequestProperty("Authorization", authHeader);
		httpURLConnection.setRequestMethod("POST");
		int responseCode1=httpURLConnection.getResponseCode();
		//System.out.println("delete"+responseCode1);
		if (responseCode1==200) {
			System.out.println("Delete token is succesful for pid"+pidd);

		}


	}


	public static void moveToken(String pidd,String TokenID ,String FlowObjID,String env) throws Exception
	{

		String username="svc-cm_usr";
		String password="Drreddy$june@102021",URL1 = "";



		if(env == "PROD") {
			URL1=" https://dms.drreddys.com/rest/bpm/wle/v1/process/"+pidd+"?action=moveToken&tokenId="+TokenID+"&target="+FlowObjID+"&resume=true&parts=all";
		}
		else if(env == "DEV") {
			System.out.println("Running DEV URL");   	    	
			URL1=" https://devbaw.drreddys.com/rest/bpm/wle/v1/process/"+pidd+"?action=moveToken&tokenId="+TokenID+"&target="+FlowObjID+"&resume=true&parts=all";


		}
		else{
			URL1=" https://uatbaw.drreddys.com/rest/bpm/wle/v1/process/"+pidd+"?action=moveToken&tokenId="+TokenID+"&target="+FlowObjID+"&resume=true&parts=all";
		}



		//		     String URL1=" https://dms.drreddys.com/rest/bpm/wle/v1/process/"+pidd+"?action=moveToken&tokenId="+TokenID+"&target="+FlowObjID+"&resume=true&parts=all";

		System.out.println(URL1);

		URL obj = new URL(URL1);
		System.out.println("The URL--> "+obj);
		HttpURLConnection httpURLConnection = (HttpURLConnection) obj.openConnection();
		String authString = username + ":" + password;
		String authHeader = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(authString.getBytes());
		httpURLConnection.setRequestProperty("Authorization", authHeader);
		httpURLConnection.setRequestMethod("POST");
		int responseCode1=httpURLConnection.getResponseCode();
		System.out.println("move"+responseCode1);
		if (responseCode1==200) {
			System.out.println("Move token is succesfull for pid"+pidd);

		}
	}

	public static void excelfile(int i,String pid,XSSFSheet spreadsheet,XSSFWorkbook workbook) {

		String status="Case Closed From Backend";
		String Casestate="Complete";
		try {


			XSSFRow Row=spreadsheet.createRow(0);
			//System.out.println("-----------"+i+"----"+pid);
			XSSFCell Cell =Row.createCell(0); 
			Cell.setCellValue("output Query");

			XSSFRow Datarow=spreadsheet.createRow(i+1);
			XSSFCell  Cell1 =Datarow.createCell(0);
			Cell1.setCellValue("UPDATE APDB.PROCESS_INSTANCES_DETAILS SET  FILE_STATUS='"+status+"',CLOSED_ON=CURRENT_DATE, CASE_STATE='"+Casestate+"', STATUS='"+status+"' WHERE P_ID='"+pid+"';");

			FileOutputStream out =new FileOutputStream(new File("C:\\Users\\Premsai.P\\Desktop\\hotelgst100.xlsx"));
			workbook.write(out);
			out.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}

