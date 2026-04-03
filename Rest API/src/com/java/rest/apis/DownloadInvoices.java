package com.java.rest.apis;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.stream.Collectors;

import javax.security.auth.Subject;

import com.filenet.api.collection.ContentElementList;
import com.filenet.api.collection.IndependentObjectSet;
import com.filenet.api.core.Connection;
import com.filenet.api.core.ContentTransfer;
import com.filenet.api.core.Document;
import com.filenet.api.core.Domain;
import com.filenet.api.core.Factory;
import com.filenet.api.core.IndependentObject;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.property.Properties;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;
import com.filenet.api.util.Id;
import com.filenet.api.util.UserContext;

public class DownloadInvoices {

    public static void main(String[] args) {
        
      String serverUrl = "https://dmsfn.drreddys.com/wsi/FNCEWS40MTOM";
      String user = "svc-cm_usr";
      String password = "Drreddy$june@102021";
      String documentId = "H5067741781,H5067741789,H5067741788,H5067741786";  
      String[] CaseID = documentId.split(",");
      //String outputDirectory = "C:\\Users\\Vamsi.T\\Downloads\\TestFold";  //Invoice Documents
      String outputDirectory = "D:\\DRL\\JAVA\\Projects\\TestResults1";
      
      System.out.println("Number of documents to fetch: " + CaseID.length);
        
        

        try {
            // Establish Connection
            Connection connection = Factory.Connection.getConnection(serverUrl);
            Subject subject = UserContext.createSubject(connection, user, password, null);
            UserContext.get().pushSubject(subject);

            // Get Domain and ObjectStore directly
            Domain domain = Factory.Domain.fetchInstance(connection, null, null);
            ObjectStore store = Factory.ObjectStore.fetchInstance(domain, "OS", null);
            System.out.println("Connected to ObjectStore: " + store.get_Name());

            // Build single IN query
            String inClause = Arrays.stream(CaseID)
                                    .map(id -> "'" + id.trim() + "'")
                                    .collect(Collectors.joining(","));
            String sqlQuery = "SELECT ID, DocumentTitle FROM BAW_Invoice WHERE DocumentTitle IN (" + inClause + ")";
            System.out.println("Executing batch query: " + sqlQuery);

            // Execute search
            SearchSQL sql = new SearchSQL(sqlQuery);
            SearchScope scope = new SearchScope(store);
            IndependentObjectSet sqlResult = scope.fetchObjects(sql, null, null, true);

            // Build map of title -> ID
            Map<String, Id> docIdMap = new HashMap<>();
            Iterator iter = sqlResult.iterator();
            while (iter.hasNext()) {
                IndependentObject obj = (IndependentObject) iter.next();
                Properties props = obj.getProperties();
                String title = props.getStringValue("DocumentTitle");
                Id docId = (Id) props.getObjectValue("ID");
                docIdMap.put(title, docId);
                System.out.println("Mapped title to ID: " + title + " => " + docId);
            }

            // Loop through input CaseIDs
            for (String caseId : CaseID) {
                caseId = caseId.trim();
                Id docId = docIdMap.get(caseId);

                if (docId != null) {
                    System.out.println("Fetching document with ID: " + docId);
                    Document document = Factory.Document.fetchInstance(store, docId, null);
                    ContentElementList contentList = document.get_ContentElements();

                    File pdfFile = new File(outputDirectory + "\\" + document.get_Name() + ".pdf");

                    try (OutputStream outputStream = new FileOutputStream(pdfFile)) {
                        Iterator contentIter = contentList.iterator();
                        while (contentIter.hasNext()) {
                            ContentTransfer CT = (ContentTransfer) contentIter.next();
                            try (InputStream inputStream = new BufferedInputStream(CT.accessContentStream())) {
                                byte[] buffer = new byte[16384]; // 16KB buffer
                                int bytesRead;
                                while ((bytesRead = inputStream.read(buffer)) != -1) {
                                    outputStream.write(buffer, 0, bytesRead);
                                }
                            }
                        }
                    }

                    System.out.println("Downloaded: " + pdfFile.getAbsolutePath());
                } else {
                    System.out.println("No document found for CaseID: " + caseId);
                }
            }

        } catch (Exception e) {
            System.err.println("Error downloading document: " + e.getMessage());
            e.printStackTrace();
        }
    }
}


//************************************************************************************************************

//package com.java.rest.apis;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.OutputStream;
//
//import java.io.InputStream;
//import java.io.BufferedInputStream;
//
//import java.util.Iterator;
//import javax.security.auth.Subject;
//import com.filenet.api.collection.ContentElementList;
//import com.filenet.api.collection.IndependentObjectSet;
//import com.filenet.api.collection.ObjectStoreSet;
//import com.filenet.api.core.Connection;
//import com.filenet.api.core.ContentTransfer;
//import com.filenet.api.core.Document;
//import com.filenet.api.core.Domain;
//import com.filenet.api.core.Factory;
//import com.filenet.api.core.IndependentObject;
//import com.filenet.api.core.ObjectStore;
//import com.filenet.api.property.Properties;
//import com.filenet.api.query.SearchSQL;
//import com.filenet.api.query.SearchScope;
//import com.filenet.api.util.Id;
//import com.filenet.api.util.UserContext;
//
//public class DownloadInvoices {
//
//    public static void main(String[] args) {
//        
//        String serverUrl = "https://dmsfn.drreddys.com/wsi/FNCEWS40MTOM";
//        String user = "svc-cm_usr";
//        String password = "Drreddy$june@102021";
//        String documentId = "H5067741781,H5067741789,H5067741788,H5067741786";  
//        String[] CaseID = documentId.split(",");
//        //String outputDirectory = "C:\\Users\\Vamsi.T\\Downloads\\TestFold";  //Invoice Documents
//        String outputDirectory = "D:\\DRL\\JAVA\\Projects\\TestResults";
//        
//        System.out.println("Number of documents to fetch: " + CaseID.length);
//
//        try {
//            // Establish Connection
//            Connection connection = Factory.Connection.getConnection(serverUrl);
//            Subject subject = UserContext.createSubject(connection, user, password, null);
//            UserContext.get().pushSubject(subject);
//
//            // Get Domain
//            Domain domain = Factory.Domain.fetchInstance(connection, null, null);
//
//            // Get Object Store
//            ObjectStore store = null;
//            ObjectStoreSet osSet = domain.get_ObjectStores();
//            Iterator osIter = osSet.iterator();
//            while (osIter.hasNext()) {
//                store = (ObjectStore) osIter.next();
//                System.out.println("Object store found: " + store.get_Name());
//                if (store.get_DisplayName().equals("OS")) { // Fix: Use .equals()
//                    break;
//                }
//            }
//
//            // Iterate Over Case IDs
//            for (int i = 0; i < CaseID.length; i++) {  // Fix: Changed `<=` to `<`
//                
//                String SqlQuery = "SELECT ID FROM BAW_Invoice WHERE DocumentTitle = '" + CaseID[i] + "'";
//                System.out.println("Executing query: " + SqlQuery);
//
//                // Search for Documents
//                SearchSQL sql = new SearchSQL();
//                sql.setQueryString(SqlQuery); // Fix: Set query before fetching
//                SearchScope scope = new SearchScope(store);
//                IndependentObjectSet sqlResult = scope.fetchObjects(sql, null, null, true);
//
//                if (!sqlResult.isEmpty()) {
//                    Iterator iter = sqlResult.iterator();
//                    while (iter.hasNext()) {
//                        IndependentObject obj = (IndependentObject) iter.next();
//                        Properties props = obj.getProperties();
//                        Id docId = (Id) props.getObjectValue("ID");
//                        System.out.println("Found document: " + docId);
//
//                        // Fetch Document
//                        Document document = Factory.Document.fetchInstance(store, docId, null);
//                        ContentElementList contentList = document.get_ContentElements();
//                        File pdfFile = new File(outputDirectory + "\\" + document.get_Name() + ".pdf");
//
//                        // Write File
//                        
//                        try (OutputStream outputStream = new FileOutputStream(pdfFile)) {
//                            Iterator iter1 = contentList.iterator();
//                            
//							/*
//							 * while (iter1.hasNext()) { ContentTransfer CT = (ContentTransfer)
//							 * iter1.next(); byte[] byteData = CT.accessContentStream().readAllBytes(); //
//							 * Fix: Correct method to read content outputStream.write(byteData); }
//							 */
//                            
//                            while (iter1.hasNext()) {
//                                ContentTransfer CT = (ContentTransfer) iter1.next();
//                                try (InputStream inputStream = new BufferedInputStream(CT.accessContentStream())) {
//                                    byte[] buffer = new byte[4096];
//                                    int bytesRead;
//                                    while ((bytesRead = inputStream.read(buffer)) != -1) {
//                                        outputStream.write(buffer, 0, bytesRead);
//                                    }
//                                }
//                            }
//                            
//                        }
//                        System.out.println("Downloaded: " + pdfFile.getAbsolutePath());
//                    }
//                } else {
//                    System.out.println("No documents found for CaseID: " + CaseID[i]);
//                }
//            }
//
//        } catch (Exception e) {
//            System.err.println("Error downloading document: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//}