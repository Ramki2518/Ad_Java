package com.java.rest.apis;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import javax.security.auth.Subject;

import com.filenet.api.collection.ContentElementList;
import com.filenet.api.collection.IndependentObjectSet;
import com.filenet.api.collection.ObjectStoreSet;
import com.filenet.api.core.Connection;
import com.filenet.api.core.ContentTransfer;
import com.filenet.api.core.Document;
import com.filenet.api.core.Domain;
import com.filenet.api.core.Factory;
import com.filenet.api.core.IndependentObject;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.property.Properties;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.util.Id;
import com.filenet.api.util.UserContext;

public class GetDocumentsFromFileNet {

    public static void main(String[] args) {

        String serverUrl = "https://dmsfn.drreddys.com/wsi/FNCEWS40MTOM";
        String user = "svc-cm_usr";
        String password = "Drreddy$june@102021";
        String documentId = "H5065882251";
        String[] CaseID = documentId.split(",");
        String outputDirectory = "C:\\Users\\Vamsi.T\\Downloads\\testFol";

        System.out.println(CaseID.length);
        for (int i = 0; i < CaseID.length; i++) {  // Fixed index loop issue

            String SqlQuery = "Select ID from BAW_Vikreta_PO where DocumentTitle='" + CaseID[i] + "'";     //original--> BAW_Invoice

            try {
                Connection connection = Factory.Connection.getConnection(serverUrl);
                Subject subject = UserContext.createSubject(connection, user, password, null);
                UserContext.get().pushSubject(subject);
                Domain domain = Factory.Domain.fetchInstance(connection, null, null);

                ObjectStoreSet osSet = domain.get_ObjectStores();
                ObjectStore store = null;
                Iterator osIter = osSet.iterator();

                while (osIter.hasNext()) {
                    store = (ObjectStore) osIter.next();
                    if (store.get_DisplayName().equals("OS")) {  // Corrected string comparison
                        break;
                    }
                }

                com.filenet.api.query.SearchScope scope = new com.filenet.api.query.SearchScope(store);
                SearchSQL sql = new SearchSQL(SqlQuery);
                IndependentObjectSet sqlResult = scope.fetchObjects(sql, null, null, true);
                Iterator iter = sqlResult.iterator();
                while (iter.hasNext()) {
                    IndependentObject obj = (IndependentObject) iter.next();
                    Properties props = obj.getProperties();
                    Id docId = (Id) props.getObjectValue("ID");
                    System.out.println(i + "--CaseID---" + CaseID[i] + "--docID---" + docId);
                    Document document = Factory.Document.fetchInstance(store, docId, null);
                    ContentElementList cont = (ContentElementList) document.get_ContentElements();
                    File pdffile = new File(outputDirectory + "\\" + document.get_Name() + ".pdf");
                    OutputStream outputstream = new FileOutputStream(pdffile);
                    Iterator iter1 = cont.iterator();
                    
                    while (iter1.hasNext()) {
                        ContentTransfer CT = (ContentTransfer) iter1.next();
                        InputStream contentStream = CT.accessContentStream();
                        
                        if (contentStream != null) {
                            byte[] buffer = new byte[4096];  // Read content in chunks
                            int bytesRead;
                            while ((bytesRead = contentStream.read(buffer)) != -1) {
                                outputstream.write(buffer, 0, bytesRead);  // Write the chunk to file
                            }
                        }
                        else {
                            System.err.println("Content stream is null for document: " + document.get_Name());
                        }
                    }
                       
                    outputstream.close();
                           
                }
                    //outputstream.close();  // Close stream outside the loop
                }
             catch (Exception e) {
                e.printStackTrace();  // Print the stack trace for debugging
                System.err.println("Error downloading document: " + e.getMessage());
            }
        }
    }
}