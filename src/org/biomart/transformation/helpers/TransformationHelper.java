package org.biomart.transformation.helpers;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.objects.MartConfiguratorUtils;
import org.biomart.objects.helpers.PartitionReference;
import org.biomart.old.martService.Configuration;
import org.biomart.old.martService.objects.DatasetInMart;
import org.biomart.old.martService.objects.MartInVirtualSchema;


public class TransformationHelper {

	private TransformationGeneralVariable general = null;
	private TransformationVariable vars = null;
	
	public TransformationHelper (TransformationGeneralVariable general, TransformationVariable vars) {
		this.general = general;
		this.vars = vars;
	}
	
	public boolean containsAliases(String value) {
		return value!=null && value.contains(TransformationConstants.ALIAS_DELIMITER);
	}
	
	public String replaceAliases(String value) throws FunctionalException {
		
		String valueCopy = null;
		if (value!=null) {
			valueCopy = new String(value);
			List<String> aliasList = extractAliases(valueCopy);
			for (String alias : aliasList) {
				Integer column = vars.getDdptColumnNameToColumnNumberMap().get(alias);
				if (null==column) {
					throw new FunctionalException("No value defined for alias: " + alias);
				}
				PartitionReference dynamicDatasetPartitionReference = new PartitionReference(vars.getDdPT(), column);
				valueCopy = valueCopy.replace(TransformationConstants.ALIAS_DELIMITER + alias + TransformationConstants.ALIAS_DELIMITER, 
						dynamicDatasetPartitionReference.toXmlString());
			}
		}
		return valueCopy;
	}
	
	public List<String> extractAliases(String value) {	//TODO use Pattern instead
		String[] split = value.split("\\" + TransformationConstants.ALIAS_DELIMITER);
		/*MyUtils.checkStatusProgram(split.length%2==0);*/
		Set<String> aliasSet = new HashSet<String>();
		for (int i = 1; i < split.length; i+=2) {
			aliasSet.add(split[i]);
		}
		return new ArrayList<String>(aliasSet);
	}
	
	public String unreference(String value, Integer mainRowNumber) throws FunctionalException {
		List<String> list = MartConfiguratorUtils.extractPartitionReferences(value);
		StringBuffer stringBuffer = new StringBuffer();
		for (int i = 0; i < list.size(); i++) {
			String s = list.get(i);
			if (i%2==1) {	// partition reference
				PartitionReference partitionReference = PartitionReference.fromString(s);
				s = partitionReference.getValue(vars.getNameToPartitionTableMap(), mainRowNumber);
				MyUtils.checkStatusProgram(null!=s, "value = " + value + ", partitionReference = " + partitionReference.toString());
			}
			stringBuffer.append(s);
		}
		return stringBuffer.toString();
	}

	public MartServiceIdentifier getNewTrueHostIdentifer(MartServiceIdentifier currentMartServiceIdentifier, String virtualSchema, String datasetName) {
		Configuration configuration = getConfiguration(currentMartServiceIdentifier);
		MyUtils.checkStatusProgram(null!=configuration, currentMartServiceIdentifier.formatMartServiceUrl());
		DatasetInMart datasetInMart = getDatasetInMart(configuration, virtualSchema, datasetName);
		MyUtils.checkStatusProgram(null!=datasetInMart, 
				currentMartServiceIdentifier.formatMartServiceUrl() + ", " + virtualSchema + ", " + datasetName);
		MartInVirtualSchema martInVirtualSchema = datasetInMart.getMartInVirtualSchema();
		return new MartServiceIdentifier(martInVirtualSchema);
	}

	public Configuration getConfiguration(MartServiceIdentifier martServiceIdentifier) {
		Map<String, Configuration> webServiceConfigurationMap = general.getWebServiceConfigurationMap();
		Configuration configuration = webServiceConfigurationMap.get(martServiceIdentifier.formatMartServiceUrl());
		MyUtils.checkStatusProgram(configuration!=null);
		return configuration;
	}

	public DatasetInMart getDatasetInMart(Configuration configuration, String virtualSchema, String datasetName) {
		
		DatasetInMart datasetInMart = null;
		
		DatasetInMart datasetInMartTmp = configuration.getDatasetInMart(virtualSchema, datasetName);
		if (null!=datasetInMartTmp) {
			MyUtils.checkStatusProgram(datasetInMart==null, datasetInMart + " / " + datasetInMartTmp);	// there should be only one
			datasetInMart = datasetInMartTmp;
		}

		// If still null, try without virtualSchema, there may be no conflict (most likely there won't)
		if (null==datasetInMart) {
			List<DatasetInMart> datasetInMartList = configuration.getDatasetInMartList(datasetName);
			/*exception*/MyUtils.checkStatusProgram(datasetInMartList.size()==1, "datasetInMartList = " + datasetInMartList);
			datasetInMart = datasetInMartList.get(0);
		}
		
		return datasetInMart;
	}
}
