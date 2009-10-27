package org.biomart.test.linkIndicesTest.program;


import java.io.BufferedWriter;
import java.io.File;
import java.lang.reflect.Field;
import java.sql.SQLException;

import org.biomart.common.general.utils.MyUtils;
import org.biomart.test.linkIndicesTest.LinkIndexesUtils;


public class LinkIndexesParameters {
	public Integer totalDataset = null;
	public DataSource[] dataSources = null;
	public DatabaseSchema localDatabase = null;
	
	public BufferedWriter resultWriter = null;
	
	public Integer batchSizeBase = null;
	public Double batchSizeMultiplier = null;
	public Integer batchSizeIncrease = null;
	public Integer[] batchTab = null;
	
	public Mode mode = null;
	
	public Boolean buildDbIndexes =  null; 
	public Boolean buildLinkIndex = null;
	public Boolean interactive = null;
	public Boolean withIndex = null;
	public Boolean withoutIndex = null;
	
	public Boolean distinct = null;
	public Boolean useIndex = null;
	public Boolean indexCreation = null;	
	public File createIndextemporaryFile = null;
	
	public String withoutIndexResultTableName = null;
	public String withIndexResultTableName = null;
	public Boolean storeResults = null;
	public Boolean persistResults = null;
	
	public Boolean printDebug = null;
	public Boolean displaySuperDebug = null;
	
	public Boolean error = null;
	
	public LinkIndexesParameters(DatabaseSchema localDataset, Mode mode, 
			int batchSizeBase, double batchSizeMultiplier, int batchSizeIncrease, 
			boolean printDebug, boolean displaySuperDebug,
			DataSource... datasets) {
		this(localDataset, mode, null, null, null, batchSizeBase, batchSizeMultiplier, batchSizeIncrease, printDebug, displaySuperDebug, datasets);
	}
	
	public void setProperties(Boolean distinct, Boolean useIndex, Boolean indexCreation, File createIndextemporaryFile) {
		this.distinct = distinct;
		this.useIndex = useIndex;
		this.indexCreation = indexCreation;
		this.createIndextemporaryFile = createIndextemporaryFile;
	}
	
	public LinkIndexesParameters(DatabaseSchema localDataset, Mode mode, 
			Boolean useIndex, Boolean createIndex, Boolean distinct,
			int batchSizeBase, double batchSizeMultiplier, int batchSizeIncrease, 
			boolean printDebug, boolean displaySuperDebug,
			DataSource... dataSources) {
		
		super();
		this.localDatabase = localDataset;
		this.totalDataset = dataSources.length;
		this.dataSources = dataSources;
		
		this.useIndex = useIndex;
		this.indexCreation = createIndex;
		this.distinct = distinct;
		
		this.batchSizeBase = batchSizeBase;
		this.batchSizeMultiplier = batchSizeMultiplier;
		this.batchSizeIncrease = batchSizeIncrease;
		this.batchTab = new Integer[batchSizeIncrease];
		this.batchTab[0] = batchSizeBase; 
		for (int i = 1; i < this.batchTab.length; i++) {
			this.batchTab[i] = (int)(this.batchTab[i-1]*batchSizeMultiplier);
		}
		
		this.printDebug = printDebug;
		this.displaySuperDebug = displaySuperDebug;
		
		this.mode = mode;
		this.withoutIndexResultTableName = LinkIndexesUtils.getResultTableName(localDataset.dbParam.databaseName, false);
		this.withIndexResultTableName = LinkIndexesUtils.getResultTableName(localDataset.dbParam.databaseName, true); 
		
		this.error = Boolean.FALSE;
		
		this.buildDbIndexes =  false; 
		if (this.mode.equals(Mode.FULL_INTERACTIVE)) {
			this.buildLinkIndex = Boolean.TRUE;
			this.withIndex = true;
			this.withoutIndex = true;
	this.interactive = Boolean.TRUE;
		} else if (this.mode.equals(Mode.FULL_NON_INTERACTIVE)) {
			this.buildLinkIndex = Boolean.TRUE;
			this.withIndex = true;
			this.withoutIndex = true;
			this.interactive = Boolean.FALSE;
		} else if (this.mode.equals(Mode.LINK_INDEX_CREATION)) {
			this.buildLinkIndex = Boolean.TRUE;
			this.withIndex = false;
			this.withoutIndex = false;
			this.interactive = false;
} else if (this.mode.equals(Mode.LINK_INDEX_CREATION_TMP)) {
	this.buildLinkIndex = null;
	this.withIndex = null;
	this.withoutIndex = null;
	this.interactive = Boolean.FALSE;
	} else if (this.mode.equals(Mode.USE_INDEX_TMP)) {
		this.buildLinkIndex = null;
		this.withIndex = null;
		this.withoutIndex = null;
		this.interactive = Boolean.FALSE;
			} 		
		else {
			this.buildLinkIndex = false;
			
			if (this.mode.equals(Mode.INTERACTIVE)) {
				this.withIndex = true;
				this.withoutIndex = true;
				this.interactive = Boolean.TRUE;
			} else if (this.mode.equals(Mode.NON_INTERACTIVE)) {
				this.withIndex = true;
				this.withoutIndex = true;
				this.interactive = Boolean.FALSE;
			} if (this.mode.equals(Mode.INTERACTIVE_WITH_LINK_INDEX_ONLY)) {
				this.withIndex = true;
				this.withoutIndex = false;
				this.interactive = Boolean.TRUE;
			} else if (this.mode.equals(Mode.INTERACTIVE_WITHOUT_LINK_INDEX_ONLY)) {
				this.withIndex = false;
				this.withoutIndex = true;
				this.interactive = Boolean.TRUE;
			} else if (this.mode.equals(Mode.NON_INTERACTIVE_WITHOUT_LINK_INDEX_ONLY)) {
				this.withIndex = false;
				this.withoutIndex = true;
				this.interactive = Boolean.FALSE;
			} else if (this.mode.equals(Mode.NON_INTERACTIVE_WITH_LINK_INDEX_ONLY)) {
				this.withIndex = true;
				this.withoutIndex = false;
				this.interactive = Boolean.FALSE;
			}
		}
				
		this.storeResults = true;
		this.persistResults = false;
	}
	
	public void connectAll() throws SQLException {
		this.localDatabase.sqlUtils.connect();
    	for (int datasetNumber = 0; datasetNumber < this.totalDataset; datasetNumber++) {
    		DataSource dataSource = this.dataSources[datasetNumber];
    		if (dataSource instanceof DatabaseSchema) {
    			((DatabaseSchema)dataSource).sqlUtils.connect();
    		}
		}
	}	
	public void disconnectAll() {
		this.localDatabase.sqlUtils.disconnect();
    	for (int datasetNumber = 0; datasetNumber < this.totalDataset; datasetNumber++) {
    		DataSource dataSource = this.dataSources[datasetNumber];
    		if (dataSource instanceof DatabaseSchema) {
	    		((DatabaseSchema)dataSource).sqlUtils.disconnect();
    		}
		}
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
				String fieldName = field.getName();
				if (
						!fieldName.equals("localDatabase") &&
						!fieldName.equals("batchTab") && 
						!fieldName.equals("totalDataset") &&
						!fieldName.equals("createIndextemporaryFile") &&
						!fieldName.equals("datasets")	) {
					fieldObject = field.get(this);
					if (null==fieldObject) {
						stringBuilder.append( "(" + field.getType().getSimpleName() + ")" + 
								field.getName() + " = null, ");
					} else {
						stringBuilder.append( "(" + fieldObject.getClass().getSimpleName() + ")" + 
								field.getName() + " = " + fieldObject + ", ");
					}
				}
			}
		} catch (IllegalArgumentException e) {			
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		stringBuilder.append(MyUtils.LINE_SEPARATOR);
		stringBuilder.append(MyUtils.TAB_SEPARATOR + "localDatabase = " + this.localDatabase + MyUtils.LINE_SEPARATOR);
		stringBuilder.append(MyUtils.TAB_SEPARATOR + "totalDataset = " + this.totalDataset + MyUtils.LINE_SEPARATOR);
		stringBuilder.append(MyUtils.TAB_SEPARATOR + "batchTab = " + MyUtils.arrayToStringBuffer(batchTab) + MyUtils.LINE_SEPARATOR);
		stringBuilder.append(MyUtils.TAB_SEPARATOR + "createIndextemporaryFile = " + 
				(null!=createIndextemporaryFile ? createIndextemporaryFile.getAbsolutePath() : null) + MyUtils.LINE_SEPARATOR);
		for (int i = 0; i < this.totalDataset; i++) {
			stringBuilder.append(MyUtils.TAB_SEPARATOR + MyUtils.TAB_SEPARATOR + this.dataSources[i] + MyUtils.LINE_SEPARATOR);
		}
		
		stringBuilder.append( "]" );
		return stringBuilder.toString();
	}
}
