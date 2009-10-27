package org.biomart.test.linkIndicesTest.program;


import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.biomart.common.general.constants.MyConstants;
import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.common.general.utils.Timer;
import org.biomart.objects.helpers.Rdbs;
import org.biomart.test.linkIndicesTest.LinkIndexesUtils;


public class LinkIndicesTestEnvironmentMain {

	public static boolean exit = false;

	public static final int ROWS = 0;
	public static final int OVERLAP = 1;
	public static final int LOCATION = 2;
	public static Object[] getDatabaseFeature(String databaseName) {
		Object[] objectTab = new Object[3];
		StringTokenizer stringTokenizer = new StringTokenizer(databaseName, "_");
		stringTokenizer.nextToken();
		stringTokenizer.nextToken();
		objectTab[ROWS] = Integer.valueOf(stringTokenizer.nextToken());
		objectTab[OVERLAP] = Integer.valueOf(stringTokenizer.nextToken());
		objectTab[LOCATION] = stringTokenizer.nextToken();
		return objectTab;
	}
	
	public static List<String> listDatabaseName = null;
	public static List<LinkIndicesTestEnvironmentResult> listWithoutIndex = new ArrayList<LinkIndicesTestEnvironmentResult>();
	public static List<LinkIndicesTestEnvironmentResult> listWithIndex = new ArrayList<LinkIndicesTestEnvironmentResult>();
public static boolean reverse = false;
	public static void main(String[] args) {
		
		MyUtils.alterConsoleOutput();
		

		MyUtils.println("start.");			
		
		boolean printDebug = false;
		//runTmp(Mode.LINK_INDEX_CREATION, 4, printDebug);
		
//		consoleWriter.write(runTmp(Mode.LINK_INDEX_CREATION, null, printDebug).toString() + MyUtils.LINE_SEPARATOR);
		//runTmp(Mode.NON_INTERACTIVE, 4, printDebug);
//runTmp(Mode.NON_INTERACTIVE, null, printDebug);
		//runTmp(Mode.NON_INTERACTIVE_WITH_LINK_INDEX_ONLY, 4, printDebug);
		//runTmp(Mode.NON_INTERACTIVE_WITHOUT_LINK_INDEX_ONLY, 4, printDebug);
		//runTmp(Mode.NON_INTERACTIVE_WITHOUT_LINK_INDEX_ONLY, null, printDebug);
//runTmp(Mode.NON_INTERACTIVE_WITH_LINK_INDEX_ONLY, null, printDebug);
//	runTmp(Mode.NON_INTERACTIVE_WITHOUT_LINK_INDEX_ONLY, null, printDebug);

		//runTmp(Mode.FULL_NON_INTERACTIVE, 4, printDebug);

		
		
//		consoleWriter.write(runTmp(Mode.FULL_NON_INTERACTIVE, null, printDebug).toString() + MyUtils.LINE_SEPARATOR);

//			consoleWriter.write(runTmp(Mode.NON_INTERACTIVE_WITH_LINK_INDEX_ONLY, null, printDebug).toString() + MyUtils.LINE_SEPARATOR);
		

		//consoleWriter.write(runTmp(Mode.LINK_INDEX_CREATION, 4, printDebug).toString() + MyUtils.LINE_SEPARATOR);
	//consoleWriter.write(runTmp(Mode.FULL_NON_INTERACTIVE, 4, printDebug).toString() + MyUtils.LINE_SEPARATOR);
		
	//consoleWriter.write(runTmp(Mode.LINK_INDEX_CREATION, null, printDebug).toString() + MyUtils.LINE_SEPARATOR);
		//consoleWriter.write(runTmp(Mode.LINK_INDEX_CREATION, 5, printDebug).toString() + MyUtils.LINE_SEPARATOR);
		
		
		//consoleWriter.write(runTmp(Mode.NON_INTERACTIVE_WITHOUT_LINK_INDEX_ONLY, 4, printDebug).toString() + MyUtils.LINE_SEPARATOR);
		
		
		reverse = false;
		//runTmp(Mode.LINK_INDEX_CREATION, 5, printDebug);
		//runTmp(Mode.LINK_INDEX_CREATION, null, printDebug);
		runTmp(Mode.NON_INTERACTIVE, null, printDebug);
		runTmp(Mode.NON_INTERACTIVE, 5, printDebug);
		
		
		reverse = true;
		//runTmp(Mode.LINK_INDEX_CREATION, 5, printDebug);
		//runTmp(Mode.LINK_INDEX_CREATION, null, printDebug);
		runTmp(Mode.NON_INTERACTIVE, null, printDebug);
		runTmp(Mode.NON_INTERACTIVE, 5, printDebug);
		
		//runTmp(Mode.NON_INTERACTIVE_WITH_LINK_INDEX_ONLY, 5, printDebug);
		//consoleWriter.write(runTmp(Mode.FULL_NON_INTERACTIVE, 4, printDebug).toString() + MyUtils.LINE_SEPARATOR);			
		//consoleWriter.write(runTmp(Mode.FULL_NON_INTERACTIVE, 5, printDebug).toString() + MyUtils.LINE_SEPARATOR);
		//consoleWriter.write(runTmp(Mode.FULL_NON_INTERACTIVE, null, printDebug).toString() + MyUtils.LINE_SEPARATOR);
		
		
		//consoleWriter.write(runTmp(Mode.NON_INTERACTIVE_WITH_LINK_INDEX_ONLY, 5, printDebug).toString() + MyUtils.LINE_SEPARATOR);

		/*runTmp(Mode.LINK_INDEX_CREATION, 4, printDebug);
		runTmp(Mode.FULL_NON_INTERACTIVE, 4, printDebug);*/
		
		

		
		
		MyUtils.println("done.");
		
		MyUtils.closeConsoleOutput();
	}
	
	public static StringBuffer runTmp(Mode mode, Integer totalDataset, Boolean printDebug) {
		
		// Parameters to use
		//---------------------------------------------------------------
		String databaseHost = MyConstants.BMTEST_SERVER;
		Integer databasePort = 3306;
		String databaseUser = "martadmin";
		String databasePassword = "biomart";
		String databaseNameBase4 = "ac_test_tmp";
		String databaseNameBase5 = "ac_test";
		Rdbs rdbs = Rdbs.MYSQL; 

		/*Mode mode =
			//Mode.NON_INTERACTIVE;
			Mode.NON_INTERACTIVE_WITHOUT_LINK_INDEX_ONLY;
		int totalDataset = 2;
		Boolean printDebug = false;*/
		
		int batchSizeBase = 200;
		double batchSizeMultiplier = 2.0;
		int batchSizeIncrease = 9;	// >=1, 200 400 800 1600 3200 6400 12800 25600 51200 -> 9
	
		
		
		
		
		Boolean displaySuperDebug=true;

		LinkIndexesParameters parameters = null;
		LinkIndexesParameters parameters4 = null;
		LinkIndexesParameters parameters5 = null;
		LinkIndexesParameters parametersReal = null;
		if (null!=totalDataset) {
			
			String databaseNameBase_4_or_5 = totalDataset==4 ? databaseNameBase4 : databaseNameBase5;
					
			DatabaseSchema localDataset4 = new DatabaseSchema(rdbs, databaseHost, databasePort, databaseUser, databasePassword, databaseNameBase4);
			DatabaseSchema localDataset5 = new DatabaseSchema(rdbs, databaseHost, databasePort, databaseUser, databasePassword, databaseNameBase5);
			DatabaseSchema sequentialDataset[] = new DatabaseSchema[totalDataset];		
			for (int datasetNumber = 0; datasetNumber < totalDataset; datasetNumber++) {
				sequentialDataset[datasetNumber] = 
					new DatabaseSchema(
							new DatabaseParameter(rdbs, databaseHost, databasePort, databaseUser, databasePassword, LinkIndexesUtils.getDatabaseName(databaseNameBase_4_or_5, datasetNumber)),
							new SuperTable(
									new Table[] {
											new Table(LinkIndexesUtils.getMainTableName(datasetNumber), 
													LinkIndexesUtils.getMainIdField(datasetNumber), LinkIndexesUtils.getMainDescField(datasetNumber))
											},
									new JoinTable(
											LinkIndexesUtils.getMainTableName(datasetNumber), 
											LinkIndexesUtils.getMainIdField(datasetNumber), LinkIndexesUtils.getMainIdField(datasetNumber))
									
							),
							true, 
							LinkIndexesUtils.getLinkIndexTableName(databaseNameBase_4_or_5, datasetNumber, datasetNumber+1), 
							LinkIndexesUtils.getLinkIndexTableKey(datasetNumber, datasetNumber+1)
					);
			}		
	
			
			if (totalDataset==4) {
				parameters4 = new LinkIndexesParameters(localDataset4, mode,
						
						batchSizeBase, batchSizeMultiplier, batchSizeIncrease, 
						printDebug, displaySuperDebug,
						sequentialDataset[0], sequentialDataset[1], sequentialDataset[2], sequentialDataset[3]);
			} else if (totalDataset==5) {
				parameters5 = new LinkIndexesParameters(
						localDataset5, mode, batchSizeBase, batchSizeMultiplier, batchSizeIncrease, 
						printDebug, displaySuperDebug,
						sequentialDataset[0], sequentialDataset[1], sequentialDataset[2], sequentialDataset[3], sequentialDataset[4]);
			}
			
			if (totalDataset==4) {
				parameters = parameters4;
			} else if (totalDataset==5) {
				parameters = parameters5;
			}
		} else if (totalDataset==null) {
						
			DatabaseSchema localDataset3 = new DatabaseSchema(rdbs, MyConstants.BMTEST_SERVER, 3306, "martadmin", "biomart", "ac_test3");
			
			DatabaseSchema cyGene07SummarySiteADataset= new DatabaseSchema(
					new DatabaseParameter(rdbs, MyConstants.BMTEST_SERVER, 3306, "martadmin", "biomart", "cy_gene07_summary_siteA"),
					new SuperTable(
							new Table[] {
									new Table("gene10_summary__transcript_specimen__main", 
											"transcript_id_key", "ensembl_gene_id", "em_variant_size_201_to_400_bool")
									},
							new JoinTable("gene10_summary__transcript_specimen__main", "transcript_id_key")
						),
						true, "link_A_B", "a_b"
					);
			
			DatabaseSchema cyGene07SummarySiteBDataset = new DatabaseSchema(
					new DatabaseParameter(rdbs, MyConstants.BMTEST_SERVER, 3306, "martadmin", "biomart", "cy_gene07_summary_siteB"),
					new SuperTable(
							new Table[] {
									new Table("gene10_summary__transcript_specimen__main", 
											"transcript_id_key", "ensembl_gene_id")
									},
							new JoinTable("gene10_summary__transcript_specimen__main", "transcript_id_key", "ensembl_gene_id")
						),
						true, "link_B_E", "b_e"
					);
			
			DatabaseSchema ensemblDataset = new DatabaseSchema(
					new DatabaseParameter(rdbs, "martdb.ensembl.org", 5316, "anonymous", "", "ensembl_mart_53"),
					new SuperTable(
							new Table[] {
									new Table("hsapiens_gene_ensembl__gene__main", 
											"stable_id_1023", "seq_region_start_1020")
									}, 
							new JoinTable("hsapiens_gene_ensembl__gene__main", "stable_id_1023", "stable_id_1023")
							
						),
						true, "link_E1_E2", "e1_e2"
					);

			DatabaseSchema ensemblDataset2 = new DatabaseSchema(
					new DatabaseParameter(rdbs, "martdb.ensembl.org", 5316, "anonymous", "", "ensembl_mart_52"),
					new SuperTable(
							new Table[] {
									new Table("hsapiens_gene_ensembl__translation__main", 
											"stable_id_1023", "seq_region_start_1020")
									}, 
							new JoinTable("hsapiens_gene_ensembl__translation__main", "stable_id_1023")
							
						),
						true, "hsapiens_gene_ensembl__translation__main", "stable_id_1023"
					);ensemblDataset2.toString();	// To skip the unused
			
			parametersReal = new LinkIndexesParameters(
						localDataset3, mode, batchSizeBase, batchSizeMultiplier, batchSizeIncrease, 
						printDebug, displaySuperDebug
						, cyGene07SummarySiteADataset
						, cyGene07SummarySiteBDataset
						, ensemblDataset
						, ensemblDataset2
						);
			
			parameters = parametersReal;
		}
		
		
		//---------------------------------------------------------------
		
		
		
		
		
		
		
		
		
		
		
		
		/*
		
		batchSizeBase = 2;
		batchSizeMultiplier = 1.5;
		batchSizeIncrease = 2;	// >=1, 2 3 -> 2
		
		Database localDataset = new Database(BMTEST + ".res.oicr.on.ca", 3306, "martadmin", "biomart", "ac_test4");
		
		Database acTest4_0= new Database(
				BMTEST + ".res.oicr.on.ca", 3306, "martadmin", "biomart", "ac_test4_0",
				new Dataset(
						new Table[] {
								new Table("table0", 
										"right_id")
								}, 
						new JoinWrapper(
								new JoinTable("table0", 
										"right_id")),
						new JoinWrapper(
								new JoinTable("gene10_summary__transcript_specimen__main", 
									"transcript_id_key", "ensembl_gene_id"),
								new JoinTable("link_B_E", 
									"b_e")
						)
					));
		
		Database acTest4_1 = new Database(
				BMTEST + ".res.oicr.on.ca", 3306, "martadmin", "biomart", "ac_test4_1",
				new Dataset(
						new Table[] {
								new Table("table1", 
										"left_id", "right_id")
								}, 
						new JoinWrapper(
								new JoinTable("table1", 
										"left_id", "right_id")),
						new JoinWrapper(
								new JoinTable("gene10_summary__transcript_specimen__main", 
									"transcript_id_key", "ensembl_gene_id"),
								new JoinTable("link_B_E", 
									"b_e")
						)
					));
		
		Database acTest4_2 = new Database(
				BMTEST + ".res.oicr.on.ca", 3306, "martadmin", "biomart", "ac_test4_2",
				new Dataset(
						new Table[] {
								new Table("table2", 
										"left_id")
								}, 
						new JoinWrapper(
								new JoinTable("table2", 
										"left_id")),
						new JoinWrapper(
								new JoinTable("gene10_summary__transcript_specimen__main", 
									"transcript_id_key", "ensembl_gene_id"),
								new JoinTable("link_B_E", 
									"b_e")
						)
					));
		
		parameters = new LinkIndexesParameters(
					localDataset, mode, batchSizeBase, batchSizeMultiplier, batchSizeIncrease, 
					printDebug, displaySuperDebug
					, acTest4_0
					, acTest4_1
					, acTest4_2
					);*/
		
		
		
		
		
		
		
		
		
		//MyUtils.println(parameters);
		
		
		// Run program
		return run(parameters);
	}

	private static Float computeRatio(LinkIndicesTestEnvironmentResult withoutIndexResult, LinkIndicesTestEnvironmentResult withIndexResult) {
		float f = ((float)withoutIndexResult.getTimer().getTimeEllapsedMs()/(float)withIndexResult.getTimer().getTimeEllapsedMs());
		return f;
	}
	
	public static StringBuffer run (LinkIndexesParameters parameters) {
		
		StringBuffer stringBuffer = null;
		try {
			stringBuffer = new StringBuffer();
			
			String string = MyUtils.DASH_LINE + MyUtils.LINE_SEPARATOR;
			MyUtils.println(string);
			stringBuffer.append(string);
			
			// Build Link Index
			Timer buildLinkIndexTimer = null;
			if (parameters.buildLinkIndex) {
				buildLinkIndexTimer = new MultipleDatasetJoinCoordinator(parameters).buildAllLinkIndex();
			}
			
			// Without index
			LinkIndicesTestEnvironmentResult withoutIndexResult = null;
			
			if (parameters.withoutIndex) {
				if (parameters.persistResults) {
					/*linkIndicesTestEnvironment.buildResultTable(index, parameters);*/
				}
				
				withoutIndexResult = new MultipleDatasetJoinCoordinator(parameters).joinMultipleDataset(false, false, false, null);
				if (parameters.persistResults) {
					/*linkIndicesTestEnvironment.displayResult(withoutIndexResult, index, parameters);*/
				}
				string = "withoutIndexResult = " + withoutIndexResult;
				MyUtils.println(string);
				stringBuffer.append(string);
				
				listWithoutIndex.add(withoutIndexResult);
			}
			
			// With index
			LinkIndicesTestEnvironmentResult withIndexResult = null;
			
			if (parameters.withIndex) {
				if (parameters.persistResults) {
					/*linkIndicesTestEnvironment.buildResultTable(index, parameters);*/
				}
								
				withIndexResult = new MultipleDatasetJoinCoordinator(parameters).joinMultipleDataset(false, true, false, null);
				if (parameters.persistResults) {
					/*linkIndicesTestEnvironment.displayResult(withIndexResult, index, parameters);*/
				}
				string = "withIndexResult = " + withIndexResult;
				MyUtils.println(string);
				stringBuffer.append(string);
				
				listWithIndex.add(withIndexResult);
			}
			
			if (parameters.buildLinkIndex) {
				string = "\n----------------------------------------------------------\n" +
										"Time to build index: " + buildLinkIndexTimer + "\n" +
										"\n----------------------------------------------------------\n";
				MyUtils.println(string);		
				stringBuffer.append(string);
			}
			if (parameters.withIndex && parameters.withoutIndex) {
				float ratio = computeRatio(withoutIndexResult, withIndexResult);
				string = "\n----------------------------------------------------------\n" +
										"Without index: " + withoutIndexResult.getTimer() + "\n" + 
										"With index: " + withIndexResult.getTimer() + "\n" +
										"\nRatio: " + ratio + " times faster with index." +
										"\n----------------------------------------------------------\n";
				MyUtils.println(string);
				stringBuffer.append(string);
			}
		} catch (FunctionalException e) {
			e.printStackTrace();
		}
		
	    return stringBuffer;
	}
}
/*
time msba -e "select ac_test_0.ds0__main0__main.main0_id_key, ac_test_0.ds0__main0__main.main0_desc,ac_test_1.ds1__main1__main.main1_id_key, ac_test_1.ds1__main1__main.main1_desc,ac_test_2.ds2__main2__main.main2_id_key, ac_test_2.ds2__main2__main.main2_desc,ac_test_3.ds3__main3__main.main3_id_key, ac_test_3.ds3__main3__main.main3_desc,ac_test_4.ds4__main4__main.main4_id_key,ac_test_4.ds4__main4__main.main4_desc from ac_test_0.ds0__main0__main,ac_test_1.ds1__main1__main,ac_test_2.ds2__main2__main,ac_test_3.ds3__main3__main,ac_test_4.ds4__main4__main where ac_test_0.ds0__main0__main.main0_id_key=ac_test_1.ds1__main1__main.main1_id_key and ac_test_1.ds1__main1__main.main1_id_key=ac_test_2.ds2__main2__main.main2_id_key and ac_test_2.ds2__main2__main.main2_id_key=ac_test_3.ds3__main3__main.main3_id_key and ac_test_3.ds3__main3__main.main3_id_key=ac_test_4.ds4__main4__main.main4_id_key;" > /home/anthony/Desktop/my_rez
real	0m42.547s
*/
// Build DB Indexes
/*	// TODO broken

	String tableToDbIndex1 = "ds0__main0__main";
	String fieldToDbIndex1 = "main0_id_key";
	
	String tableToDbIndex2 = "ds1__main1__main";
	String fieldToDbIndex2 = "main1_id_key";
	
	String dbIndexName1 = "dbIndex_" + tableToDbIndex1 + "_" + fieldToDbIndex1;
	String dbIndexName2 = "dbIndex_" + tableToDbIndex2 + "_" + fieldToDbIndex2;

	if (buildDbIndexes) {
		linkIndicesDbIndexes = new LinkIndicesTestEnvironment(host, databaseNameBase);
		
		//MyUtils.println("building 1st db index (if doesn't exists)...");
		linkIndicesDbIndexes.buildDbIndex(dbIndexName1, tableToDbIndex1, fieldToDbIndex1);
		
		//MyUtils.println("building 2nd db index (if doesn't exists)...");
		linkIndicesDbIndexes.buildDbIndex(dbIndexName2, tableToDbIndex2, fieldToDbIndex2);
	}

	if (!parameters.buildLinkIndex) {
		int size = listDatabaseName.size();
		for (int i = 0; i < size; i++) {
			String databaseName = listDatabaseName.get(i);
			LinkIndicesTestEnvironmentResult withoutIndexResult = listWithoutIndex.get(i);
			LinkIndicesTestEnvironmentResult withIndexResult = listWithIndex.get(i);
			displayFinalResults(i, databaseName, withoutIndexResult, withIndexResult);	
		}
	}
	private static String getDatabaseDescription(String databaseName) {
		Object[] objectTab = getDatabaseFeature(databaseName);
		return "2 tables with:\t\t\t\t\t" + MyUtils.getTenToThe((Integer)objectTab[ROWS]) + " rows\n" +
				"\tincluding:\t\t\t\t" + MyUtils.getTenToThe((Integer)objectTab[OVERLAP]) + " overlapping ones\n" +
				"\t'source' table location:\t\t" + objectTab[LOCATION];
	}
	
	private static void displayFinalResults(int i, String databaseNameBase, 
			LinkIndicesTestEnvironmentResult withoutIndexResult, LinkIndicesTestEnvironmentResult withIndexResult) {
		MyUtils.println(MyUtils.EQUAL_LINE);
		MyUtils.println("Scenario " + (i+1) + ":" + MyUtils.TAB_SEPARATOR + databaseNameBase); //getDatabaseDescription(databaseName)
		MyUtils.println();
		MyUtils.println("Without link index:\t\t" + withoutIndexResult.getTimer().getTimeEllapsedMs() + " ms");
		MyUtils.println("With    link index:\t\t" + withIndexResult.getTimer().getTimeEllapsedMs() + " ms");
		MyUtils.println("\tto retrieve the first 200 matching results.");
		MyUtils.println();
		MyUtils.println("Time Gain:\t\t" + computeRatio(withoutIndexResult, withIndexResult) + " times faster with the index.");
		MyUtils.println();
	}
*/