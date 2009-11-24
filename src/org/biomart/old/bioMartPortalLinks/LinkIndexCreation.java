package org.biomart.old.bioMartPortalLinks;


import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.common.general.utils.CollectionsUtils;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.common.general.utils.Timer;
import org.biomart.old.martService.MartServiceConstants;
import org.biomart.old.martService.restFulQueries.RestFulQuery;
import org.biomart.test.linkIndicesTest.LinkIndexesUtils;


public class LinkIndexCreation {
	
	//private static final Boolean UNIQUE = false;
	
	public RestFulQuery martServiceRestFulQuery = null;
	private TreeSet<String> treeSetLeft = null;
	private TreeSet<String> treeSetRight = null;
	private TreeSet<String> treeSetLinkIndex = null;
	
	public LinkIndexCreation() {}	
	public LinkIndexCreation(RestFulQuery martServiceRestFulQuery) {
		this.martServiceRestFulQuery = martServiceRestFulQuery;
	}
	
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		
		/*try {
			RestFulQuery martServiceRestFul1 = 
					new RestFulQuery(MartServiceConstants.BMTEST_MART_SERVICE_STRING_URL, "pancreas_expression_db", "0.5", 
							MartServiceConstants.DEFAULT_FORMATTER, false, false, UNIQUE, null, null,		// unique?
							new RestFulQueryDataset("scerevisiae_gene_ensembl", new ArrayList<Attribute>(Arrays.asList(new Attribute[] {new Attribute("uniprot_swissprot_accession")})), null));
			
			RestFulQuery martServiceRestFul2 = 
					new RestFulQuery(MartServiceConstants.BMTEST_MART_SERVICE_STRING_URL, "pancreas_expression_db", "0.5", 
							MartServiceConstants.DEFAULT_FORMATTER, false, false, UNIQUE, null, null,		// unique?
							new RestFulQueryDataset("olatipes_gene_ensembl", new ArrayList<Attribute>(Arrays.asList(new Attribute[] {new Attribute("uniprot_swissprot_accession")})), null));	
			
			RestFulQuery martServiceRestFul3 = 
					new RestFulQuery(MartServiceConstants.BMTEST_MART_SERVICE_STRING_URL, "default", "0.6", 
							MartServiceConstants.DEFAULT_FORMATTER, false, false, false, null, null,		// unique?
							new RestFulQueryDataset("interaction", new ArrayList<Attribute>(Arrays.asList(new Attribute[] {new Attribute("id_complex_db_id__dm_value")})), null));
			
			RestFulQuery martServiceRestFul4 = 
					new RestFulQuery(MartServiceConstants.BMTEST_MART_SERVICE_STRING_URL, "default", "0.6", 
							MartServiceConstants.DEFAULT_FORMATTER, false, false, false, null, null,		// unique?
							new RestFulQueryDataset("complex", new ArrayList<Attribute>(Arrays.asList(new Attribute[] {new Attribute("complex_db_id_key")})), null));
			
			Timer timer = new Timer();
			timer.startTimer();
			
			timer.stopTimer();
			System.out.println(timer.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}*/
	}
	
	public boolean createDataFile(int exportableDataTmpCount, PortableData exportableData, String folder, boolean reUseFiles, int sleepLength, Integer timeOut) throws FunctionalException, TechnicalException, MalformedURLException, IOException, UnsupportedEncodingException, InterruptedException {
		
		String filePathAndName = folder + exportableData.getFileName();
		File createIndextemporaryFile = new File(filePathAndName);
		String readableUrl = martServiceRestFulQuery.getReadableUrl();
		String errorMessage = null;
		boolean created = true;
		if (!reUseFiles || (reUseFiles && !createIndextemporaryFile.exists())) {
			Integer totalLines = null;
			Timer timer = new Timer();			
			timer.startTimer();
			System.out.println(exportableDataTmpCount + " - writting file " + createIndextemporaryFile.getName() + " from: " + readableUrl);
			try {
				totalLines = martServiceRestFulQuery.urlContentToFile(martServiceRestFulQuery.getQueryHtmlGet(), createIndextemporaryFile, timeOut);/*totalLines = RestFulQuery.urlContentToFile(martServiceRestFulQuery.getUrlGet(), createIndextemporaryFile, true);*/
			} catch (TechnicalException e) {
				errorMessage = e.getMessage();
				exportableData.setErrorMessage(errorMessage);
			}
			timer.stopTimer();
			Thread.sleep(sleepLength);	// So we don't overload the server
			exportableData.setTotalRows(totalLines);
			exportableData.setUrlString(readableUrl);
			exportableData.setTimer(timer);
		} else {
			System.out.println(exportableDataTmpCount + " - reusing file " + createIndextemporaryFile.getName() + " from: " + readableUrl);
			errorMessage = LinkIndexesUtils.checkForErrors(createIndextemporaryFile);
			exportableData.setErrorMessage(errorMessage);
			created = false;
		}
		
		/*createIndextemporaryFile.deleteOnExit();*/
		exportableData.setFileSize(createIndextemporaryFile.length());
		
		if (errorMessage!=null) {
			throw new TechnicalException(errorMessage);
		}
		
		return created;
	}
	
	public boolean createLinkIndexUsingFileSystemAndJavaMemory(Link link, String temporaryFileFolder,
			String linkIndexFolder, boolean reUseFiles, Boolean doLeft, Boolean doRight) 
			throws MalformedURLException, IOException, UnsupportedEncodingException, InterruptedException, FunctionalException {
		boolean created = true;
		
		Timer timer = new Timer();
		timer.startTimer();

		PortableData leftExportableData = link.left.getPortableData();
		PortableData rightExportableData = link.right.getPortableData();
		
		//String fileName = ExportableData.createLinkIndexFileName(leftExportableData.getFileName(), rightExportableData.getFileName());
		String linkIndexFilePathAndName = link.createLinkIndexFilePathAndName(linkIndexFolder);
		File linkIndexfile = new File(linkIndexFilePathAndName);
		boolean error = false;
		
		if (!reUseFiles || (reUseFiles && !linkIndexfile.exists())) {

			String errorMessageLeft = null;
			String errorMessageRight = null;
			System.out.println ("link index file " + linkIndexfile.getName() + " does not exist yet, creating it");
			
			if (doLeft==null || doLeft==Boolean.TRUE || treeSetLeft==null) {	// in case reusing left but previous link index already existed
				System.out.println("reading left side file");
				File leftFile = new File(leftExportableData.temporaryFileFolder + leftExportableData.getFileName());
				if ((errorMessageLeft = LinkIndexesUtils.checkForErrors(leftFile))==null) {
					treeSetLeft = MyUtils.getDataFromFileToTreeSet(leftFile, MyUtils.TAB_SEPARATOR);			
					leftExportableData.setTotalRows(treeSetLeft.size());
				} else {
					System.out.println("errorMessageLeft = " + errorMessageLeft);
				}
			} else {
				MyUtils.checkStatusProgram(
						treeSetLeft!=null && treeSetLeft.size()==leftExportableData.getTotalRows(), "treeSetLeft.size()==link.left.getTotalRow(), " +
						"treeSetLeft.size() = " + (treeSetLeft!=null ? treeSetLeft.size() : treeSetLeft) + ", link.left.getTotalRow() = " + leftExportableData.getTotalRows(), true);
				System.out.println("reusing memory for left side: " + treeSetLeft.size() + " elements (" + leftExportableData.getTotalRows() + ")");
				leftExportableData.setTotalRows(treeSetLeft.size());
			}
						
			if (doRight==null || doRight==Boolean.TRUE || treeSetRight==null) {
				System.out.println("reading right side file");
				File rightFile = new File(rightExportableData.temporaryFileFolder + rightExportableData.getFileName());
				if ((errorMessageRight = LinkIndexesUtils.checkForErrors(rightFile))==null) {
					treeSetRight = MyUtils.getDataFromFileToTreeSet(rightFile, MyUtils.TAB_SEPARATOR);	
					rightExportableData.setTotalRows(treeSetRight.size());	
				} else {
					System.out.println("errorMessageRight = " + errorMessageRight);
				}
			} else {
				MyUtils.checkStatusProgram(treeSetRight.size()==rightExportableData.getTotalRows(), "listData2.size()==link.right.getTotalRow(), " +
						"listData2.size() = " + treeSetRight.size() + ", link.right.getTotalRow() = " + rightExportableData.getTotalRows(), true);
				System.out.println("reusing memory for right side: " + treeSetRight.size() + " elements (" + rightExportableData.getTotalRows() + ")");
				rightExportableData.setTotalRows(treeSetRight.size());
			}
						
			// Merging
			error = errorMessageLeft!=null || errorMessageRight!=null;
			if (!error) {
				System.out.println("merging " + treeSetLeft.size() + "x" + treeSetRight.size() + " rows");
				treeSetLinkIndex = CollectionsUtils.mergeTreeSetString(treeSetLeft, treeSetRight);
				link.setTotalRows(treeSetLinkIndex.size());
				System.out.println ("writting valid link index file " + linkIndexfile.getName() + " (" + link.getTotalRows() + " rows)");
				MyUtils.writeCollectionToFile(treeSetLinkIndex, linkIndexfile);
			} else {
				System.out.println ("writting erroneous link index file " + linkIndexfile.getName());	
				String content = MartServiceConstants.INVALID_FILE_ERROR_MESSAGE + MyUtils.LINE_SEPARATOR + 
						errorMessageLeft + MyUtils.LINE_SEPARATOR + errorMessageRight + MyUtils.LINE_SEPARATOR;
				MyUtils.writeFile(linkIndexFilePathAndName, content);
			}			
		} else {
			treeSetLinkIndex = MyUtils.getDataFromFileToTreeSet(linkIndexfile, MyUtils.TAB_SEPARATOR);	
			link.setTotalRows(treeSetLinkIndex.size());
			System.out.println ("link index file " + linkIndexfile.getName() + " already exists with (" + link.getTotalRows() + " rows)");
			created = false;
		}
		
		timer.stopTimer();
		if (!error) {
			link.setTimer(timer);
		}
		link.setFileSize(linkIndexfile.length());
		link.setFile(linkIndexfile);
		link.setFilePathAndName(linkIndexFilePathAndName);
		
		return created;
	}	
	
	public String createDataSerial(String folder, String temporaryFilePrefix) throws MalformedURLException, IOException, TechnicalException {
		System.out.println(martServiceRestFulQuery.getReadableUrl());
		System.out.print("writting serial " + temporaryFilePrefix + " for: ");
		Set<List<String>> set = MyUtils.copyUrlContentToHashSetStringList(martServiceRestFulQuery.getUrlGet(), MyUtils.TAB_SEPARATOR);
		MyUtils.writeSerializedObject(set, folder + temporaryFilePrefix);
		return temporaryFilePrefix;
	}
	
	@SuppressWarnings("deprecation")
	public Integer createLinkIndexUsingLinuxUniq(RestFulQuery martServiceRestFulQuery1, RestFulQuery martServiceRestFulQuery2) 
	throws UnsupportedEncodingException, MalformedURLException, IOException, InterruptedException, FunctionalException, TechnicalException {
		
		// Check if file already exists
		//TODO
		
		File createIndextemporaryFile1 = 
			File.createTempFile("link_index", null);
			//new File(MyUtils.OUTPUT_FILES_PATH + "f1");
		File createIndextemporaryFile2 = 
			File.createTempFile("link_index", null);
			//new File(MyUtils.OUTPUT_FILES_PATH + "f2");
		
		System.out.println(MyUtils.TAB_SEPARATOR + "writting 1st file");
		System.out.println(martServiceRestFulQuery1.getReadableUrl());
		RestFulQuery.urlContentToFile(martServiceRestFulQuery1.getUrlGet(), createIndextemporaryFile1, true);
		
		System.out.println(MyUtils.TAB_SEPARATOR + "writting 2nd file");
		System.out.println(martServiceRestFulQuery2.getReadableUrl());
		RestFulQuery.urlContentToFile(martServiceRestFulQuery2.getUrlGet(), createIndextemporaryFile2, true);
		
		System.out.println(MyUtils.TAB_SEPARATOR + "writting intersection file");
		String resultFile = MyUtils.OUTPUT_FILES_PATH + "idx.txt";
		String command = "sort " + createIndextemporaryFile1.getAbsolutePath() + " " + createIndextemporaryFile2.getAbsolutePath() + " | uniq -d >" + resultFile;
		StringBuffer sb = MyUtils.runShCommand(command);
		if (sb.length()>0) {
			throw new FunctionalException("ERROR: " + sb);
		}
		
		createIndextemporaryFile1.delete();
		createIndextemporaryFile2.delete();
		
		StringBuffer totalLines = MyUtils.runShCommand("wc -l " + resultFile);
		new File(resultFile).delete();
		
		return Integer.valueOf(totalLines.toString().split(" ")[0]);	// output is like 45 xorg.conf.bak
	}
}
