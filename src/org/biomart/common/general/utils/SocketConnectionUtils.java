package org.biomart.common.general.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URLEncoder;

/**
HTTP/1.1 200 OK
Date: Thu, 02 Jul 2009 15:37:27 GMT
Server: Apache/2.2.9 (Debian) mod_perl/2.0.4 Perl/v5.10.0
Connection: close
Content-Type: text/plain

ENSG00000208234	ENST00000385499
ENSG00000199674	ENST00000362804
ENSG00000221622	ENST00000408695
 * @author anthony
 *
 */
public class SocketConnectionUtils {

	public static void main(String[] args) {
		try {
	        // Construct data
	        String data = URLEncoder.encode("query", "UTF-8") + "=" + URLEncoder.encode("<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE Query><Query  virtualSchemaName = \"default\" formatter = \"TSV\" header = \"0\" uniqueRows = \"0\" count = \"\" datasetConfigVersion = \"0.6\" ><Dataset name = \"hsapiens_gene_ensembl\" interface = \"default\" ><Attribute name = \"ensembl_gene_id\" /><Attribute name = \"ensembl_transcript_id\" /></Dataset></Query>", "UTF-8");
	        //data += "&" + URLEncoder.encode("key2", "UTF-8") + "=" + URLEncoder.encode("value2", "UTF-8");
	        //http://www.biomart.org/biomart/martservice?query=
	        
	        // Create a socket to the host
	        String hostname = "www.biomart.org";
	        int port = 80;
	        InetAddress addr = InetAddress.getByName(hostname);
	        Socket socket = new Socket(addr, port);
	    
	        // Send header
	        String path = "/biomart/martservice";
	        BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
	        
	        PrintWriter pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
	        pw.println("POST "+path+" HTTP/1.0");
	        pw.println("Content-Length: "+data.length()+"");
	        pw.println("Content-Type: application/x-www-form-urlencoded");
	        pw.println("");
	    
	        // Send data
	        pw.print(data);
	        pw.flush();
	        
	        /*wr.write("POST "+path+" HTTP/1.0\r\n");
	        wr.write("Content-Length: "+data.length()+"\r\n");
	        wr.write("Content-Type: application/x-www-form-urlencoded\r\n");
	        wr.write("\r\n");
	    
	        // Send data
	        wr.write(data);
	        wr.flush();*/
	    
	        // Get response
	        BufferedReader rd = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	        String line;
	        while ((line = rd.readLine()) != null) {
	            System.out.println(line);
	        }
	        wr.close();
	        rd.close();
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }

	}
}
