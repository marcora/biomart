package org.biomart.common.general.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.StringTokenizer;


public class Tmp2 {
	public static void main(String[] args) throws Exception {
		/*FileWriter fw = new FileWriter("/home/anthony/Desktop/slash.sql");
		BufferedWriter bw = new BufferedWriter(fw);
		for (int i = 0; i < 10000000; i++) {
			bw.write("x_0_1_" + i + "\t" + i + "\t0_1_1_" + i + "\n");
		}
		bw.close();
		fw.close();*/
		
		System.out.println(	
				MyUtils.split("bm-test.res.oicr.on.ca:3306:martadmin:biomart:ac_query_runner_x	table0	l0,m0	r0,s0	pk0", '\t').length);
		System.exit(0);
		
		String s = MyUtils.readFile("/home/anthony/Desktop/zyui");
		StringTokenizer stringTokenizer = new StringTokenizer(s, "\n");
		
		FileWriter fw = new FileWriter("/home/anthony/Desktop/ac_query_runner.sql");
		BufferedWriter bw = new BufferedWriter(fw);
		while (stringTokenizer.hasMoreTokens()) {
			bw.write(Integer.valueOf(stringTokenizer.nextToken()) + "\t" + MyUtils.randomString() + MyUtils.LINE_SEPARATOR);
		}
		bw.close();
		fw.close();
		
	}
}
