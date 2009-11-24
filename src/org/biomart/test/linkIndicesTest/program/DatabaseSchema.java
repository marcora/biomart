package org.biomart.test.linkIndicesTest.program;


import java.lang.reflect.Field;

import org.biomart.common.general.utils.SqlUtils;
import org.biomart.objects.helpers.Rdbs;




public class DatabaseSchema extends DataSource {
	
	public DatabaseParameter dbParam = null;
	public SqlUtils sqlUtils = null;
	public SuperTable superTable = null;	// Only 1 table for now (no within-dataset joins yet)
	
	public DatabaseSchema(Rdbs rdbs, String databaseHost, int databasePort, String databaseUser, String databasePassword, String databaseName) {
		this(rdbs, databaseHost, databasePort, databaseUser, databasePassword, databaseName, null);
	}
	public DatabaseSchema(Rdbs rdbs, String databaseHost, int databasePort, 
			String databaseUser, String databasePassword, String databaseName, SuperTable superTable) {
		this(rdbs, databaseHost, databasePort, databaseUser, databasePassword, databaseName, superTable, null, null);
	}
	public DatabaseSchema(Rdbs rdbs, String databaseHost, int databasePort, 
			String databaseUser, String databasePassword, String databaseName, SuperTable superTable, String linkTableName, String linkTableKey) {
		this(new DatabaseParameter(rdbs, databaseHost, databasePort, databaseUser, databasePassword, databaseName), superTable, true, linkTableName, linkTableKey);
	}
	public DatabaseSchema(DatabaseParameter dbParam, SuperTable superTable, boolean doBatch, String linkTableName, String linkTableKey) {
		super();
		
		this.dbParam = dbParam;
		this.sqlUtils = new SqlUtils(this.dbParam);	//TODO
		this.superTable = superTable;
		super.setDoBatch(doBatch);
		super.setLinkIndex(linkTableName, linkTableKey);
	}
	
	/**
	 * A inclure dans la classe directement
	 * 
	 * Affiche tous les champs d�clar�s d'un objet 
	 * (public, private, protected, avec ou sans getters/setters, ...)
	 * et leurs valeurs par introspection
	 * @return la String ainsi construite
	 */	
	/* 2007/12/28-14:34 */
	@Override
	public String toString () {	
		// Can't happen but just in case
		if (null==this) {
			return "instance is null";
		}	
		
		StringBuilder stringBuilder = new StringBuilder ("");
		stringBuilder.append( "[instance of " +
				this.getClass().getSimpleName() + " : ");
//		stringBuilder.append( "instance of " +
//		object.getClass().getSimpleName() + " (" + 
//		getObjectSuperClass(object).toString() + ") : ");
		Object fieldObject = null;
		try {
			for (Field field : this.getClass().getDeclaredFields()) {
				fieldObject = field.get(this);
				if (null==fieldObject) {
					stringBuilder.append( "(" + field.getType().getSimpleName() + ")" + 
							field.getName() + " = null, ");
				} else {
					stringBuilder.append( "(" + fieldObject.getClass().getSimpleName() + ")" + 
							field.getName() + " = " + fieldObject + ", ");
				}
			}
stringBuilder.append( super.toString() );
		} catch (IllegalArgumentException e) {			
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		stringBuilder.append( "]" );
		return stringBuilder.toString();
	}
}
