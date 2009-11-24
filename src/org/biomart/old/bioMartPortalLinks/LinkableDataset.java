package org.biomart.old.bioMartPortalLinks;


import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.biomart.common.general.utils.MyUtils;
import org.biomart.old.martService.MartServiceConstants;
import org.biomart.old.martService.objects.Exportable;
import org.biomart.old.martService.objects.Importable;
import org.biomart.old.martService.restFulQueries.objects.Filter;





public class LinkableDataset implements Serializable {

	private static final long serialVersionUID = -3631756958938310103L;
	
	String bioMartVersion = null;
	String serverVirtualSchema = null;
	String mart = null;
	String datasetName = null;
	Boolean visible = null;
	String datasetType = null;
	List<Importable> importableList = null;
    List<Exportable> exportableList = null;
    public Map<String, org.biomart.old.martService.restFulQueries.objects.Element> attributesByNameMap = null;
    public Map<String, org.biomart.old.martService.restFulQueries.objects.Element> filtersByNameMap = null;
    public Map<String, Filter> mapRemoteDatasetToExternalFilterPointer = null;	// Only one for now
        
    public LinkableDataset(String bioMartVersion, String serverVirtualSchema, String mart, String dataset, Boolean visible, String datasetType, 
    		List<Importable> importableList, List<Exportable> exportableList, Map<String, Filter> mapRemoteDatasetToFilterPointer) {
		super();
		this.bioMartVersion = bioMartVersion==null ? 
				MartServiceConstants.DEFAULT_BIOMART_VERSION : bioMartVersion;
		this.serverVirtualSchema = serverVirtualSchema;
		this.mart = mart;
		this.datasetName = dataset;
		this.visible = visible;
		this.datasetType = datasetType;
		this.importableList = importableList;
		this.exportableList = exportableList;
		this.mapRemoteDatasetToExternalFilterPointer = mapRemoteDatasetToFilterPointer;
	}
    
    
	@Override
	public String toString() {
		return
		"bioMartVersion = " + bioMartVersion + ", " +
		"serverVirtualSchema = " + serverVirtualSchema + ", " +
		"mart = " + mart + ", " +
		"dataset = " + datasetName + ", " +
		"visibility = " + visible + ", " +
		"datasetType = " + datasetType + ", " +
		"importableList = " + importableList + ", " +
		"exportableList = " + exportableList;
	}
	public String toShortString() {
		return 
		"importableList.size() = " + importableList.size() + "," + MyUtils.TAB_SEPARATOR +
		"exportableList.size() = " + exportableList.size() + "," + MyUtils.TAB_SEPARATOR +
		"bmv = " + bioMartVersion + MyUtils.TAB_SEPARATOR +
		"vs = " + serverVirtualSchema + MyUtils.TAB_SEPARATOR +
		"m = " + mart + MyUtils.TAB_SEPARATOR +
		"ds = " + datasetName + MyUtils.TAB_SEPARATOR +
		"v = " + visible + MyUtils.TAB_SEPARATOR +
		"t = " + datasetType;
	}
	public String toImpExpOrientedString() {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("bmv = " + bioMartVersion + ", " + "vs = " + serverVirtualSchema + ", " + 
				"mart = " + mart + ", " + "dataset = " + datasetName  + ", " + 
				"visibility = " + visible + MyUtils.LINE_SEPARATOR  + ", " + "type = " + datasetType + MyUtils.LINE_SEPARATOR);
		stringBuffer.append(MyUtils.TAB_SEPARATOR + importableList.size() + MyUtils.LINE_SEPARATOR);
		for (int i = 0; i < importableList.size(); i++) {
			stringBuffer.append(MyUtils.TAB_SEPARATOR + MyUtils.TAB_SEPARATOR + importableList.get(i) + MyUtils.LINE_SEPARATOR);
		}
		stringBuffer.append(MyUtils.TAB_SEPARATOR + exportableList.size() + MyUtils.LINE_SEPARATOR);
		for (int i = 0; i < exportableList.size(); i++) {
			stringBuffer.append(MyUtils.TAB_SEPARATOR + MyUtils.TAB_SEPARATOR + exportableList.get(i) + MyUtils.LINE_SEPARATOR);
		}
		return stringBuffer.toString();
	}
    public boolean sameVirtualSchema(LinkableDataset ds) {
    	return this.serverVirtualSchema.equals(ds.serverVirtualSchema);
    }
	@Override
	public boolean equals(Object object) {
		LinkableDataset linkableDataset = (LinkableDataset)object;
		return
		this.bioMartVersion.equals(linkableDataset.bioMartVersion) &&
		this.serverVirtualSchema.equals(linkableDataset.serverVirtualSchema) &&
		this.mart.equals(linkableDataset.mart) &&
		this.datasetName.equals(linkableDataset.datasetName) &&
		this.visible.equals(linkableDataset.visible) &&
		this.datasetType.equals(linkableDataset.datasetType) &&
		this.importableList.equals(linkableDataset.importableList) &&
		this.exportableList.equals(linkableDataset.exportableList);
	}
	@Override
	public int hashCode() {
		return 0;
	}
}
