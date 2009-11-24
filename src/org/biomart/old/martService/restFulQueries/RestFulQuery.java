package org.biomart.old.martService.restFulQueries;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.biomart.common.general.constants.MyConstants;
import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.objects.MartConfiguratorUtils;
import org.biomart.old.martService.MartServiceConstants;
import org.biomart.old.martService.restFulQueries.objects.Attribute;



public class RestFulQuery {
	
	String martServiceServer = null;
	String virtualSchemaName = null;
	
	String datasetConfigVersion = null;
	String formatter = null;
	Boolean count = null;
	Boolean header = null;
	Boolean unique = null;
	Integer limitStart = null;
	Integer limitSize = null;
	public List<RestFulQueryDataset> datasetList = null;
	
	//Boolean emptyInList = null;
	
	String query = null;
	String queryHtmlGet = null;
	
	public static void main(String[] args) throws Exception {
		multipleDatasetJoin();
	}

	public static void multipleDatasetJoin() throws UnsupportedEncodingException, IOException, FunctionalException, TechnicalException {
		RestFulQuery query = new RestFulQuery(MartServiceConstants.BMTEST_MART_SERVICE_STRING_URL, "default", 
				MartServiceConstants.DEFAULT_BIOMART_VERSION, MartServiceConstants.DEFAULT_FORMATTER, false, false, false, null, null 
				, new RestFulQueryDataset("rgd_genes", new ArrayList<Attribute>(Arrays.asList(new Attribute[] {new Attribute("uniprot_acc_attr")})), null)
				//, new RestFulQueryDataset("reaction" , new ArrayList<Attribute>(Arrays.asList(new Attribute[] {new Attribute("referencedatabase_uniprot")})), null)
				//, new RestFulQueryDataset("ipi_rat", new ArrayList<Attribute>(Arrays.asList(new Attribute[] {new Attribute("uniprot_acc")})), null)
		
				, new RestFulQueryDataset("kermits", new ArrayList<Attribute>(Arrays.asList(new Attribute[] {new Attribute("escell_clone_name")})), null)
		);
		query.buildQuery();
		
		//[Query ERROR: caught BioMart::Exception: non-BioMart die(): Can't call method "defaultLink" on an undefined value at /home/acros/biomart-perl3/lib/BioMart/Query.pm line 1716.]

		System.out.println("running : " + query.getReadableUrl());
		List<String> results1 = query.urlContentToStringList(180000); //resultsToListString();
		System.out.println(results1);
	}
	
	/*public RestFulQuery(String datasetName, String datasetConfigVersion, Boolean count, List<Attribute> attributesList, List<Filter> filtersList) throws UnsupportedEncodingException {
		this(MartServiceConstants.CENTRAL_PORTAL_MART_SERVICE_STRING_URL, "default", datasetName, datasetConfigVersion, "TSV", count, false, false, null, null, attributesList, filtersList);
		
		public RestFulQuery(String martServiceServer, String virtualSchemaName, String datasetName, String datasetConfigVersion, String formatter, Boolean count, Boolean header, Boolean unique, 
			Integer limitStart, Integer limitSize, List<Attribute> attributesList, List<Filter> filtersList) throws UnsupportedEncodingException {
	}*/
	public RestFulQuery(String martServiceServer, String virtualSchemaName, String datasetConfigVersion, String formatter, Boolean count, Boolean header, Boolean unique, 
			Integer limitStart, Integer limitSize, RestFulQueryDataset dataset) throws UnsupportedEncodingException {
		this(martServiceServer, virtualSchemaName, datasetConfigVersion, formatter, count, header, unique, 
				limitStart, limitSize, new RestFulQueryDataset[]{dataset});
	}
	public RestFulQuery(String martServiceServer, String virtualSchemaName, String datasetConfigVersion, String formatter, Boolean count, Boolean header, Boolean unique, 
			Integer limitStart, Integer limitSize, RestFulQueryDataset dataset1, RestFulQueryDataset dataset2) throws UnsupportedEncodingException {
		this(martServiceServer, virtualSchemaName, datasetConfigVersion, formatter, count, header, unique, 
				limitStart, limitSize, new RestFulQueryDataset[]{dataset1, dataset2});
	}
	public RestFulQuery(String datasetConfigVersion, Boolean count, RestFulQueryDataset... datasets) throws UnsupportedEncodingException {
		this(MartServiceConstants.CENTRAL_PORTAL_MART_SERVICE_STRING_URL, "default", datasetConfigVersion, "TSV", count, false, false, null, null, datasets);
	}
	public RestFulQuery(String martServiceServer, String virtualSchemaName, String datasetConfigVersion, String formatter, Boolean count, Boolean header, Boolean unique, 
			Integer limitStart, Integer limitSize, RestFulQueryDataset[] datasets) throws UnsupportedEncodingException {
		super();
		this.martServiceServer = martServiceServer;
		this.virtualSchemaName = virtualSchemaName;
		this.datasetConfigVersion = datasetConfigVersion;
		this.formatter = formatter;
		this.count = count;
		this.header = header;
		this.unique = unique;
		this.limitStart = limitStart;
		this.limitSize = limitSize;
		this.datasetList = new ArrayList<RestFulQueryDataset>();
		for (RestFulQueryDataset dataset : datasets) {
			this.datasetList.add(dataset);
		}	
		this.buildQuery();		//TODO seperate

		// Check for an empty IN list
		/*checkEmptyInList(datasets);*/
	}

	/*public void checkEmptyInList(RestFulQueryDataset... datasets) {
		this.emptyInList = false;
		for (RestFulQueryDataset dataset : datasets) {
			for (Filter filter : dataset.filtersList) {
				if (filter.value==null || filter.value.length()==0) {
					this.emptyInList = true;
					break;
				}
			}
			if (this.emptyInList) {
				break;
			}
		}
	}*/
	
	public void buildQuery() throws UnsupportedEncodingException {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE Query><Query ");
		stringBuffer.append("virtualSchemaName=\"" + this.virtualSchemaName + "\" ");
		stringBuffer.append("formatter=\"" + this.formatter + "\" ");
		stringBuffer.append("header=\"" + MartConfiguratorUtils.booleanToBinaryDigit(this.header) + "\" ");
		stringBuffer.append("uniqueRows=\"" + MartConfiguratorUtils.booleanToBinaryDigit(this.unique) + "\" ");
		stringBuffer.append("count=\"" + MartConfiguratorUtils.booleanToBinaryDigit(this.count) + "\" ");
		if (this.limitSize!=null) {
			if (this.limitStart!=null) {
				stringBuffer.append("limitStart=\"" + this.limitStart + "\" ");
			}
			stringBuffer.append("limitSize=\"" + this.limitSize + "\" ");
		}
		stringBuffer.append("datasetConfigVersion=\"" + this.datasetConfigVersion + "\" >");
		
		for (RestFulQueryDataset dataset : datasetList) {
			stringBuffer.append("<Dataset name=\"" + dataset.datasetName + "\" interface=\"default\" >");
			if (dataset.attributesList!=null) {
				for (int i = 0; i < dataset.attributesList.size(); i++) {
					stringBuffer.append("<Attribute name=\"" + dataset.attributesList.get(i).internalName.toLowerCase() + "\" />");
									// toLowerCase because of rule that says so
				}
			}
			if (dataset.filtersList!=null) {
				for (int i = 0; i < dataset.filtersList.size(); i++) {		// To do an IN: <Filter name = "ensembl_gene_id" value = "152,3516,1563"/>
					StringBuffer filtervalue = dataset.filtersList.get(i).value;
					StringBuffer value = new StringBuffer(filtervalue!=null && filtervalue.length()>0 ? filtervalue : MartServiceConstants.EMPTY_IN_LIST_STRING);
					stringBuffer.append("<Filter name=\"" + dataset.filtersList.get(i).internalName.toLowerCase() + 
							"\" value=\"" + value + "\"/>");
									// toLowerCase because of rule that says so
				}
			}
			stringBuffer.append("</Dataset>");
		}
		
		stringBuffer.append("</Query>");
		this.query = stringBuffer.toString();
		this.queryHtmlGet = this.martServiceServer + MyConstants.URL_PARAMETERS_SEPARATOR + getQueryHtmlPostData();
	}
	
	public String toShortString() {
		
		StringBuffer datasetSb = new StringBuffer();
		for (int i = 0; i < datasetList.size(); i++) {
			datasetSb.append(MyUtils.TAB_SEPARATOR + datasetList.get(i).toShortString() + MyUtils.LINE_SEPARATOR);
		}
		return "martServiceServer	= " + 	martServiceServer + 
		", virtualSchemaName = " + 	virtualSchemaName + MyUtils.LINE_SEPARATOR +		
		MyUtils.TAB_SEPARATOR + "datasetConfigVersion = " + datasetConfigVersion + 
		", formatter = " + 	formatter + 
		", count = " + 	count + 
		", header = " + 	header + 
		", unique = " + 	unique + 
		", limitStart = " + 	limitStart + 
		", limitSize = " + 	limitSize + 
		/*", emptyInList = " + 	emptyInList +*/ 
		MyUtils.LINE_SEPARATOR +
		MyUtils.TAB_SEPARATOR + datasetSb.toString() + MyUtils.LINE_SEPARATOR +
		MyUtils.TAB_SEPARATOR + "query = " + query + MyUtils.LINE_SEPARATOR +  
		MyUtils.TAB_SEPARATOR + "queryHtmlGet = " + queryHtmlGet + MyUtils.LINE_SEPARATOR +  
		getReadableUrl() + MyUtils.LINE_SEPARATOR;
	}
	public String getQueryHtmlPostData() throws UnsupportedEncodingException {
		return URLEncoder.encode(MartServiceConstants.URL_QUERY_PARAMETER, "UTF-8") + MyConstants.URL_KEY_VALUE_SEPARATOR + 
		URLEncoder.encode(this.query, "UTF-8");
	}
	public String getReadableUrl() {
		return this.martServiceServer + MyConstants.URL_PARAMETERS_SEPARATOR + MartServiceConstants.URL_QUERY_PARAMETER + MyConstants.URL_KEY_VALUE_SEPARATOR + this.query;
	}
	public String getShortenedReadableUrl() {
		String shortQuery = null;
		int length = this.query.length();
		if (length>1000) {
			shortQuery = this.query.substring(0, 500) + " ... " + this.query.substring(length-500, length);
		} else {
			shortQuery = this.query;
		}
		return this.martServiceServer + MyConstants.URL_PARAMETERS_SEPARATOR + MartServiceConstants.URL_QUERY_PARAMETER + MyConstants.URL_KEY_VALUE_SEPARATOR + shortQuery;
	}
	public String getQueryHtmlGet() {
		return queryHtmlGet;
	}
	public URLConnection getUrlConnectionPost() throws MalformedURLException, IOException, UnsupportedEncodingException {
		URL url = new URL(this.martServiceServer);
        URLConnection urlConnection = url.openConnection();
        urlConnection.setDoOutput(true);
        urlConnection.setUseCaches (false);
        OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
        writer.write(this.getQueryHtmlPostData());
        writer.close();
		return urlConnection;
	}
	public URL getUrlGet() throws MalformedURLException, IOException {
		return new URL(this.queryHtmlGet);
	}
	/*public List<String> resultsToListString() throws IOException, SQLException {
		// No results if nothing in the IN list (not handled by martservice: will consider no filtering)
		if (this.emptyInList) {
			return new ArrayList<String>();
		}
		
		URLConnection url = getUrlConnectionPost();
		
		InputStreamReader inputStreamReader = new InputStreamReader(url.getInputStream());
		BufferedReader in = new BufferedReader(inputStreamReader);
		
		List<String> listData = new ArrayList<String>();
		String line = null;
		String errorMessage = null;
		while ((line = in.readLine()) != null) {
			if (line.startsWith(MartServiceConstants.ERROR_MESSAGE)) {
				throw new TechnicalException(line);
			}
			if (!line.isEmpty()) {
				listData.add(line);
			}
		}
	      
		return listData;
	}*/
	public String getFirstRow() throws IOException {
		URLConnection urlConnection = getUrlConnectionPost();
		InputStreamReader inputStreamReader = new InputStreamReader(urlConnection.getInputStream());
		BufferedReader in = new BufferedReader(inputStreamReader);
		String firstRow = in.readLine();
		in.close();
		inputStreamReader.close();
		return firstRow;
	}
	
	@Deprecated	// would be better if we could use resultsToListString() too, to look into it
	public Map<String, List<String>> getMapData (boolean display) throws MalformedURLException, IOException, SQLException {
		if (display) {
			System.out.println(this.query + MyUtils.LINE_SEPARATOR + this.queryHtmlGet);
		}
		URLConnection url = this.getUrlConnectionPost();
        
		Map<String, List<String>> mapData = null;
		
		List<String> attributeNamesList = new ArrayList<String>();
		for (RestFulQueryDataset dataset : datasetList) {
			attributeNamesList.addAll(dataset.getAttributeNamesList());
		}
		mapData = copyUrlContentToMapStringList(url, MyUtils.TAB_SEPARATOR, attributeNamesList);
		return mapData;
	}
	@Deprecated	// would be better if we could use resultsToListString() too, to look into it
	public Map<String, List<String>> copyUrlContentToMapStringList(URLConnection url, String separator, List<String> fieldNames) throws IOException, SQLException {
		BufferedReader in = new BufferedReader(new InputStreamReader(url.getInputStream()));
		
		Map<String, List<String>> mapData = new HashMap<String, List<String>>();
		for (String fieldName : fieldNames) {
			mapData.put(fieldName, new ArrayList<String>());
		}
		
		String line = null;
		boolean first = true;
		while ((line = in.readLine()) != null) {
			if (!MyUtils.isEmpty(line)) {
				if (first) {
					if (line.contains(MartServiceConstants.ERROR_MESSAGE)) {
						throw new SQLException(line);
					}
					first = false;
				}
				String [] split = line.split(separator);
				for (int i = 0; i < split.length; i++) {	// fieldNames must be ordered the same way line is
					mapData.get(fieldNames.get(i)).add(split[i]);
				}
			}
		}
		in.close();
		return mapData;
	}
	@Deprecated	// doesn't handle timeout
	public static int urlContentToFile(URL url, File file, boolean noEmptyLines) throws IOException, TechnicalException {
		InputStreamReader inputStreamReader = new InputStreamReader(url.openStream());
		BufferedReader in = new BufferedReader(inputStreamReader);
		FileWriter fileWriter = new FileWriter(file);
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		String line = null;
		int totalLines = 0;
		boolean error = false;
		try {
			while ((line = in.readLine()) != null) {
				if (line.startsWith(MartServiceConstants.ERROR_MESSAGE)) {
					throw new TechnicalException(line);
				}
				if (!noEmptyLines || !MyUtils.isEmpty(line)) {
					bufferedWriter.write(line + MyUtils.LINE_SEPARATOR);
					totalLines++;
				}
			}
		} catch (TechnicalException e) {
			error = true;
		} finally {
			bufferedWriter.close();
			fileWriter.close();
			in.close();
			inputStreamReader.close();
			if (error) {
				file.delete();
				file.createNewFile();
				throw new TechnicalException(line);
			}
		}
		return totalLines;
	}
	
    
    /*
      	HTTP/1.1 200 OK
		Date: Wed, 17 Jun 2009 21:32:41 GMT
		Server: Apache/2.2.3 (Debian) mod_perl/2.0.2 Perl/v5.8.8
		Connection: close
		Content-Type: text/plain
		(empty line)
    */
    public static List<String> PROTOCOLS = new ArrayList<String>(Arrays.asList(new String[] {
    		"HTTP/1.1",
			"Date:",
			"Server:",
			"Connection:",
			"Content-Type:",
			"Content-Length:"
    }));
	/**
	 * Taken from http://www.cafeaulait.org/slides/sd2003west/sockets/Java_Socket_Programming.html
		String urlString = "http://bm-test.res.oicr.on.ca:9061/biomart/martservice?query=%3C?xml%20version=%221.0%22%20encoding=%22UTF-8%22?%3E%3C!DOCTYPE%20Query%3E%3CQuery%20virtualSchemaName=%22default%22%20formatter=%22TSV%22%20header=%220%22%20uniqueRows=%220%22%20count=%220%22%20datasetConfigVersion=%220.6%22%20%3E%3CDataset%20name=%22oglaberrima_gene_ensembl%22%20interface=%22default%22%20%3E%3CAttribute%20name=%22ensembl_gene_id%22%20/%3E%3C/Dataset%3E%3C/Query%3E";
		File file = new File("/home/anthony/Desktop/zzyy");
		urlContentToFile(urlString, file);
	 * @param urlString
	 */
	public Integer urlContentToFile(String urlString, File file, Integer timeOut) throws TechnicalException, FunctionalException {
	    
		/*// No results if nothing in the IN list (not handled by martservice: will consider no filtering)
		if (this.emptyInList) {
			return new ArrayList<String>();
		}*/
		
	      int port = 80;
	      String fileSeparator = "/";

		String errorMessage = null;
		Socket socket = null;
		OutputStream theOutput = null;
		PrintWriter pw = null;
		InputStream in = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		FileWriter fileWriter = null;
	    BufferedWriter bufferedWriter = null;
	    int totalLines = 0;
		
		try {
	      fileWriter = new FileWriter(file);
	      bufferedWriter = new BufferedWriter(fileWriter);
	      
	      
	        URL u = new URL(urlString);
	        if (u.getPort() != -1) port = u.getPort();
	        if (!(u.getProtocol().equalsIgnoreCase("http"))) {
	          throw new FunctionalException("I only understand http.");
	        }
	        if (!(u.getFile().equals(""))) fileSeparator = u.getFile();
	        socket = new Socket(u.getHost(), port);
	        if (null!=timeOut) {
	        	socket.setSoTimeout(timeOut);		// Time out!
	        }
	        theOutput = socket.getOutputStream();
	        pw = new PrintWriter(theOutput, false);
	        pw.println("GET " + fileSeparator + " HTTP/1.0");
	        pw.println("Accept: text/plain, text/html, text/*");
	        pw.println();
	        pw.flush();
	        
	        in = socket.getInputStream();
	        isr = new InputStreamReader(in);
	        br = new BufferedReader(isr/*, "ASCII"*/);

		    List<String> prot = new ArrayList<String>();
	        String line;
	        while ((line = br.readLine()) != null) {
	        	if (MyUtils.isEmpty(line)) {
	        		break;
	        	}
	        }
	        for (String p : prot) {
	        	boolean belongs = false;
	        	for (String p2 : PROTOCOLS) {
	        		if (p.startsWith(p2)) {
	        			belongs=true;
	        			break;
	        		}
	        	}
	        	MyUtils.checkStatusProgram(belongs, "belongs, p = " + p, true);
	        }
	        	  
	        while ((line = br.readLine()) != null) {
				if (line.startsWith(MartServiceConstants.ERROR_MESSAGE)) {
					throw new TechnicalException(line);
				}
	        	if (!MyUtils.isEmpty(line)) {
		          //System.out.println(theLine);
		          bufferedWriter.write(line + MyUtils.LINE_SEPARATOR);
		          totalLines++;
	        	}
	        }
	      } catch (java.net.ConnectException e) {
	    	  errorMessage = e.getMessage();
	      } catch (MalformedURLException e) {
	    	  //System.out.println(urlString + " is not a valid URL");
	    	  errorMessage = e.getMessage();
	    	  //throw new FunctionalException(e);
	      }  catch (SocketTimeoutException e) {
		      //System.out.println(ex);
	    	  errorMessage = e.getMessage();
		  } catch (TechnicalException e) {
		        //System.out.println(ex);
		    	errorMessage = e.getMessage();
		  } catch (Exception e) {
		      //System.out.println(e);
	    	  errorMessage = e.getMessage();
	    	  //throw new FunctionalException(e);
	      } finally {
	    	  try {
	    		  if (br!=null) br.close();
	    		  if (isr!=null) isr.close();
	    		  if (in!=null) in.close();
	    		  if (pw!=null) pw.close();
	    		  if (theOutput!=null) theOutput.close();
	    		  if (socket!=null) socket.close();
				    
				  if (errorMessage!=null) {
					  // Erase and mark as error
					  bufferedWriter.close();
					  fileWriter.close();
					  file.delete();
				      fileWriter = new FileWriter(file);
				      bufferedWriter = new BufferedWriter(fileWriter);
					  bufferedWriter.write(MartServiceConstants.INVALID_FILE_ERROR_MESSAGE + MyUtils.LINE_SEPARATOR);
					  bufferedWriter.write(errorMessage + MyUtils.LINE_SEPARATOR);
				  }
				  
				  if (bufferedWriter!=null) bufferedWriter.close();
				  if (fileWriter!=null) fileWriter.close();
			      
				  if (errorMessage!=null) {
					  throw new TechnicalException(errorMessage);
				  }
			} catch (IOException e) {
				e.printStackTrace();
			}
	      }
	      
	      return totalLines;
	}
	
	public List<String> urlContentToStringList2(Integer timeOut) throws TechnicalException, FunctionalException {
	    
		String urlString = this.getQueryHtmlGet();
		
		/*// No results if nothing in the IN list (not handled by martservice: will consider no filtering)
		if (this.emptyInList) {
			return new ArrayList<String>();
		}*/
		
	      int port = 80;
	      String file = "/";

		String errorMessage = null;
		Socket socket = null;
		OutputStream theOutput = null;
		PrintWriter pw = null;
		InputStream in = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		List<String> list = new ArrayList<String>();
	    int totalLines = 0;
		
		try {
	      
	      
	        URL u = new URL(urlString);			
	        if (u.getPort() != -1) port = u.getPort();
	        if (!(u.getProtocol().equalsIgnoreCase("http"))) {
	          throw new FunctionalException("I only understand http.");
	        }
	        if (!(u.getFile().equals(""))) file = u.getFile();
	        socket = new Socket(u.getHost(), port);
	        if (null!=timeOut) {
	        	socket.setSoTimeout(timeOut);		// Time out!
	        }
	        theOutput = socket.getOutputStream();
	        pw = new PrintWriter(theOutput, false);
	        pw.println("GET " + file + " HTTP/1.0");
	        pw.println("Accept: text/plain, text/html, text/*");
	        pw.println();
	        pw.flush();
	        
	        in = socket.getInputStream();
	        isr = new InputStreamReader(in);
	        br = new BufferedReader(isr/*, "ASCII"*/);

		    List<String> prot = new ArrayList<String>();
	        String line;
	        while ((line = br.readLine()) != null) {
	        	if (MyUtils.isEmpty(line)) {
	        		break;
	        	}
	        }
	        for (String p : prot) {
	        	boolean belongs = false;
	        	for (String p2 : PROTOCOLS) {
	        		if (p.startsWith(p2)) {
	        			belongs=true;
	        			break;
	        		}
	        	}
	        	MyUtils.checkStatusProgram(belongs, "belongs, p = " + p, true);
	        }
	        	  
	        while ((line = br.readLine()) != null) {
				if (line.startsWith(MartServiceConstants.ERROR_MESSAGE)) {
					throw new TechnicalException(line);
				}
	        	if (!MyUtils.isEmpty(line)) {
		          //System.out.println(theLine);
	        	  list.add(line);
		          totalLines++;
	        	}
	        }
		} catch (java.net.ConnectException e) {
		  errorMessage = e.getMessage();
		} catch (MalformedURLException e) {
		  //System.out.println(urlString + " is not a valid URL");
		  errorMessage = e.getMessage();
		  //throw new FunctionalException(e);
		}  catch (SocketTimeoutException e) {
		      //System.out.println(ex);
			errorMessage = e.getMessage();
		  } catch (TechnicalException e) {
		        //System.out.println(ex);
		    	errorMessage = e.getMessage();
		  } catch (Exception e) {
		      //System.out.println(e);
		  errorMessage = e.getMessage();
		  //throw new FunctionalException(e);
		} finally {
	    	  try {
	    		  if (br!=null) br.close();
	    		  if (isr!=null) isr.close();
	    		  if (in!=null) in.close();
	    		  if (pw!=null) pw.close();
	    		  if (theOutput!=null) theOutput.close();
	    		  if (socket!=null) socket.close();
				    
				  if (errorMessage!=null) {
					  // Erase and mark as error
					  list = new ArrayList<String>();
					  list.add(MartServiceConstants.INVALID_FILE_ERROR_MESSAGE);
					  
					  throw new TechnicalException(errorMessage);
				  }
			} catch (IOException e) {
				e.printStackTrace();
			}
	      }
	      
	      return list;
	}
	
	@Deprecated	// not generic at all!! use this method but make it generic
	public List<String> urlContentToStringList(Integer timeOut) throws TechnicalException, FunctionalException {
	    
		//String urlString = this.getQueryHtmlPostData();
		//this.queryHtmlGet = this.martServiceServer + MyConstants.URL_PARAMETER_SEPARATOR + getQueryHtmlPostData();
		String urlString = this.martServiceServer;
			
		/*// No results if nothing in the IN list (not handled by martservice: will consider no filtering)
		if (this.emptyInList) {
			return new ArrayList<String>();
		}*/
		
	      int port = 80;
	      String file = "/";

		String errorMessage = null;
		Socket socket = null;
		OutputStream theOutput = null;
		PrintWriter pw = null;
		InputStream in = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		List<String> list = new ArrayList<String>();
	    int totalLines = 0;
		
		try {
	urlString = MyConstants.HTTP_PROTOCOL + MyConstants.BMTEST_SERVER;
			
	      
	        URL u = new URL(urlString);			
	        if (u.getPort() != -1) port = u.getPort();
	        if (!(u.getProtocol().equalsIgnoreCase("http"))) {
	          throw new FunctionalException("I only understand http.");
	        }
	        if (!(u.getFile().equals(""))) file = u.getFile();
	        socket = new Socket(u.getHost(), port);
	        if (null!=timeOut) {
	        	socket.setSoTimeout(timeOut);		// Time out!
	        }
	        theOutput = socket.getOutputStream();
	        String data = this.getQueryHtmlPostData();
	        String path = MartServiceConstants.DEFAULT_PATH_TO_MART_SERVICE;
	        pw = new PrintWriter(new OutputStreamWriter(theOutput, "UTF8"));
	        pw.println("POST " + path + " HTTP/1.0");
	        pw.println("Content-Length: "+data.length()+"");
	        pw.println("Content-Type: application/x-www-form-urlencoded");
	        pw.println("");
	    
	        // Send data
	        pw.print(data);
	        pw.flush();
	
/*//	      Send header
	        String path = "/servlet/SomeServlet";
	        BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
	        wr.write("POST "+path+" HTTP/1.0\r\n");
	        wr.write("Content-Length: "+data.length()+"\r\n");
	        wr.write("Content-Type: application/x-www-form-urlencoded\r\n");
	        wr.write("\r\n");
	    
	        // Send data
	        wr.write(data);
	        wr.flush();*/

	        
	        
	        
	        
	        in = socket.getInputStream();
	        isr = new InputStreamReader(in);
	        br = new BufferedReader(isr/*, "ASCII"*/);

		    List<String> prot = new ArrayList<String>();
	        String line;
	        while ((line = br.readLine()) != null) {
	        	if (MyUtils.isEmpty(line)) {
	        		break;
	        	}
	        }
	        for (String p : prot) {
	        	boolean belongs = false;
	        	for (String p2 : PROTOCOLS) {
	        		if (p.startsWith(p2)) {
	        			belongs=true;
	        			break;
	        		}
	        	}
	        	MyUtils.checkStatusProgram(belongs, "belongs, p = " + p, true);
	        }
	        	  
	        while ((line = br.readLine()) != null) {
				if (line.startsWith(MartServiceConstants.ERROR_MESSAGE)) {
					throw new TechnicalException(line);
				}
	        	if (!MyUtils.isEmpty(line)) {
		          //System.out.println(theLine);
	        	  list.add(line);
		          totalLines++;
	        	}
	        }
		} catch (java.net.ConnectException e) {
		  errorMessage = e.getMessage();
		} catch (MalformedURLException e) {
		  //System.out.println(urlString + " is not a valid URL");
		  errorMessage = e.getMessage();
		  //throw new FunctionalException(e);
		}  catch (SocketTimeoutException e) {
		      //System.out.println(ex);
			errorMessage = e.getMessage();
		  } catch (TechnicalException e) {
		        //System.out.println(ex);
		    	errorMessage = e.getMessage();
		  } catch (Exception e) {
		      //System.out.println(e);
		  errorMessage = e.getMessage();
		  //throw new FunctionalException(e);
		} finally {
	    	  try {
	    		  if (br!=null) br.close();
	    		  if (isr!=null) isr.close();
	    		  if (in!=null) in.close();
	    		  if (pw!=null) pw.close();
	    		  if (theOutput!=null) theOutput.close();
	    		  if (socket!=null) socket.close();
				    
				  if (errorMessage!=null) {
					  // Erase and mark as error
					  list = new ArrayList<String>();
					  list.add(MartServiceConstants.INVALID_FILE_ERROR_MESSAGE);
					  
					  throw new TechnicalException(errorMessage);
				  }
			} catch (IOException e) {
				e.printStackTrace();
			}
	      }
	      
	      return list;
	}
	
	@Deprecated
	public String firstLineToString(Integer timeOut) throws TechnicalException, FunctionalException {
	       
		/*// No results if nothing in the IN list (not handled by martservice: will consider no filtering)
		if (this.emptyInList) {
			return null;
		}*/
		
	      int port = 80;
	      String file = "/";

		String errorMessage = null;
		Socket socket = null;
		OutputStream theOutput = null;
		PrintWriter pw = null;
		InputStream in = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
	    int totalLines = 0;
	    String firstLine = null;
	    
		try {
	      
	      
	        URL u = new URL(this.queryHtmlGet);
	        if (u.getPort() != -1) port = u.getPort();
	        if (!(u.getProtocol().equalsIgnoreCase("http"))) {
	          throw new FunctionalException("I only understand http.");
	        }
	        if (!(u.getFile().equals(""))) file = u.getFile();
	        socket = new Socket(u.getHost(), port);
	        if (null!=timeOut) {
	        	socket.setSoTimeout(timeOut);		// Time out!
	        }
	        theOutput = socket.getOutputStream();
	        pw = new PrintWriter(theOutput, false);
	        pw.println("GET " + file + " HTTP/1.0");
	        pw.println("Accept: text/plain, text/html, text/*");
	        pw.println();
	        pw.flush();
	        
	        in = socket.getInputStream();
	        isr = new InputStreamReader(in);
	        br = new BufferedReader(isr/*, "ASCII"*/);

		    List<String> prot = new ArrayList<String>();
	        String line;
	        while ((line = br.readLine()) != null) {
	        	if (MyUtils.isEmpty(line)) {
	        		break;
	        	}
	        }
	        for (String p : prot) {
	        	boolean belongs = false;
	        	for (String p2 : PROTOCOLS) {
	        		if (p.startsWith(p2)) {
	        			belongs=true;
	        			break;
	        		}
	        	}
	        	MyUtils.checkStatusProgram(belongs, "belongs, p = " + p, true);
	        }
	       
	        firstLine = br.readLine();
			if (line.startsWith(MartServiceConstants.ERROR_MESSAGE)) {
				throw new TechnicalException(line);
			}
        	if (!MyUtils.isEmpty(line)) {
	          //System.out.println(theLine);
	          totalLines++;
        	}
	      
		} catch (java.net.ConnectException e) {
		  errorMessage = e.getMessage();
		} catch (MalformedURLException e) {
		  //System.out.println(urlString + " is not a valid URL");
		  errorMessage = e.getMessage();
		  //throw new FunctionalException(e);
		}  catch (SocketTimeoutException e) {
		      //System.out.println(ex);
		  errorMessage = e.getMessage();
		  } catch (TechnicalException e) {
		        //System.out.println(ex);
		    	errorMessage = e.getMessage();
		  } catch (Exception e) {
		      //System.out.println(e);
		  errorMessage = e.getMessage();
		  //throw new FunctionalException(e);
		} finally {
	    	  try {
	    		  if (br!=null) br.close();
	    		  if (isr!=null) isr.close();
	    		  if (in!=null) in.close();
	    		  if (pw!=null) pw.close();
	    		  if (theOutput!=null) theOutput.close();
	    		  if (socket!=null) socket.close();
				    				 
				  if (errorMessage!=null) {
					  firstLine = errorMessage;
					  throw new TechnicalException(errorMessage);
				  }
			} catch (IOException e) {
				e.printStackTrace();
			}
	      }
	      
	      return firstLine;
	}
	
	
	public Boolean getCount() {
		return count;
	}

	public void setCount(Boolean count) {
		this.count = count;
	}
}
