package com.java.rest.apis;


import com.filenet.api.core.*;
import com.filenet.api.query.*;
import com.filenet.api.util.UserContext;
import com.filenet.api.collection.ContentElementList;
import com.filenet.api.collection.IndependentObjectSet;
import com.filenet.api.util.Id;

import java.io.*;
import java.util.Iterator;
import java.util.Properties;

import javax.security.auth.Subject;

public class TestDownloadDocuments {

    private static final String SERVER_URL = "https://dmsfn.drreddys.com/wsi/FNCEWS40MTOM";
    private static final String USERNAME = "svc-cm_usr"; 	// Secure Credentials
    private static final String PASSWORD = "Drreddy$june@102021"; 	// Secure Credentials
    private static final String OUTPUT_DIRECTORY = "C:\\Users\\Vamsi.T\\Downloads\\Invoice Documents";

    public static void main(String[] args) {
        String documentIds = "ID1,ID2,ID3"; // Example document IDs
        String[] caseIds = documentIds.split(",");

        System.out.println("Total documents to download: " + caseIds.length);

        try {
            // Establish Connection
            Connection connection = Factory.Connection.getConnection(SERVER_URL);
            Subject subject = UserContext.createSubject(connection, USERNAME, PASSWORD, null);
            UserContext.get().pushSubject(subject);

            // Get Object Store
            Domain domain = Factory.Domain.fetchInstance(connection, null, null);
            ObjectStore store = getObjectStore(domain, "OS");
            if (store == null) {
                throw new RuntimeException("Object Store not found!");
            }

            // Process each Case ID sequentially
            for (String caseId : caseIds) {
                processDocument(store, caseId, OUTPUT_DIRECTORY);
            }

        } catch (Exception e) {
            System.err.println("Error initializing FileNet connection: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static ObjectStore getObjectStore(Domain domain, String storeName) {
        for (Iterator osIter = domain.get_ObjectStores().iterator(); osIter.hasNext(); ) {
            ObjectStore store = (ObjectStore) osIter.next();
            if (store.get_DisplayName().equals(storeName)) {
                System.out.println("Connected to Object Store: " + store.get_Name());
                return store;
            }
        }
        return null;
    }

    private static void processDocument(ObjectStore store, String caseId, String outputDir) {
        long startTime = System.currentTimeMillis();

        // Ensure security context for each document process
        Connection connection = Factory.Connection.getConnection(SERVER_URL);
        Subject subject = UserContext.createSubject(connection, USERNAME, PASSWORD, null);
        UserContext.get().pushSubject(subject);

        try {
            // Construct Query
            String sqlQuery = "SELECT ID, DocumentTitle FROM BAW_Invoice WHERE DocumentTitle = '" + caseId + "'";
            SearchSQL searchSQL = new SearchSQL(sqlQuery);
            SearchScope searchScope = new SearchScope(store);
            IndependentObjectSet results = searchScope.fetchObjects(searchSQL, null, null, true);

            if (results.isEmpty()) {
                System.out.println("No documents found for CaseID: " + caseId);
                return;
            }

            for (Iterator iter = results.iterator(); iter.hasNext(); ) {
                IndependentObject obj = (IndependentObject) iter.next();
                Properties props = (Properties) obj.getProperties();
                Id docId = (Id) ((com.filenet.api.property.Properties) props).getObjectValue("ID");

                System.out.println("Downloading document ID: " + docId);
                fetchAndSaveDocument(store, docId, outputDir);
            }

        } catch (Exception e) {
            System.err.println("Error processing document for CaseID " + caseId + ": " + e.getMessage());
        } finally {
            long endTime = System.currentTimeMillis();
            System.out.println("Download time for CaseID " + caseId + ": " + (endTime - startTime) + " ms");

            // Clear security context after execution
            UserContext.get().popSubject();
        }
    }

    private static void fetchAndSaveDocument(ObjectStore store, Id docId, String outputDir) {
        long startTime = System.currentTimeMillis();
        try {
            Document document = Factory.Document.fetchInstance(store, docId, null);
            ContentElementList contentList = document.get_ContentElements();

            // Determine file type (PDF or ZIP) based on MIME type
            String mimeType = document.get_MimeType();
            String fileExtension = ".pdf"; // Default to PDF
            if ("application/zip".equalsIgnoreCase(mimeType)) {
                fileExtension = ".zip";
            }

            String fileName = document.get_Name() + fileExtension;
            File outputFile = new File(outputDir, fileName);

            try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile))) {
                for (Iterator iter = contentList.iterator(); iter.hasNext(); ) {
                    ContentTransfer contentTransfer = (ContentTransfer) iter.next();
                    try (InputStream inputStream = new BufferedInputStream(contentTransfer.accessContentStream())) {
                        byte[] buffer = new byte[8192]; // Larger buffer for efficiency
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                    }
                }
            }

            long endTime = System.currentTimeMillis();
            System.out.println("Downloaded: " + outputFile.getAbsolutePath() + " (Time: " + (endTime - startTime) + " ms)");

        } catch (Exception e) {
            System.err.println("Error saving document ID " + docId + ": " + e.getMessage());
        }
    }
}
