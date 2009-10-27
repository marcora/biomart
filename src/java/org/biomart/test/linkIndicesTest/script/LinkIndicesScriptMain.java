package org.biomart.test.linkIndicesTest.script;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.biomart.common.general.utils.Hack;
import org.biomart.common.general.utils.HackEnum;
import org.biomart.common.general.utils.Range;
import org.biomart.test.linkIndicesTest.LinkIndexesUtils;


public class LinkIndicesScriptMain {

	public static void main(String[] args) throws IOException {
		System.out.println("start.");
				
		String databaseNameBase = "ac_test_tmp";
		int totalDataset = 
			//5;
			4;
		int[] totalRowsTab = new int[] {
				//100000, 1000000
				//10000000
				10000
				};
		double[] overlapPercentageTab = new double[] {
				//0.2, 0.5, 0.75
				0.1
				};
		Repartition[] repartitionTab = 
			//Repartition.values();
			//new Repartition[] {Repartition.TOP, Repartition.MIDDLE, Repartition.BOTTOM, Repartition.SCATTERED};
			//new Repartition[] {Repartition.TOP, Repartition.MIDDLE, Repartition.BOTTOM};
			new Repartition[] {Repartition.BOTTOM};
		
		String host = 
			//LinkIndicesScript.LOCALHOST;
			LinkIndicesScript.BMTEST;
		
		List<LinkIndicesScript> listLinkIndicesScript = new ArrayList<LinkIndicesScript>();
		StringBuffer dropsStringBuffer = new StringBuffer();
		StringBuffer importStringBuffer = new StringBuffer();
		StringBuffer commandsStringBuffer = new StringBuffer();
		
		for (int totalRowsI = 0; totalRowsI < totalRowsTab.length; totalRowsI++) {
			for (int overlapPercentageI = 0; overlapPercentageI < overlapPercentageTab.length; overlapPercentageI++) {
				for (int repartitionTabI = 0; repartitionTabI < repartitionTab.length; repartitionTabI++) {
										
					int totalRows = totalRowsTab[totalRowsI];
					double overlapPercentage = overlapPercentageTab[overlapPercentageI];
					Repartition repartition = repartitionTab[repartitionTabI];
					int overlap = (int)(totalRows*overlapPercentage);				
					
					String[] databaseNameTab = new String[totalDataset];
					dropsStringBuffer.append(host + " -e \"drop database if exists " + databaseNameBase + "\"\n");
					for (int datasetNumber = 0; datasetNumber < databaseNameTab.length; datasetNumber++) {						
						databaseNameTab[datasetNumber] = LinkIndexesUtils.getDatabaseName(databaseNameBase, datasetNumber);/*"ac_test" + datasetNumber + "_" + totalRows + "_" + overlap + "_" + repartition;*/
						dropsStringBuffer.append(host + " -e \"drop database if exists " + databaseNameTab[datasetNumber] + "\"\n");
						System.out.println(totalRows + ", " + overlap + ", " + repartition + ", " + databaseNameTab[datasetNumber]);
					}
					
					Range[] mainIdRanges = null;
					     
					if (!repartition.equals(Repartition.SCATTERED)) {
						int rangeStart = repartition.getRangeStart(totalRows, overlap);
						Range range = new Range(rangeStart, rangeStart+overlap-1);	// -1 because inclusive
						System.out.println("range = " + range.toString());
						mainIdRanges = new Range[] {range};
					} else {
						mainIdRanges = new Range[overlap];
						for (int i = 0; i < overlap; i++) {
							int gap = (int)(i*(1/overlapPercentage));
							mainIdRanges[i] = new Range(gap, gap);
						}
						System.out.println("mainIdRanges = " + mainIdRanges.length + 
								": mainIdRanges[first] = " + mainIdRanges[0] + ", " +
								"mainIdRanges[last] = " + mainIdRanges[mainIdRanges.length-1]);
					}
					
					Integer[] decreasingOverlapTab = new Integer[totalDataset];
					decreasingOverlapTab[0] = totalRows;
					for (int i = 1; i < totalDataset; i++) {
						decreasingOverlapTab[i] = (int)(decreasingOverlapTab[i-1]*overlapPercentage);
					}
					
					LinkIndicesScript linkIndicesScript = new LinkIndicesScript(host, "test phase 2", false,
							totalDataset, totalRows, 0, -1,
							mainIdRanges, 
							null, 
							null,
							databaseNameBase, 
							new Hack[] {
								/*repartition.equals(Repartition.SCATTERED) ? 
										new Hack (HackEnum.HIDE_FULL_RANGES, new String[] {}, new Integer[] {}, new Double[] {}, new Boolean[] {}) : null,
								new Hack (HackEnum.SAME_DATABASE, new String[] {}, new Integer[] {}, new Double[] {}, new Boolean[] {}),
								new Hack (HackEnum.FULLY_DESCRIPTIVE_DATABASE_NAMES, new String[] {}, new Integer[] {}, new Double[] {}, new Boolean[] {}),
								new Hack (HackEnum.FULLY_DESCRIPTIVE_SCRIPT_NAMES,  new String[] {}, new Integer[] {}, new Double[] {}, new Boolean[] {}),
								new Hack (HackEnum.ON_LOCAL,  new String[] {}, new Integer[] {}, new Double[] {}, new Boolean[] {}),
								new Hack (HackEnum.MIDDLED,  new String[] {}, new Integer[] {}, new Double[] {}, new Boolean[] {}, 
										new Hack (HackEnum.SUB_MIDDLED, null, new Integer[] {null}, null, null)),*/
								new Hack (HackEnum.DECREASING_OVERLAP,  new String[] {}, decreasingOverlapTab, new Double[] {}, new Boolean[] {}),
							}
					);
					listLinkIndicesScript.add(linkIndicesScript);
				}
			}
		}
		System.out.println();
		System.out.println();
		
		for (LinkIndicesScript linkIndicesScript : listLinkIndicesScript) {
			importStringBuffer.append(linkIndicesScript.process() + "\n");
		}
		
		System.out.println();
		System.out.println(dropsStringBuffer.toString());
		System.out.println(importStringBuffer.toString());
		System.out.println(commandsStringBuffer.toString());
		
		System.out.println("done.");
	}
}
