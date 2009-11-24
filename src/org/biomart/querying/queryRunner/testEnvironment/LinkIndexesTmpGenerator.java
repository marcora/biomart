package org.biomart.querying.queryRunner.testEnvironment;


import java.io.FileWriter;
import java.io.IOException;

import org.biomart.common.general.utils.MyUtils;
import org.biomart.objects.helpers.Rdbs;


public class LinkIndexesTmpGenerator {

	public static final Rdbs rdbs = Rdbs.POSTGRESQL;
	public static final int TOTAL_THREADS = 10;
	public static final int OVERLAP = 50;
	public static final String dbName = OVERLAP + "_1_" + (TOTAL_THREADS==10 ? 1000000 : 100000) + "_b" + (rdbs.isOracle() ? "" : "ottom");
	public static final String TABLE_NAME = (rdbs.isOracle() ? "si_" : "super_index_") + dbName;
	public static void main(String[] args) {
		superIndex();
		//linkIndexes();
	}
	private static void linkIndexes() {
		createIndex();
		loadIndex();
	}	
	public static void loadIndex() {
		
		System.out.println("use link_indexes;");
		
		for (int i = 0; i < TOTAL_THREADS-1; i++) {
			String tableName = createTableName(i);
			System.out.println("drop table if exists " + tableName + ";");
			String fieldName = "f" + i + "_" + (i+1);
			System.out.println("create table " + tableName + "(" + fieldName + " varchar(16), " + "tmp" + " varchar(16));");
			
		}
		for (int i = 0; i < TOTAL_THREADS-1; i++) {
			String fileName = "linkIndex_" + i + "_" + (i+1);
			String tableName = createTableName(i);
			System.out.println("load data local infile '/home/anthony/Desktop/QueryRunner/" + fileName + "' " +
					"into table " + tableName + ";");
		}
		for (int i = 0; i < TOTAL_THREADS-1; i++) {
			String tableName = createTableName(i);
			System.out.println("alter table " + tableName + " drop column tmp;"); 
		}
		for (int i = 0; i < TOTAL_THREADS-1; i++) {
			String tableName = createTableName(i);
			String fieldName = "f" + i + "_" + (i+1);
			System.out.println("create unique index index_" + fieldName + " on " + tableName + "(" + fieldName + ");"); 
		}
	}
	
	public static void createIndex() {
		for (int l = 0; l < TOTAL_THREADS; l++) {
			try {
				FileWriter fw = new FileWriter("/home/anthony/Desktop/QueryRunner/linkIndex_" + l + "_" + (l+1));
				for (int i = 50000; i < 100000; i++) {
					fw.write(l + "_" + (l+1) + "_1_" + i + "\n");			
				}
				fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
	}
	
	public static void superIndex() {
		try {
			FileWriter fw = new FileWriter("/home/anthony/Desktop/QueryRunner/" + TABLE_NAME + ".sql");
			String separator = rdbs.isMySql() ? MyUtils.TAB_SEPARATOR : ",";
			for (int i = 500000; i < (TOTAL_THREADS==10 ? 1000000 : 100000); i++) {
				if (!rdbs.isMySql()) {
					fw.write("insert into " + TABLE_NAME + " values(");
				}
				for (int l = 0; l < TOTAL_THREADS-1; l++) {
					fw.write(
							(!rdbs.isMySql() ? "'" : "") + DataScriptGenerator.generateRightValue(l, i) + (!rdbs.isMySql() ? "'" : "") +  
							(l!=TOTAL_THREADS-2 ? separator : ""));
				}
				if (!rdbs.isMySql()) {
					fw.write(");");
				}
				fw.write(MyUtils.LINE_SEPARATOR);
			}
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String createTableName(int i) {
		String tableName = "link_index_" + dbName + "_" + i + "_" + (i+1);
		return tableName;
	}
}
