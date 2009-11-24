package org.biomart.old.bioMartPortalLinks;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.common.general.utils.Timer;
import org.biomart.old.martService.MartServiceConstants;
import org.biomart.old.martService.restFulQueries.RestFulQuery;
import org.biomart.old.martService.restFulQueries.RestFulQueryDataset;
import org.biomart.old.martService.restFulQueries.objects.Attribute;
import org.biomart.old.martService.restFulQueries.objects.Filter;
import org.biomart.test.linkIndicesTest.LinkIndexesUtils;


public class LinkIndexTestCouple {

	public static void main(String[] args) throws Exception {
		LinkIndexTestCouple linkIndexTestCouple = new LinkIndexTestCouple();
		linkIndexTestCouple.initialize();
		linkIndexTestCouple.process();
	}

	private TreeSet<String> treeSet = null;
	private File file = null;
	private RestFulQuery leftQueryWithIndex = null;
	private RestFulQuery rightQueryWithIndex = null;
	private RestFulQuery leftQueryWithoutIndex = null;
	private RestFulQuery rightQueryWithoutIndex = null;
	@SuppressWarnings("unused")
	private Link link = null;
	private Boolean unique = null;
	private List<String> filterNamesList1 = null;
	private List<String> filterNamesList2 = null;
	private String linkIndexFilePathAndName = null;
	
	private Timer timer1 = null;
	private Timer timer2 = null;
	private Integer totalResults1 = null;
	private Integer totalResults2 = null;
	
	public LinkIndexTestCouple(Link link, String martServiceUrl, boolean unique) throws Exception {
		this.link = link;
		this.unique = unique;
		this.leftQueryWithIndex = link.left.createMartServiceRestFulQuery(martServiceUrl, this.unique);
		this.rightQueryWithIndex = link.right.createMartServiceRestFulQuery(martServiceUrl, this.unique);
		this.leftQueryWithoutIndex = link.left.createMartServiceRestFulQuery(martServiceUrl, this.unique);
		this.rightQueryWithoutIndex = link.right.createMartServiceRestFulQuery(martServiceUrl, this.unique);
		this.file = new File(link.getFilePathAndName());
		
		MyUtils.checkStatusProgram(link.left.importable.getElementsList().size()==1, "", true);	// for now
		MyUtils.checkStatusProgram(link.right.exportable.getCompleteFiltersList(), "", true);	// for now
		MyUtils.checkStatusProgram(link.right.exportable.getElementsList().size()==1, "", true);	// for now
		this.filterNamesList1  = link.left.importable.elementNamesList;
		this.filterNamesList2 = link.right.exportable.getFilterNamesList();
		
		this.linkIndexFilePathAndName = link.getFilePathAndName();
		
		initialize();
	}
	public LinkIndexTestCouple() throws Exception {
		createQueries();
	}
	public void createQueries() throws Exception {
		leftQueryWithIndex = new RestFulQuery(MartServiceConstants.BMTEST_MART_SERVICE_STRING_URL, "default", 
				MartServiceConstants.DEFAULT_BIOMART_VERSION, MartServiceConstants.DEFAULT_FORMATTER, false, false, false, null, null, 
				new RestFulQueryDataset("rgd_genes", new ArrayList<Attribute>(Arrays.asList(new Attribute[] {new Attribute("uniprot_acc_attr")})), null));
		
		rightQueryWithIndex = new RestFulQuery(MartServiceConstants.BMTEST_MART_SERVICE_STRING_URL, "default", 
				MartServiceConstants.DEFAULT_BIOMART_VERSION, MartServiceConstants.DEFAULT_FORMATTER, false, false, false, null, null, 
				new RestFulQueryDataset("reaction", new ArrayList<Attribute>(Arrays.asList(new Attribute[] {new Attribute("referencedatabase_uniprot")})), null));
		
		
		String FOLDER = "/home/anthony/javaIO/_OutputFiles/LinkIndexCreation/backup1/LinkIndexes/";
		this.file = new File(
				FOLDER + "default_#_reaction_#_referencedatabase_uniprot_#__##_default_#_rgd_genes_#_uniprot_acc_attr_#_");

		this.filterNamesList1 = new ArrayList<String>(Arrays.asList(new String[] {"unip_id_list"}));
		this.filterNamesList2 = new ArrayList<String>(Arrays.asList(new String[] {"referencepeptidesequence_uniprot_id_list"}));
		
		this.linkIndexFilePathAndName = this.file.getAbsolutePath();
		
		initialize();
	}
	
	private void initialize() throws Exception {
		leftQueryWithIndex.buildQuery();
		rightQueryWithIndex.buildQuery();
		leftQueryWithoutIndex.buildQuery();
		rightQueryWithoutIndex.buildQuery();
		
		System.out.println("link index = " + this.linkIndexFilePathAndName);
	}
	
	public void process() throws TechnicalException, Exception {
		
		System.out.println("running 1: " + leftQueryWithoutIndex.getShortenedReadableUrl());
		List<String> results1 = leftQueryWithoutIndex.urlContentToStringList(BioMartPortalLinks.MART_SERVICE_TIMEOUT);
		LinkIndexesUtils.checkForErrorsAndThrowException(results1);
		System.out.println("running 2: " + rightQueryWithoutIndex.getShortenedReadableUrl());
		List<String> results2 = rightQueryWithoutIndex.urlContentToStringList(BioMartPortalLinks.MART_SERVICE_TIMEOUT);
		LinkIndexesUtils.checkForErrorsAndThrowException(results2);
		displayResultStatistics(results1, "results1");
		displayResultStatistics(results2, "results2");
		List<String> results = new ArrayList<String>(results1);
		Set<String> results2Set = new TreeSet<String>(results2);
		results.retainAll(
				//results2
				results2Set
				);
		displayResultStatistics(results, "results");
		
		System.out.println("$");
		List<String> results0 = LinkIndexesUtils.join(results1, results2);
		displayResultStatistics(results0, "results0");
		
		TreeSet resultSet0 = new TreeSet<String>(results0);
		System.out.println("resultSet0.size() = " + resultSet0.size());
		TreeSet resultSet = new TreeSet<String>(results);
		System.out.println("resultSet.size() = " + resultSet.size());
		
		runWithIndex();
		runWithoutIndex();
		
		double ratio = (double)timer2.getTimeEllapsedMs()/(double)timer1.getTimeEllapsedMs();
		System.out.println();
		System.out.println("==============>" + MyUtils.TAB_SEPARATOR + ratio + MyUtils.TAB_SEPARATOR + 
				timer1.getTimeEllapsedMs() + results0.size() + MyUtils.TAB_SEPARATOR + results0.size() + 
				MyUtils.TAB_SEPARATOR + this.totalResults1 + 
				MyUtils.TAB_SEPARATOR + 
				this.totalResults2 + MyUtils.TAB_SEPARATOR);
		System.out.println();
		
		
/*	StringBuffer stringBuffer = new StringBuffer();
	for (int i = 0; i < results0.size(); i++) {
		stringBuffer.append(results0.get(i) + MyUtils.LINE_SEPARATOR);
	}
	stringBuffer.toString();
	MyUtils.writeFile("/home/anthony/Desktop/aba", stringBuffer.toString());*/
		
		
	}
	private void displayResultStatistics(List<String> results, String name) {
		System.out.println(name + ".size() = " + results.size());
		if (results.size()>1) System.out.println(MyUtils.TAB_SEPARATOR + name + ".get(0) = " + results.get(0));
		if (results.size()>2) System.out.println(MyUtils.TAB_SEPARATOR + name + ".get(last) = " + results.get(results.size()-1));
	}
	
	private void runWithIndex() throws TechnicalException, Exception {
		timer1 = new Timer();
		timer1.startTimer();
		
		readFiles();
		
		List<Filter> filterList1 = createFilterList(this.filterNamesList1, treeSet);
		leftQueryWithIndex.datasetList.get(0).setFiltersList(filterList1);
		leftQueryWithIndex.buildQuery();
		System.out.println("running 1: " + leftQueryWithIndex.getShortenedReadableUrl());
		List<String> results1 = (treeSet.isEmpty() ? new ArrayList<String>() : leftQueryWithIndex.urlContentToStringList(BioMartPortalLinks.MART_SERVICE_TIMEOUT));
		LinkIndexesUtils.checkForErrorsAndThrowException(results1);

		/*List<Filter> filterList2 = createFilterList(this.filterNamesList2, treeSet);
		rightQueryWithIndex.datasetList.get(0).setFiltersList(filterList2);
		rightQueryWithIndex.buildQuery();
		System.out.println("running 2: " + rightQueryWithIndex.getShortenedReadableUrl());
		List<String> results2 = (treeSet.isEmpty() ? new ArrayList<String>() : rightQueryWithIndex.urlContentToStringList(BioMartPortalLinks.MART_SERVICE_TIMEOUT));*/
		
		TreeSet<String> uniqueResultsFrom1 = new TreeSet<String>(results1);	
System.out.println("uniqueResultsFrom1.size() = " + uniqueResultsFrom1.size());
		List<Filter> filterList2 = createFilterList(this.filterNamesList2, uniqueResultsFrom1);
		rightQueryWithIndex.datasetList.get(0).setFiltersList(filterList2);
		rightQueryWithIndex.buildQuery();
		System.out.println("running 2: " + rightQueryWithIndex.getShortenedReadableUrl());
		List<String> results2 = (uniqueResultsFrom1.isEmpty() ? new ArrayList<String>() : rightQueryWithIndex.urlContentToStringList(BioMartPortalLinks.MART_SERVICE_TIMEOUT));
		LinkIndexesUtils.checkForErrorsAndThrowException(results2);
		
		displayResultStatistics(results1, "results1");
		displayResultStatistics(results2, "results2");
		
		List<String> results = LinkIndexesUtils.join(results1, results2);
		displayResultStatistics(results, "results");
		
		timer1.stopTimer();
		
		this.totalResults1 = results.size();
		System.out.println(MyUtils.TAB_SEPARATOR + MyUtils.TAB_SEPARATOR + this.totalResults1);
		//System.out.println(results);
		System.out.println(timer1);
		
/*StringBuffer stringBuffer = new StringBuffer();
for (int i = 0; i < results.size(); i++) {
	stringBuffer.append(results.get(i) + MyUtils.LINE_SEPARATOR);
}
stringBuffer.toString();
MyUtils.writeFile("/home/anthony/Desktop/bcb", stringBuffer.toString());*/
	}
	
	private void runWithoutIndex() throws TechnicalException, Exception {
		
		timer2 = new Timer();
		timer2.startTimer();
					
		leftQueryWithoutIndex.buildQuery();
		System.out.println("running 1: " + leftQueryWithoutIndex.getShortenedReadableUrl());
		List<String> results1 = leftQueryWithoutIndex.urlContentToStringList(BioMartPortalLinks.MART_SERVICE_TIMEOUT);
		LinkIndexesUtils.checkForErrorsAndThrowException(results1);
		
		TreeSet<String> uniqueResultsFrom1 = new TreeSet<String>(results1);		
		List<Filter> filterList2 = createFilterList(this.filterNamesList2, uniqueResultsFrom1);
		rightQueryWithoutIndex.datasetList.get(0).setFiltersList(filterList2);
		rightQueryWithoutIndex.buildQuery();
		System.out.println("running 2: " + rightQueryWithoutIndex.getShortenedReadableUrl());
//System.out.println("running 2: " + rightQueryWithoutIndex.getReadableUrl());		
		List<String> results2 = (uniqueResultsFrom1.isEmpty() ? new ArrayList<String>() : rightQueryWithoutIndex.urlContentToStringList(BioMartPortalLinks.MART_SERVICE_TIMEOUT));
		LinkIndexesUtils.checkForErrorsAndThrowException(results2);
		
		displayResultStatistics(results1, "results1");
		displayResultStatistics(results2, "results2");
		
		List<String> results = LinkIndexesUtils.join(results1, results2);
		displayResultStatistics(results, "results");
		
		timer2.stopTimer();
		
		this.totalResults2 = results.size();
		System.out.println(MyUtils.TAB_SEPARATOR + MyUtils.TAB_SEPARATOR + this.totalResults2);
		//System.out.println(results);
		System.out.println(timer2);
	}

	private void readFiles() throws TechnicalException, IOException {
		String errorMessage = LinkIndexesUtils.checkForErrors(file);
		if (errorMessage!=null) {
			throw new TechnicalException(errorMessage);
		}
		treeSet = MyUtils.getDataFromFileToTreeSet(file, MyUtils.TAB_SEPARATOR);	
		System.out.println("treeSet.size() = " + treeSet.size());
		//System.out.println("treeSet = " + treeSet);
	}

	private List<Filter> createFilterList(List<String> filterNamesList, TreeSet<String> values) {
		List<Filter> filterList = new ArrayList<Filter>();
		for (String filterName : filterNamesList) {
			filterList.add(new Filter(filterName, values, true));
		}
		return filterList;
	}
	
	/*private int processResults(Timer timer, List<String> results1, List<String> results2) throws FunctionalException {
		System.out.println(results1.size());
		System.out.println(results2.size());
		
		ArrayList<String> results = new ArrayList<String>(results1);
		results.retainAll(results2);
		
		timer.stopTimer();
		
		int resultsSize = results.size();
		System.out.println(MyUtils.TAB_SEPARATOR + resultsSize);
		System.out.println(results);
		System.out.println(timer);
		
		return resultsSize;
	}*/
}
