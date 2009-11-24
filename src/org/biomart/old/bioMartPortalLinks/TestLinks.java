package org.biomart.old.bioMartPortalLinks;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.biomart.common.general.utils.MyUtils;
import org.biomart.old.martService.MartServiceConstants;
import org.biomart.old.martService.objects.MartInVirtualSchema;
import org.biomart.old.martService.restFulQueries.RestFulQuery;
import org.biomart.old.martService.restFulQueries.RestFulQueryDataset;
import org.biomart.old.martService.restFulQueries.objects.Attribute;
import org.biomart.old.martService.restFulQueries.objects.Element;
import org.biomart.old.martService.restFulQueries.objects.Filter;


/**
 * anthony@anthony-desktop:~/workspace/00LinkIndex/bin$ java6 -cp "$JDOM:." bioMartPortalLinks.TestLinks > ~/Desktop/TestLinks0626 2> ~/Desktop/TestLinksError0626
 * @author anthony
 *
 */
public class TestLinks {
	
	static int TIME_OUT_JOIN = 120000;
	static int TIME_OUT = 30000;

	public static void main(String[] args) {
		BioMartPortalLinks.main(new String[]{"test"});
	}
	BioMartPortalLinks bioMartPortalLinks = null;
	public TestLinks(BioMartPortalLinks biomartPortalLinks) {
		this.bioMartPortalLinks = biomartPortalLinks;
	}
	public void test() throws Exception {
		System.out.println(bioMartPortalLinks.listLinks1.size());
		
		boolean justCount = false;
		int count = 0;
		
		int validCount=0;
		int visibilityProblemCount=0;
		int otherProblemCount=0;
		List<String> otherProblemList = new ArrayList<String>();
			
		for (Link link : bioMartPortalLinks.listLinks1) {

			if (!link.left.datasetName.equals("hsapiens_gene_ensembl") || !link.left.virtualSchemaName.equals("default")) continue;
			
			
			
			for (Iterator<Element> it = link.left.dataset.attributesByNameMap.values().iterator(); it.hasNext();) {

				Attribute att = (Attribute)it.next();
				System.out.println(att.internalName);
				if (att.hidden) continue;
				
				RestFulQuery restFulQuery = new RestFulQuery(BioMartPortalLinks.MART_SERVICE_URL, "default", "0.7", 
						MartServiceConstants.DEFAULT_FORMATTER, false, false, false, null, null,		// unique?
						new RestFulQueryDataset("hsapiens_gene_ensembl", new ArrayList<Attribute>(Arrays.asList(new Attribute[] {new Attribute(att.internalName)})), null));
				List<String> rez = null;
				try {
					rez = restFulQuery.urlContentToStringList(TIME_OUT_JOIN);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (null!=rez) {
					TreeSet<String> set = new TreeSet<String>(rez);
					if (rez.size()!=set.size()) {
						
						
						Filter counterPartFilter = null;
						for (Iterator<Element> it2 = link.left.dataset.filtersByNameMap.values().iterator(); it2.hasNext();) {
							Filter fil = (Filter)it2.next();
							System.out.println("\t" + fil.internalName);
							if (fil.field.isValid() && att.field.isValid() && fil.field.equals(att.field) && !fil.hidden) {
								counterPartFilter = fil;
								System.out.println("ok!");
								break;
							}
						}
						
						System.out.println(counterPartFilter);
						
						if (counterPartFilter!=null) {
							System.out.println("\t\t" + counterPartFilter.internalName);
						
							String s1 = null;
							
							for (int i = 0; i < rez.size(); i++) {
							
								s1 = rez.get(i);
								int occ=0;
								for (int j = i+1; j < rez.size(); j++) {
									String s = rez.get(j);
									if (s1.equals(s)) {
										//System.out.println(j + "\t" + s);
										occ++;
										System.out.println("\t++");
									}
								}
								
								if (occ>=2) {
									System.out.println(rez.size());
									System.out.println(set.size());
									System.out.println(att.internalName);
									System.out.println(occ + "\t" + s1);	
									
									
									System.out.println("==>" + counterPartFilter.internalName);
									System.exit(0);
									
								}
							}
						}
					}
					Thread.sleep(500);
				}
			}
	
			
			MartInVirtualSchema leftMart = bioMartPortalLinks.configuration.getMartByName(link.left.martName);
			MartInVirtualSchema rightMart = bioMartPortalLinks.configuration.getMartByName(link.right.martName);
			
			
		
			// Both dataset must be visible
			boolean validVisibility =
				link.left.dataset.visible && !link.right.dataset.visible; 
				//link.getValidVisibility();
			
			
			// Skip if both visible
			if (!validVisibility) continue;
			
			MyUtils.checkStatusProgram(link.left.importable.getCompleteElementList(), 
					"link.left.importable.getCompleteElementList() = " + link.left.importable.getCompleteElementList(), true);		
			
			if (justCount) {
				count++;
				continue;
			}
			
			if (!link.virtualSchema.equals("pancreas_expression_db")) continue;
			
			/*if (link.left.dataset.mart.equals("ENSEMBL_MART_ENSEMBL")) continue;
			if (link.left.dataset.mart.equals("QTL_MART")) continue;
			if (link.left.dataset.mart.equals("ENSEMBL_MART_SNP")) continue;
			if (link.left.dataset.mart.equals("ENSEMBL_MART_GENOMIC_FEATURES")) continue;
			if (link.left.dataset.mart.equals("ENSEMBL_MART_SEQUENCE")) continue;
			if (link.left.dataset.mart.equals("GRAMENE_MARKER_29")) continue;
			if (link.left.dataset.mart.equals("GRAMENE_MAP_29")) continue;
			if (link.left.dataset.mart.equals("GRAMENE_ONTOLOGY_29")) continue;*/
			
			//if (link.left.dataset.mart.equals("Eurexpress Biomart") || link.right.dataset.mart.equals("Eurexpress Biomart")) continue;
			//if (!link.left.dataset.datasetType.equals(MartServiceConstants.ATTRIBUTE_TABLE_SET) || !link.right.dataset.datasetType.equals(MartServiceConstants.ATTRIBUTE_TABLE_SET)) continue;
						
			
			
			
			
			if (count>=0) {
				System.out.print("count = " + count + MyUtils.TAB_SEPARATOR + link.toStatisticString() + MyUtils.TAB_SEPARATOR);
				RestFulQuery query =
					link.createPointerMartServiceRestFulQuery(BioMartPortalLinks.MART_SERVICE_URL);
					//link.createMartServiceRestFulQuery(BioMartPortalLinks.MART_SERVICE_URL);
				
				if (null==query) {	// most likely a "random" link, one that just happens by luck
					System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
					continue;
				}
				
				query.buildQuery();
				System.out.print("running : " + query.getReadableUrl());
				String firstRow = "999999999999999999999999999999999999999";
				String errorMessage = null;
				try {
					firstRow = query.firstLineToString(TIME_OUT_JOIN);
				} catch (Exception e) {
					errorMessage = e.getMessage();
				}
				
				if (firstRow!=null && firstRow.contains("\"batching\"")) { // corresponds to visibility problem
					
					System.out.println();
					//System.out.println("################ " + link.left.martName + "/" + link.right.martName);
					
					visibilityProblemCount++;
					
					System.out.println("################ " + leftMart.martName + "(" + leftMart.visible + ")" + "/" + 
							rightMart.martName + "(" + rightMart.visible + ")" + " - " + 
							link.left.dataset.datasetName + "(" + link.left.dataset.visible + ")" + "/" + 
							link.right.dataset.datasetName + "(" + link.right.dataset.visible + ")"
							);
					
					//MartInVirtualSchema leftMart = null;
					//Set<Martin>biomartPortalLinks.configuration.virtualSchemaMartSetMap
					/*for (Iterator<MartInVirtualSchema> it = biomartPortalLinks.configuration.virtualSchemaMartSetMap.values().iterator(); it.hasNext();) {
						MartInVirtualSchema mart = it.next();
					}*/
					
					
					//link.left.martName
					
				} else if (errorMessage!=null || (firstRow!=null && 
						
								
//!firstRow.contains("\"batching\"") &&
						
						(firstRow.startsWith(MartServiceConstants.NO_LINK_ERROR_MESSAGE) ||
						firstRow.startsWith(MartServiceConstants.ERROR_MESSAGE) ||
						firstRow.contains("ERROR") || firstRow.contains("Error") || firstRow.contains("error")))) {
					
					otherProblemCount++;
					otherProblemList.add(errorMessage!=null ? errorMessage : firstRow);
					
					System.out.println();
					System.out.println("$$$$$$$$$$$$$$$$$ ");
					System.out.println();
					System.out.println(MyUtils.DASH_LINE);
					System.out.println("firstRow = " + firstRow);
					System.out.println("errorMessage = " + errorMessage);
					
					System.out.println(link.toNiceStringPreCreation());
					
					RestFulQuery queryExportable = link.right.createMartServiceRestFulQuery(BioMartPortalLinks.MART_SERVICE_URL);
					queryExportable.buildQuery();
					System.out.println("running : " + queryExportable.getReadableUrl());
					
					String firstRowExportable = "999999999999999999999999999999999999999";
					errorMessage = null;
					try {
						firstRowExportable = queryExportable.firstLineToString(TIME_OUT);
					} catch (Exception e) {
						errorMessage = e.getMessage();
					}
					System.out.println(MyUtils.TAB_SEPARATOR + (errorMessage==null ? firstRowExportable : errorMessage));

					RestFulQuery queryImportable = link.left.createMartServiceRestFulQuery(BioMartPortalLinks.MART_SERVICE_URL);
					queryImportable.buildQuery();
					System.out.println("running : " + queryImportable.getReadableUrl());
					
					String firstRowImportable = "999999999999999999999999999999999999999";
					errorMessage = null;
					try {
						firstRowImportable = queryImportable.firstLineToString(TIME_OUT);
					} catch (Exception e) {
						errorMessage = e.getMessage();
					}
					System.out.println(MyUtils.TAB_SEPARATOR + (errorMessage==null ? firstRowImportable : errorMessage));
					
					System.out.println(MyUtils.DASH_LINE);
					
					//MyUtils.error();
				} else {
					System.out.println(MyUtils.TAB_SEPARATOR + firstRow);
					
					validCount++;
					System.out.println();
					System.out.println("@@@@@@@@@@@@@@@@@@@ " + leftMart.martName + "(" + leftMart.visible + ")" + "/" + 
							rightMart.martName + "(" + rightMart.visible + ")" + " - " + 
							link.left.dataset.datasetName + "(" + link.left.dataset.visible + ")" + "/" + 
							link.right.dataset.datasetName + "(" + link.right.dataset.visible + ")"
							);
					
					//System.out.println("@@@@@@@@@@@@@@@@@@@ " + leftMart.martName + "(" + leftMart.visible + ")" + "/" + rightMart.martName + "(" + rightMart.visible + ")");
					
					
				}
				//MyUtils.pressKeyToContinue();
			}
			
			Thread.sleep(1000);	// to space out queries
			count++;
		}
		System.out.println("count = " + count);
		
		System.out.println("validCount = " + validCount);
		System.out.println("visibilityProblemCount = " + visibilityProblemCount);
		System.out.println("otherProblemCount = " + otherProblemCount);
		System.out.println("otherProblemList.size() = " + otherProblemList.size());
		for (String s : otherProblemList) {
			System.out.println(MyUtils.TAB_SEPARATOR + s);
		}
		System.out.println();
	}
}
