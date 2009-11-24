package org.biomart.querying.queryCompiler;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.biomart.common.general.exceptions.FunctionalException;
import org.biomart.common.general.utils.MyUtils;
import org.biomart.martRemote.MartRemoteConstants;
import org.biomart.martRemote.MartRemoteUtils;
import org.biomart.martRemote.XmlParameters;
import org.biomart.martRemote.objects.request.Query;
import org.biomart.martRemote.objects.request.QueryDataset;
import org.biomart.objects.MartConfiguratorConstants;
import org.biomart.objects.MartConfiguratorUtils;
import org.biomart.objects.objects.Attribute;
import org.biomart.objects.objects.Config;
import org.biomart.objects.objects.Container;
import org.biomart.objects.objects.Dataset;
import org.biomart.objects.objects.Element;
import org.biomart.objects.objects.Filter;
import org.biomart.objects.objects.Location;
import org.biomart.objects.objects.Mart;
import org.biomart.objects.objects.MartRegistry;
import org.jdom.Document;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

public class QueryCompiler {

	public static MartRegistry martRegistry = null;
	public static Location location = null;
	public static Mart mart = null;
	public static Dataset dataset = null;
	public static Config config = null;
	public static Container rootContainer = null;
	
	public static void main(String[] args) throws Exception {
		
		martRegistry = MartConfiguratorUtils.loadSerializedMartRegistry(
	    				MartConfiguratorConstants.BIOMART_JAVA_SERIALIZED_PORTAL_FILE);
		location = martRegistry.getLocation("ensembl_mart_55");
		mart = location.getMart("ensembl_mart_55");
		dataset = mart.getDataset("gene_ensembl");
		config = dataset.getConfig("gene_ensembl_template");
		rootContainer = config.getRootContainer();
		
		List<Attribute> attributeList = new ArrayList<Attribute>();
		attributeList.add(rootContainer.getAttribute("ensembl_gene_id"));
		attributeList.add(rootContainer.getAttribute("ensembl_transcript_ids"));
		
		Map<Filter, String> filterMap = new HashMap<Filter, String>();
		filterMap.put(rootContainer.getFilter("chromosome_name"), "");
		
		System.out.println(MartRemoteConstants.QUERY_TEST_PROPERTIES_FILE_PATH_AND_NAME);
		String queryString = MyUtils.wrappedGetProperty(MartRemoteConstants.QUERY_TEST_PROPERTIES_FILE_PATH_AND_NAME, "query2");
		SAXBuilder builder = new SAXBuilder();
		Document documentTmp = builder.build(new StringReader(queryString));
		org.jdom.Element cloneRoot = (org.jdom.Element)documentTmp.getRootElement().clone();
		Document document = MartRemoteUtils.createNewMartRemoteXmlDocument(new XmlParameters(true, 
				Namespace.getNamespace("ns", "namespace"), Namespace.getNamespace("ns2", "namespace2"), "xsd"), "query");
		org.jdom.Element rootElement = document.getRootElement();
		rootElement.addContent(cloneRoot);
		
		QueryCompiler queryCompiler = new QueryCompiler();
		queryCompiler.compile(Query.fromXml(document));
	}
	
	public QueryCompiler() {}
	public String compile(Query query) throws FunctionalException {

		QueryDataset queryDataset = query.getQueryDataset();
		
		List<String> datasetNameList = new ArrayList<String>();
		List<QueryDataset> queryDatasetList = new ArrayList<QueryDataset>(Arrays.asList(new QueryDataset[] {queryDataset}));
		for (QueryDataset queryDatasetTmp : queryDatasetList) {
			datasetNameList.addAll(queryDatasetTmp.getDatasetNameList());
		}
		System.out.println("datasetNameList = " + datasetNameList);
		String datasetName = queryDataset.getDatasetNameList().get(0);
		
		// TODO look up dataset from it's unique name (portal wide)
		martRegistry.updateNameToDatasetMap();
		Dataset dataset = martRegistry.getDataset(datasetName);
		Config config = dataset.getConfigList().get(0);
		Container rootContainer = config.getRootContainer();
		System.out.println("dataset = " + dataset.getName());
		
		List<Attribute> attributeList = new ArrayList<Attribute>();
		for (String attributeName : queryDataset.getAttributeNameList()) {
			Attribute attribute = rootContainer.getAttributeRecursively(attributeName); //TODO account for partitions!
	MyUtils.checkStatusProgram(null!=attribute, attributeName);
			attributeList.add(attribute);
		}
		
		List<Filter> filterList = new ArrayList<Filter>();
		for (String filterName : queryDataset.getFilterNameMap().keySet()) {
			Filter filter = rootContainer.getFilterRecursively(filterName);
	MyUtils.checkStatusProgram(null!=filter, filterName);
			filterList.add(filter);
		}
		
		List<Element> elementList = new ArrayList<Element>();
		elementList.addAll(attributeList);
		elementList.addAll(filterList);
		
		System.out.println("elementList = " + elementList.size() + ":");
		for (Element element : elementList) {
			System.out.print(element.getName() + ", ");
		}
		System.out.println();
		
		/*Set<Dataset> datasetList = new HashSet<Dataset>();
		for (Element element : elementList) {
			String datasetName = element.getDatasetName();
			Dataset dataset = mart.getDataset(datasetName);	//FIXME
			MyUtils.checkStatusProgram(null!=dataset, "datasetName = " + datasetName);
			datasetList.add(dataset);
		}*/
		
		return "";
	}
}