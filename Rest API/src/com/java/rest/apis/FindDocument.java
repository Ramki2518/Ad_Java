package com.java.rest.apis;

//package com.ibm.drl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import javax.security.auth.Subject;

import com.filenet.api.collection.IndependentObjectSet;
import com.filenet.api.core.Connection;
import com.filenet.api.core.Document;
import com.filenet.api.core.Domain;
import com.filenet.api.core.Factory;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.property.Properties;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;
import com.filenet.api.util.UserContext;

public class FindDocument {
    private static Connection conn = null;
    static Domain domain;
    static ObjectStore objStore;
    static String docTitle = null;
    private static String uniqueId;
    static int count=0;

    public static Connection getCEConn() {

        try {

            String ceURI = "https://dmsfn.drreddys.com/wsi/FNCEWS40MTOM/";
            String userName = "svc-cm_usr";
            String password = "Drreddy$june@102021";
            if (conn == null) {
                conn = Factory.Connection.getConnection(ceURI);
                Subject subject = UserContext.createSubject(conn, userName, password, null);
                UserContext uc = UserContext.get();
                uc.pushSubject(subject);
                domain = Factory.Domain.getInstance(conn, null);
                System.out.println("domain" + domain);
                objStore = Factory.ObjectStore.fetchInstance(domain, "OS", null);
            }

        } catch (Exception e1) {
            e1.printStackTrace();
        }
        System.out.println("CE Conn :: " + conn);
        return conn;
    }

public static void docName() throws IOException {
    
	

    SearchScope searchScope = new SearchScope(objStore);
    File cs = new File("D:\\DRL\\DownloadInvoicesFromFileNet\\FindDocument.txt");       //3000094381
            BufferedReader br = new BufferedReader(new FileReader(cs));
             while ((uniqueId = br.readLine()) != null) {
  
//     String sqlStr = "SELECT * FROM [BAW_Vikreta_PO] WHERE [DocumentTitle]  = '" + uniqueId + "'"; 
            	 //BAW_Vikreta_PO, BAW_VikretaIM, BAW_Vikreta_NONPO

        //String sqlStr = "SELECT * FROM [BAW_Vikreta_NONPO] WHERE [DocumentTitle]  = '" + uniqueId + "'";   
          //String sqlStr = "SELECT * FROM [BAW_Vikreta_PO] WHERE [DocumentTitle]  = '" + uniqueId + "'";
            	 String sqlStr = "SELECT * FROM [BAW_Invoice] WHERE [DocumentTitle]  = '" + uniqueId + "'";
 
		SearchSQL searchSQL = new SearchSQL(sqlStr);
        IndependentObjectSet independentObjectSet = searchScope.fetchObjects(searchSQL, new Integer(10), null,	new Boolean(true));
		Iterator iterator = independentObjectSet.iterator();
		
		if (iterator.hasNext()) {
		
				Document Doc = (Document) iterator.next();
                Properties documentProperties = Doc.getProperties();
				docTitle = documentProperties.getStringValue("DocumentTitle");
                System.out.println(docTitle +"\t Received");
                       count++;
                }
                else {
                    System.out.println(uniqueId +"\t Missing");
                }
}
             }

public static void main(String[] args) throws IOException {
		getCEConn();        
		docName();
		System.out.println(count);
	}
}