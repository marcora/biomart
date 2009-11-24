package org.biomart.test.linkIndicesTest.program;


import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.biomart.common.general.exceptions.TechnicalException;
import org.biomart.common.general.utils.MyUtils;



public class CheckResults {

	public static void main(String[] args) {
		try {

		@SuppressWarnings("unchecked")
			List<List<DatasetValues>> memoryResultListWithout = (List<List<DatasetValues>>)MyUtils.readSerializedObject(
					MyUtils.OUTPUT_FILES_PATH + "21-22-24-424false");
			System.out.println("+");
		@SuppressWarnings("unchecked")
			List<List<DatasetValues>> memoryResultListWith = (List<List<DatasetValues>>)MyUtils.readSerializedObject(
					MyUtils.OUTPUT_FILES_PATH + "21-22-24-549true");
			
			
			System.out.println(memoryResultListWithout.size());
			System.out.println(memoryResultListWith.size());
			
			System.out.println(memoryResultListWithout.get(0));
			System.out.println(memoryResultListWith.get(0));
			
			//System.out.println(memoryResultListWithout.get(0).get(0).equals());
			
			
			Set<String> joinFieldValueRight = new TreeSet<String>();
			Set<String> joinFieldValueRightI = new TreeSet<String>();
			
			Set<List<DatasetValues>> joinFieldValueRight2 = new HashSet<List<DatasetValues>>();
			Set<List<DatasetValues>> joinFieldValueRight2I = new HashSet<List<DatasetValues>>();
			
			
			Map<String, Integer> map = new TreeMap<String, Integer>();
			Map<String, Integer> mapI = new TreeMap<String, Integer>();
			
			for (List<DatasetValues> l : memoryResultListWithout) {
				String string = l.get(0).joinFieldValueRight;
				joinFieldValueRight.add(string);
				joinFieldValueRight2.add(l);
				
				t(map, string);
			}
			
			for (List<DatasetValues> li : memoryResultListWith) {
				String stringI = li.get(0).joinFieldValueRight;
				joinFieldValueRightI.add(stringI);
				joinFieldValueRight2I.add(li);
				t(mapI, stringI);
			}
			
			System.out.println(joinFieldValueRight.size());
			System.out.println(joinFieldValueRightI.size());
			
			joinFieldValueRight.retainAll(joinFieldValueRightI);
			System.out.println(joinFieldValueRight.size());
			
			int c=0;
			int c2=0;
			int c3=0;
			for (List<DatasetValues> l : memoryResultListWithout) {
				String string = l.get(0).joinFieldValueRight;
				int i = map.get(string);
				int iI = mapI.get(string);
				if (i!=iI) {
					System.out.println(i + ", " + iI + ", " + string);
					c++;
					if (i*2!=iI) {
						//System.out.println(i + ", " + iI + ", " + string);
						c2++;
					}
				} else {
					//System.out.println(i + ", " + iI + ", " + string);
					c3++;
				}
			}
			System.out.println();
			System.out.println(c);
			System.out.println(c2);
			System.out.println(c3);
			System.out.println();
			/*for (Iterator<String> it = map.keySet().iterator(); it.hasNext();) {
				String s = it.next();
				
			}*/
			
			/*System.out.println(map);
			System.out.println(mapI);*/
			
			System.out.println(joinFieldValueRight2.size());
			System.out.println(joinFieldValueRight2I.size());
			
			joinFieldValueRight2.removeAll(joinFieldValueRight2I);
			System.out.println(joinFieldValueRight2.size());
			
			/*int i=0;
			for (List<DatasetValues> ldv : joinFieldValueRight2) {
				
				if (!joinFieldValueRight2I.contains(ldv)) {
					System.out.println("############### " + i + " , " + ldv);
				}
				i++;
			}
			
			int i2=0;
			for (List<DatasetValues> ldv2 : joinFieldValueRight2I) {
				
				if (!joinFieldValueRight2.contains(ldv2)) {
					System.out.println("###############2 " + i2 + " , " + ldv2);
				}
				i2++;
			}*/
			
			System.out.println("done");
		} catch (TechnicalException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void t(Map<String, Integer> map, String string) {
		Integer i = map.get(string);
		if (i==null) {
			map.put(string, 1);
		} else {
			map.put(string, i+1);
		}
	}
}
