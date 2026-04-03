package com.java.rest.apis;


import java.io.File;
import java.io.FileOutputStream;
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

public class GetInvoiceDocuments {

	public static void main(String[] args) {
		
		
		
		String serverUrl = "https://dmsfn.drreddys.com/wsi/FNCEWS40MTOM";
	       //String domain = "Domain1";
	        String user = "svc-cm_usr";
	        String password = "Drreddy$june@102021";
	        //String objectStoreName = "CMTOS";
	        String documentId="2001580338";
	        String[] CaseID=documentId.split(",");
	        
	         String outputDirectory ="C:\\Users\\Vamsi.T\\Downloads\\Invoice Documents";
	         
	         
	         
	        
	        System.out.println(CaseID.length);
	        for (int i=0;i<=CaseID.length;i++) { 
	              	
	        
	        		
	        String SqlQuery="Select ID from BAW_Invoice where DocumentTitle='"+CaseID[i]+"'";

	        try {
	            Connection connection = Factory.Connection.getConnection(serverUrl);
	            Subject subject = UserContext.createSubject(connection, user, password, null);
	    	    UserContext.get().pushSubject(subject);
	 	       Domain domain = Factory.Domain.fetchInstance(connection, null, null);
	 	      
	 	       ObjectStoreSet osSet = domain.get_ObjectStores();
		       ObjectStore store=null;
		       Iterator osIter = osSet.iterator();

		       while (osIter.hasNext()) 
		       {
		          store = (ObjectStore) osIter.next();
		          System.out.println("Object store: " + store.get_Name());
		          if(store.get_DisplayName()=="OS") {
		        	  break;
		          }
		       }
		       com.filenet.api.query.SearchScope Scope = new com.filenet.api.query.SearchScope(store);
		       
		       SearchSQL sql = new SearchSQL();
		      // Scope.fetchObjects(sql, null, null, true);
		       IndependentObjectSet sqlResult=Scope.fetchObjects(sql, null, null, true);
//		       System.out.println("res"+sqlResult);
		       Iterator iter=sqlResult.iterator();
		       while(iter.hasNext()) {
		    	   System.out.println("in while");
		    	       IndependentObject obj=(IndependentObject) iter.next();
		    	       Properties props=obj.getProperties();
		    	       Id docId =(Id)
		    	       props.getObjectValue("ID");
		    	       System.out.println(i+"--CaseID---"+CaseID[i]+"--docID---"+docId);
		               Document document = Factory.Document.fetchInstance(store, docId, null);
	                 ContentElementList cont=(ContentElementList) document.get_ContentElements();
	                 File pdffile=new File(outputDirectory+ "\\" + document.get_Name()+".pdf");
	                 OutputStream outputstream=new FileOutputStream(pdffile);
	                  Iterator iter1=cont.iterator();
	                  while (iter1.hasNext()) {
						ContentTransfer CT = (ContentTransfer) iter1.next();
//						byte[] byte1=CT.accessContentStream().readAllBytes();
						byte[] byte1=CT.get_ContentSignature();
						
						outputstream.write(byte1);
						outputstream.close();
	                  
	                  }
		       }}
	        catch (Exception e) {
	            System.err.println("Error downloading document: " + e.getMessage());
	        }
		       
	        } 

	}

}
